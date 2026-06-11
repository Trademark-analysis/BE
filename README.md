# 🔍 TrademarkAI — Backend Server

> 변리사의 선행조사 시간을 줄이는 Mircrosoft Azure 기반 상표 검색 서비스

---

## 📌 프로젝트 개요


변리사의 선행조사 시간을 줄이는 상표 검색 서비스 **TrademarkAI**의 백엔드 서버입니다.  
프론트엔드 요청을 받아 ML 분석 서버와 통신하고, 분석 결과를 Azure SQL에 저장·조회하는 역할을 합니다.

---

## 🏗️ 시스템 아키텍처

```
Frontend (React + TypeScript)
  ↕ REST API
[BE] Spring Boot Server
  ↕
ML Server (Python)  —  Azure AI Services (Custom Vision, GPT-4o, AI Vision)
  ↕
Azure SQL (분석 결과 저장)
```

---

## 🛠️ 기술 스택

- **Language**: Java
- **Framework**: Spring Boot
- **Database**: Azure SQL
- **통신**: REST API (FE ↔ BE ↔ ML)

---

## 🔄 주요 기능

- 상표 이미지 및 서비스 정보 수신 → ML 서버로 분석 요청 전달
- 1차·2차·3차 분석 결과 수신 및 Azure SQL 저장
- 유사 후보 목록 및 AI 리포트 조회 API 제공
- 분석 완료 후 상표 이미지 삭제 (보안성 확보)

---

## 💻 로컬 실행 방법

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun
```

> ML 서버 및 Azure SQL 연결 설정이 필요합니다. (환경 변수 또는 `application.properties` 설정)

---

## 📎 관련 레포지토리

- [FE](https://github.com/Trademark-analysis/FE) — React + TypeScript 프론트엔드
- [ML](https://github.com/Trademark-analysis/ML) — Python ML 분석 서버
