package com.example.demo.service;

import com.example.demo.domain.SelectedClassification;
import com.example.demo.domain.TrademarkAnalysis;
import com.example.demo.dto.ClassificationResponse;
import com.example.demo.dto.ServiceInfoRequest;
import com.example.demo.dto.TrademarkAnalysisDto;
import com.example.demo.repository.TrademarkAnalysisRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class TrademarkService {

    private final TrademarkAnalysisRepository trademarkAnalysisRepository;
    private final WebClient webClient;

    private final String PYTHON_SERVER_URL = "http://localhost:8000";

    public TrademarkService(TrademarkAnalysisRepository trademarkAnalysisRepository) {
        this.trademarkAnalysisRepository = trademarkAnalysisRepository;
        this.webClient = WebClient.builder().baseUrl(PYTHON_SERVER_URL).build();
    }

    public List<ClassificationResponse> getClassificationCodes(ServiceInfoRequest request) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("trademarkName", request.getTrademarkName());
            body.put("serviceDescription", request.getServiceDescription());

            List<Map<String, Object>> mlResponse = webClient.post()
                    .uri("/api/ml/classification")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            List<ClassificationResponse> result = new ArrayList<>();

            if (mlResponse == null) {
                return result;
            }

            for (Map<String, Object> item : mlResponse) {
                String code = String.valueOf(item.get("code"));
                String description = String.valueOf(item.get("description"));

                result.add(new ClassificationResponse(code, description));
            }

            return result;

        } catch (Exception e) {
            System.out.println("ML 니스분류 추천 통신 예외: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Map<String, Object> getSimilarGroups(ServiceInfoRequest request) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("trademarkName", request.getTrademarkName());
            body.put("serviceDescription", request.getServiceDescription());
            body.put("selectedNiceClasses", request.getSelectedNiceClasses());

            Map<String, Object> mlResponse = webClient.post()
                    .uri("/api/ml/similar-groups")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (mlResponse == null) {
                return new HashMap<>();
            }

            return mlResponse;

        } catch (Exception e) {
            System.out.println("ML 유사군 코드 추천 통신 예외: " + e.getMessage());

            Map<String, Object> fallback = new HashMap<>();
            fallback.put("trademarkName", request.getTrademarkName());
            fallback.put("nice_codes", new ArrayList<>());
            fallback.put("similar_group_codes", new ArrayList<>());
            fallback.put("message", "유사군 코드 추천 실패");

            return fallback;
        }
    }

    @Transactional
    public TrademarkAnalysisDto saveAndAnalyze(String trademarkName, String serviceDescription, List<String> selectedCodes, MultipartFile imageFile) {

        // [사진 깨짐 해결]: 파이썬 서버 static 경로 매핑
        String savedImageUrl = "http://localhost:8000/static/" + imageFile.getOriginalFilename();

        String similarityScore = "0%";
        Boolean isAvailable = true;
        String resultMessage = "분석 진행 완료";
        Object distinctivenessScore = null;

        // 프론트엔드 결과 화면에 리스트를 쏴주기 위해 파이썬 통신 원본 결과 객체를 담아둘 저장소 생성
        List<Map<String, Object>> similarTrademarkList = new ArrayList<>();

        try {
            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();

            bodyBuilder.part("trademarkName", trademarkName);
            bodyBuilder.part("serviceDescription", serviceDescription);
            bodyBuilder.part("selectedNiceClasses", selectedCodes);

            bodyBuilder.part("image", new ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() { return imageFile.getOriginalFilename(); }
            }, MediaType.parseMediaType(imageFile.getContentType()));

            // 파이썬 통합 ML 서버 호출
            Map<String, Object> mlResponse = webClient.post()
                    .uri("/api/ml/analyze")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            System.out.println("=== ML 전체 응답 확인 ===");
            System.out.println(mlResponse);

            // [무한 루프 차단 수정]: 파이썬 성공 사인이 오면 껍데기 여부와 무관하게 무조건 추출 시작
            if (mlResponse != null) {
                System.out.println("=== 📥 [자바 서비스] 파이썬 통합 ML 결과 인입 성공 ===");

                // 3차 리포트 가공 영역 파싱
                Map<String, Object> similarityAssessment = (Map<String, Object>) mlResponse.get("similarity_assessment");
                if (similarityAssessment != null) {
                    similarityScore = String.valueOf(similarityAssessment.get("overall_similarity_score")) + "%";
                    String level = String.valueOf(similarityAssessment.get("level"));
                    isAvailable = !"HIGH".equals(level);
                }

                Map<String, Object> finalReport = (Map<String, Object>) mlResponse.get("final_report");
                if (finalReport != null) {
                    resultMessage = String.valueOf(finalReport.get("분석 요약 리포트"));
                }

                // 2차 검사 결과인 'similar_trademark' 리스트를 통째로 포착하여 보관함에 전달
                if (mlResponse.get("similar_trademark") != null) {
                    similarTrademarkList = (List<Map<String, Object>>) mlResponse.get("similar_trademark");
                }

                if (mlResponse.get("distinctiveness_score") != null) {
                    distinctivenessScore = mlResponse.get("distinctiveness_score");
                } else if (finalReport != null && finalReport.get("식별력 검사") instanceof Map) {
                    Map<String, Object> distinctivenessCheck =
                            (Map<String, Object>) finalReport.get("식별력 검사");

                    distinctivenessScore = distinctivenessCheck.get("score");
                }
            }

        } catch (Exception e) {
            System.out.println("ML 파이프라인 가동 통신 예외: " + e.getMessage());
            similarityScore = "99%";
            isAvailable = false;
            resultMessage = "서버 통신 예외 발생으로 인한 폴백 모드 가동";
        }

        // DB Entity 영속화
        TrademarkAnalysis analysis = new TrademarkAnalysis();
        analysis.setTrademarkName(trademarkName);
        analysis.setServiceDescription(serviceDescription);
        analysis.setImageUrl(savedImageUrl);
        analysis.setSimilarityScore(similarityScore);
        analysis.setIsAvailable(isAvailable);
        analysis.setResultMessage(resultMessage);

        for (String code : selectedCodes) {
            SelectedClassification selectedClassification = new SelectedClassification(code);
            analysis.addClassification(selectedClassification);
        }

        TrademarkAnalysis savedEntity = trademarkAnalysisRepository.save(analysis);

        // DTO 패키징 및 프론트 전송용 커스텀 확장
        TrademarkAnalysisDto dto = new TrademarkAnalysisDto(savedEntity);
        dto.setSimilarTrademark(similarTrademarkList);
        dto.setDistinctivenessScore(distinctivenessScore);

        return dto;
    }

    public Map<String, Object> getDetailReport(String candidateName) {
        Map<String, Object> report = new HashMap<>();
        report.put("inputTrademarkName", "STARBOX COFFEE");
        report.put("candidateName", candidateName != null ? candidateName : "STARBUKS COFFEE");
        report.put("totalSimilarity", "99%");
        report.put("imageSimilarity", "98.4%");
        report.put("textSimilarity", "92.1%");
        report.put("similarityStatus", "고위험 유사성");
        report.put("analysisSummary", "이미지 분석에서는 별 모양 로고 구조와 배치 방식이 유사하게 나타났으며, 문자 분석에서는 단어 구조와 브랜드명이 높은 유사도를 보였습니다.");

        Map<String, String> niceCode = new HashMap<>();
        niceCode.put("class", "Class 30");
        niceCode.put("description", "Coffee, Tea, Pastries");
        report.put("niceCode", niceCode);

        List<Map<String, String>> viennaCodes = new ArrayList<>();
        Map<String, String> v1 = new HashMap<>(); v1.put("code", "01.01 (Match)"); v1.put("desc", "별");
        Map<String, String> v2 = new HashMap<>(); v2.put("code", "27.05 (Match)"); v2.put("desc", "특수 문자·장식화된 문자");
        viennaCodes.add(v1); viennaCodes.add(v2);
        report.put("viennaCodes", viennaCodes);

        List<Map<String, String>> currentStatus = new ArrayList<>();
        Map<String, String> s1 = new HashMap<>(); s1.put("brand", "STARBUKS"); s1.put("date", "1971.03.30"); s1.put("status", "등록 완료");
        Map<String, String> s2 = new HashMap<>(); s2.put("brand", "My Trademark (STARBOX)"); s2.put("date", "현재"); s2.put("status", "출원 준비 중");
        currentStatus.add(s1); currentStatus.add(s2);
        report.put("currentStatus", currentStatus);

        return report;
    }
}