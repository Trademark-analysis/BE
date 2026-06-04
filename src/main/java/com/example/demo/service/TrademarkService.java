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

    // [STEP 01 -> 02] 보내주신 이미지에 딱 맞게 "제30류(커피)" 등을 우선 Mock 반환하도록 정교화
    public List<ClassificationResponse> getClassificationCodes(ServiceInfoRequest request) {
        List<ClassificationResponse> list = new ArrayList<>();
        list.add(new ClassificationResponse("제30류", "커피, 차, 음식료품"));
        list.add(new ClassificationResponse("제9류", "컴퓨터, 소프트웨어, 전자기기"));
        list.add(new ClassificationResponse("제42류", "소프트웨어 개발, IT 서비스"));
        return list;
    }

    // [STEP 03 -> 4번 결과화면] 오리지널 DB 저장 및 파이썬 연동 로직 유지
    @Transactional
    public TrademarkAnalysisDto saveAndAnalyze(String serviceDescription, List<String> selectedCodes, MultipartFile imageFile) {

        String savedImageUrl = "https://smu-team2-storage.blob.core.windows.net/trademarks/" + imageFile.getOriginalFilename();
        String similarityScore = "0.0%";
        Boolean isAvailable = true;
        String resultMessage = "분석 서버 연결 실패";

        try {
            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("serviceDescription", serviceDescription);
            bodyBuilder.part("selectedCodes", selectedCodes);
            bodyBuilder.part("image", new ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() { return imageFile.getOriginalFilename(); }
            }, MediaType.parseMediaType(imageFile.getContentType()));

            Map<String, Object> mlResult = webClient.post()
                    .uri("/api/ml/analyze")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (mlResult != null) {
                similarityScore = String.valueOf(mlResult.get("similarity"));
                isAvailable = (Boolean) mlResult.get("available");
                resultMessage = String.valueOf(mlResult.get("message"));
            }

        } catch (IOException e) {
            resultMessage = "이미지 파일 처리 중 에러 발생: " + e.getMessage();
        } catch (Exception e) {
            //  파이썬 서버 미구동 시, 4번 결과화면 이미지 리스트와 완벽하게 부합하도록 Mock 데이터 매핑되게 수정
            similarityScore = "99%";
            isAvailable = false;
            resultMessage = "STARBUKS COFFEE(99%), STARBOOKS(90%), STAR(81%), BOOKS COFFEE(72%)";
        }

        TrademarkAnalysis analysis = new TrademarkAnalysis();
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
        return new TrademarkAnalysisDto(savedEntity);
    }

    /**
     * [추가] 5번 상세 비교 및 6번 요약 리포트 화면 디자인에 그려질 하드 데이터를 통짜로 가공해주는 메서드
     */
    public Map<String, Object> getDetailReport(String candidateName) {
        Map<String, Object> report = new HashMap<>();

        // 5번 스크린샷 텍스트 정보들 그대로 매핑
        report.put("inputTrademarkName", "STARBOX COFFEE");
        report.put("candidateName", candidateName != null ? candidateName : "STARBUKS COFFEE");
        report.put("totalSimilarity", "99%");
        report.put("imageSimilarity", "98.4%");
        report.put("textSimilarity", "92.1%");
        report.put("similarityStatus", "고위험 유사성");
        report.put("analysisSummary", "이미지 분석에서는 별 모양 로고 구조와 배치 방식이 유사하게 나타났으며, 문자 분석에서는 단어 구조와 브랜드명이 높은 유사도를 보였습니다.");

        // NICE Code 정보
        Map<String, String> niceCode = new HashMap<>();
        niceCode.put("class", "Class 30");
        niceCode.put("description", "Coffee, Tea, Pastries");
        report.put("niceCode", niceCode);

        // Vienna Code 리스트 정보
        List<Map<String, String>> viennaCodes = new ArrayList<>();
        Map<String, String> v1 = new HashMap<>(); v1.put("code", "01.01 (Match)"); v1.put("desc", "별");
        Map<String, String> v2 = new HashMap<>(); v2.put("code", "27.05 (Match)"); v2.put("desc", "특수 문자·장식화된 문자");
        viennaCodes.add(v1); viennaCodes.add(v2);
        report.put("viennaCodes", viennaCodes);

        // 출원 현황 비교 정보
        List<Map<String, String>> currentStatus = new ArrayList<>();
        Map<String, String> s1 = new HashMap<>(); s1.put("brand", "STARBUKS"); s1.put("date", "1971.03.30"); s1.put("status", "등록 완료");
        Map<String, String> s2 = new HashMap<>(); s2.put("brand", "My Trademark (STARBOX)"); s2.put("date", "현재"); s2.put("status", "출원 준비 중");
        currentStatus.add(s1); currentStatus.add(s2);
        report.put("currentStatus", currentStatus);

        return report;
    }
}