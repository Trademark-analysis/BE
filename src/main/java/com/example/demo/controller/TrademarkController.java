package com.example.demo.controller;

import com.example.demo.dto.AnalysisResponse;
import com.example.demo.dto.ClassificationResponse;
import com.example.demo.dto.ServiceInfoRequest;
import com.example.demo.dto.TrademarkAnalysisDto;
import com.example.demo.service.TrademarkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trademark")
@CrossOrigin(origins = "*") // 프론트엔드 포트가 다를 경우 CORS 에러 방지
public class TrademarkController {

    private final TrademarkService trademarkService;

    public TrademarkController(TrademarkService trademarkService) {
        this.trademarkService = trademarkService;
    }

    /**
     * [STEP 01] 서비스/회사 설명을 바탕으로 유사군 코드를 조회
     */
    @PostMapping("/classification")
    public ResponseEntity<List<ClassificationResponse>> getClassification(@RequestBody ServiceInfoRequest request) {
        List<ClassificationResponse> response = trademarkService.getClassificationCodes(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/similar-groups")
    public ResponseEntity<Map<String, Object>> getSimilarGroups(@RequestBody ServiceInfoRequest request) {
        Map<String, Object> response = this.trademarkService.getSimilarGroups(request);
        return ResponseEntity.ok(response);
    }

    /**
     * [STEP 03] 선택된 유사군 코드들과 상표 이미지를 전달받아 ML 분석 결과를 반환
     */
    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyzeAndSave(
            @RequestParam("trademarkName") String trademarkName, // 프론트한테 받기!
            @RequestParam("serviceDescription") String serviceDescription,
            @RequestParam("selectedCodes") List<String> selectedCodes,
            @RequestParam("image") MultipartFile imageFile) {

        // Service 단으로 trademarkName도 함께 넘겨서 파이썬으로 토스
        TrademarkAnalysisDto resultDto = trademarkService.saveAndAnalyze(trademarkName, serviceDescription, selectedCodes, imageFile);
        AnalysisResponse response = new AnalysisResponse("SUCCESS", resultDto);
        return ResponseEntity.ok(response);
    }

    /**
     * [추가] 5번, 6번 '상세 비교 및 분석 요약 리포트' 화면에 필요한 상세 정보들을 가져옴
     * candidateName 파라미터로 "STARBUKS COFFEE" 등을 넘겨받아 화면 맞춤 데이터를 반환
     */
    @GetMapping("/report/detail")
    public ResponseEntity<Map<String, Object>> getDetailReport(@RequestParam("candidateName") String candidateName) {
        Map<String, Object> report = trademarkService.getDetailReport(candidateName);
        return ResponseEntity.ok(report);
    }
}