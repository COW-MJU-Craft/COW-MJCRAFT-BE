# CI/CD·하네스 리팩토링 계획

> ✅ **완료 (2026-07-12)** — ①~⑥ 전부 main 병합·배포됨. 기록용 아카이브.
> 추가 반영: smoke check JSON 검증 강화, Boot 4 starter-flyway 대응, V2 드리프트 수정(BASIC enum·answer LONGTEXT), Feedback 엔티티 LONGTEXT 명시.

> 작성: 2026-07-11 · 기준: origin/main `c24e528` (PR #116 머지 시점)
> 근거: Claude·Codex 교차 리뷰 수렴 결과. 완료 시 이 문서 상단에 ✅ 배너를 달고 아카이브할 것.

## 현재 평가 (백엔드)

| 영역 | 점수 | 비고 |
|---|---:|---|
| CI 게이트 | 7.0/10 | diff-coverage 70%·gitleaks 실제 차단 이력 있음 |
| CD | 4.5/10 | "검증된 배포"가 아닌 "배포 요청 자동화" |
| 하네스 | 7.2/10 | 안전장치는 좋으나 컨텍스트 신선도 낮음 |

핵심 진단: 문제는 CI–deploy 분리가 아니라, **PR CI가 배포 산출물(Docker 이미지)을 검증하지 않고, deploy가 배포 완료를 확인하지 않는 것**. 그리고 `ddl-auto: update`가 유일한 비가역 리스크.

## 작업 목록 (실행 순서)

### ① 배포 후 health 폴링 + smoke check — 반나절
- `spring-boot-starter-actuator` 추가, `/actuator/health` 노출
- deploy.yml 마지막 스텝: 운영 URL 30초 간격 폴링(최대 5분) + `/api/projects` 200 확인, 실패 시 워크플로우 실패
- 효과: "Actions 초록불 = 실제 서비스 정상" 보장. 현재는 컨테이너 부팅 실패해도 초록불.

### ② PR CI에 Docker build 검증 — 1시간
- ci.yml에 `docker build .` job 추가 (push 없음)
- 효과: Dockerfile·패키징 파손을 merge 전에 발견. GHCR 이미지명 대문자 문제로 배포 빌드 깨졌던 사례 재발 방지.
- 비용: PR CI 2~3분 증가.

### ③ Flyway 도입 + prod `ddl-auto: validate` — 가장 신중하게
- Flyway 의존성 추가 → 운영 스키마 덤프로 `V1__baseline.sql` 생성 → `baseline-on-migrate` 설정 → prod를 `validate`로 전환
- 이후 모든 스키마 변경은 마이그레이션 파일로 (코드 리뷰 대상화)
- 효과: **유일하게 데이터 손상을 막는 항목.** 현재는 엔티티 수정 시 Hibernate가 운영 DB를 이력 없이 변경. 롤백 시 DB 호환성 판단 기준 확보.

### ④ Testcontainers MySQL 테스트 1개 — ③ 이후, 1~2시간
- 빈 MySQL 컨테이너에 migration 전체 적용 → Spring context 기동 → 핵심 repository 쿼리 1~2개 확인
- 효과: H2–MySQL 동작 차이, migration 파손 조기 발견. GitHub runner에 Docker 내장이라 CI에서 그대로 동작.
- 주의: ③ 없이 넣으면 `ddl-auto: update`가 스키마를 자동 보정해 검증 가치가 없음.

### ⑤ AGENTS.md 최신화 + 핸드오프 문서 정리 — 1~2시간
- 실제 `src/main/java` 구조 기준으로 모듈 트리·표 재작성. 제거된 OAuth·`accounts/user`·`mypage` 삭제 (AGENTS.md 73~129행 부근)
- `docs/agent-harness-handoff.md`: 작업 완료·main 머지 상태이므로 ✅ 배너 후 아카이브(또는 삭제). 영구 정보 2개만 AGENTS.md로 이관: clone 후 `bash scripts/setup-hooks.sh` 필요, gitleaks 미설치 시 grep 폴백
- 하네스 컨벤션 추가: 핸드오프/상태 문서에 작성 시점 HEAD SHA·브랜치 기록, 완료 시 처리 규칙 명시
- 효과: 에이전트가 존재하지 않는 구조를 전제로 계획하는 stale-context 실패 모드 제거.

### ⑥ deploy를 CI 성공 뒤로 연결 — 30분, 마지막
- deploy.yml 트리거를 `workflow_run`(CI 완료 + `conclusion == success`)으로 변경
- 효과: merge skew·직접 push 구멍 봉합. 단독 효과는 작아 마지막 순서.

## 하지 않기로 한 것

- 기존 코드 커버리지(line ~11%) 끌어올리기 캠페인 — diff 70% 게이트가 자연히 해결
- MinIO Testcontainers — presign/업로드 실제 장애가 반복될 때
- 자동 롤백·staging·canary — 현 규모에 과잉. 롤백은 "Coolify에서 이전 SHA 이미지 재배포" 절차를 문서 한 단락으로 기록
- 보안 경로 승인 게이트 — 솔로 운영이라 승인 0명 유지 (2인 이상 되면 CODEOWNERS + 승인 1명)

## 완료 기준

①~⑥ 반영 후: 배포 실패가 Actions에서 빨간불로 보이고, 스키마 변경이 전부 마이그레이션 파일로 남으며, AGENTS.md가 실제 코드 구조와 일치. 예상 평가 CI/CD 6.0 → 7점대.
