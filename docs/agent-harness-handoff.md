# 에이전트 하네스 작업 핸드오프 (2026-07-09 새벽 작업분)

`chore/agent-harness` 브랜치에 Phase 1~4 완료. **push는 안 했음** — 아래 체크리스트 순서대로 진행하면 됨.

## 커밋 내역

| 커밋 | 내용 |
|---|---|
| Phase 1 | `.agents/commands/` 단일소스화 + `.claude`/`.codex` 심링크 + `CLAUDE.md`(@AGENTS.md) + gitignore |
| Phase 2 | `CODEOWNERS` + CI에 gitleaks 시크릿 스캔 + 심링크 구조 검증 job |
| Phase 3 | `pre-commit` 훅(main 커밋 차단·시크릿 차단) + `.claude/settings.json`(팀 공유 권한) |
| Phase 4 | JaCoCo + PR diff coverage 70% 게이트 + AGENTS.md 테스트 컨벤션 + `/impl` 테스트 필수화 |

## 검증 완료

- pre-commit 훅 5개 케이스 테스트 통과: main 커밋 차단 / 정상 커밋 허용 / AWS 키 차단 / yml 리터럴 시크릿 차단(`${ENV}` 플레이스홀더는 허용) / `.env` 차단
- ci.yml YAML 유효성, settings.json JSON 유효성, 심링크 동작 확인
- 훅 자기 자신 오탐 이슈 수정 완료 (`.githooks/` 스캔 제외)

## ⚠️ 미검증 (일어나서 확인 필요)

1. **`./gradlew compileJava && ./gradlew test`** — 샌드박스에서 gradle 다운로드 불가로 build.gradle(jacoco 추가) 미검증. 표준 블록이라 문제 없을 가능성 높지만 확인 필수
2. **diff-cover CI 스텝** — 첫 PR에서 실제 동작 확인 (jacoco XML 경로: `build/reports/jacoco/test/jacocoTestReport.xml`)

## 아침 체크리스트

1. `git switch chore/agent-harness && git diff main` — 전체 변경 검토
2. `./gradlew compileJava && ./gradlew test` — 빌드 확인
3. `bash scripts/setup-hooks.sh` — pre-commit 활성화 (팀원들도 clone 후 각자 실행 필요)
4. push + PR 생성 → CI 3개 job(test, secret-scan, agent-config) 통과 확인
5. **GitHub branch protection 설정** (수동, ~5분): Settings → Branches → Add branch ruleset (main)
   - Require a pull request before merging + Require review from Code Owners
   - Require status checks: `test`, `secret-scan`, `agent-config`
   - Block force pushes
6. CODEOWNERS의 `@yunjin1213` 핸들 확인, 팀원 추가 (예: 배한준 핸들)

## 남은 Phase (계획서 참고: docs/agent-harness-plan.md)

- **Phase 5** (branch protection 활성화 후): 절대 규칙 완화 — feat/* push 허용, draft PR 생성 허용, 에이전트 라벨. `.claude/settings.json`의 `git push` deny 제거 포함
- **Phase 6** (컨벤션 위반이 반복될 때): JPA/트랜잭션/로깅 컨벤션, Spotless, ArchUnit, skills

## 참고

- diff coverage는 PR에만 적용 (일반 push는 테스트만)
- gitleaks CI는 도커 이미지(`ghcr.io/gitleaks/gitleaks:v8.21.2`) 사용 — 별도 라이선스 불필요
- 로컬 gitleaks 미설치 시 훅은 grep 폴백으로 동작 (`brew install gitleaks` 권장)
