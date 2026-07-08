# AI 에이전트 하네스 통합 — 최종 구현 계획

Claude Code와 Codex가 **하나의 설정 소스**를 공유하고, 문서로만 존재하던 규칙을 **도구로 강제**하며, 서버 방어선 확보 후 에이전트에게 **push/draft PR까지 위임**하는 계획.

원칙: "CI가 안 잡으면 규칙이 아니다" / 에이전트 PR도 사람 PR과 같은 게이트 통과 / 커버리지는 전체가 아닌 **신규 코드 기준**.

---

## 현황 진단

| 항목 | 상태 | 문제 |
|---|---|---|
| `AGENTS.md` | 잘 작성됨 (Codex 네이티브 인식) | Claude Code는 `CLAUDE.md`만 읽음 → **Claude Code에서 규칙 미적용** |
| `.claude/commands/` `.codex/commands/` | 커맨드 6개 이중 관리 | 내용 이미 어긋남. `.codex` 쪽이 최신 (AGENTS.md 참조로 중복 제거) |
| Codex custom prompts | — | repo `.codex/commands`는 네이티브 인식 안 됨. custom prompts deprecated → skills(`.agents/skills/`)가 공식 대체 |
| 절대 규칙 (push/main/보안리뷰) | 문서로만 존재 | 강제 수단 없음. branch protection 미설정 |
| 테스트 | main 323개 대비 test 4개 | 커버리지 사실상 0% → 전체 커버리지 게이트 불가, diff 기준 필요 |
| `.claude/settings.json` | 없음 (local만) | 팀 공유 permission 없음. `settings.local.json`이 gitignore에 없음 |

---

## 목표 구조

```
cow-mju-craft/
├── AGENTS.md                  # 단일 원본 (규칙/컨벤션 전부)
├── CLAUDE.md                  # 내용: "@AGENTS.md" 한 줄 (Claude Code import)
├── .agents/
│   ├── commands/              # 커맨드 원본 6개 (canonical)
│   └── skills/                # (추후) Codex 공식 skills 경로
├── .claude/
│   ├── commands → ../.agents/commands   # 심링크
│   ├── settings.json          # 팀 공유: permissions + hooks (git 추적)
│   └── settings.local.json    # 개인용 (gitignore 추가)
├── .codex/
│   └── commands → ../.agents/commands   # 심링크
├── .github/
│   ├── CODEOWNERS             # 신규: 보안 경로 사람 리뷰 강제
│   └── workflows/ci.yml       # 수정: coverage 게이트 + 심링크 체크 + gitleaks
├── .githooks/
│   └── pre-commit             # 신규: main 커밋 차단 + 시크릿 스캔
└── scripts/setup-hooks.sh     # pre-commit 등록 추가
```

---

## Phase 1 — 단일소스화

1. **커맨드 병합**: `.codex/commands/*.md`(최신) 기준으로 `.claude` 쪽 개선점(review.md의 Swagger FQN 체크 등) 병합 → `.agents/commands/`로 이동
2. **심링크**: `.claude/commands`, `.codex/commands` → `../.agents/commands`
3. **CLAUDE.md 생성**: `@AGENTS.md` 한 줄
4. **AGENTS.md 수정**: 커맨드 경로 테이블을 `.agents/commands/<command>.md` 단일 경로로 교체
5. `.gitignore`에 `.claude/settings.local.json` 추가

주의: Windows 협업자 발생 시 심링크 → sync 스크립트 전환 검토.

---

## Phase 2 — 서버 방어선 (절대 규칙의 GitHub 강제)

로컬 훅은 우회 가능하므로 서버가 최종 방어선. **Phase 5(권한 완화)의 선행 조건.**

1. **Branch protection (main)**: PR 필수, force push 차단, CI 통과 필수, 리뷰 1인 필수 → 절대 규칙 1·2·3 서버 강제
2. **CODEOWNERS**: `SecurityConfig`, `global/config/jwt/**` 등 보안 경로 → 사람 리뷰 필수 (절대 규칙 5). 리스크 티어별 리뷰 효과
3. **CI 보강** (`ci.yml`):
   - gitleaks 시크릿 스캔 (절대 규칙 4)
   - 심링크 유효성 체크 (Phase 1 구조 깨진 채 병합 방지)

---

## Phase 3 — 로컬 하네스

1. **`.githooks/pre-commit`**: main 직접 커밋 차단 + gitleaks protect (staged 스캔). Codex는 hooks 기능이 없으므로 git hook이 양쪽 공통 강제 수단. `setup-hooks.sh`에 등록
2. **`.claude/settings.json`** (팀 공유):
   - allow: `./gradlew compileJava`, `./gradlew test *`, `git add *` 등 안전 항목 승격
   - deny: `git push --force*`, `git commit * --no-verify`

---

## Phase 4 — 테스트 & 커버리지 게이트

전체 커버리지 하드 게이트는 깡통 테스트 게이밍 유발(에이전트가 특히 잘함) → **diff coverage** 채택.

1. **JaCoCo 도입** (`build.gradle`): `jacocoTestReport` XML 출력
2. **신규 코드 커버리지 게이트**: Codecov patch coverage(또는 diff-cover)로 "PR에서 변경된 코드의 70% 이상" — 기존 부채와 무관하게 즉시 적용 가능
3. **전체 커버리지는 래칫**: 현재 수치를 하한선으로, 오를 때마다 상향
4. **테스트 컨벤션을 AGENTS.md에 추가**: 네이밍 `메서드명_상황_기대결과`, given-when-then, 계층별 전략(서비스=Mockito 단위 / 리포지토리=`@DataJpaTest` / 컨트롤러=`@WebMvcTest`)
5. **`/impl` 커맨드 수정**: "구현 완료 = 테스트 포함" 단계 명시

---

## Phase 5 — 절대 규칙 완화 + push/PR 위임 (Phase 2 완료 후에만)

에이전트 PR도 사람 PR과 같은 게이트를 통과하므로 안전하게 위임 가능.

1. **AGENTS.md 절대 규칙 개정**:
   - 유지: force push 금지, main 병합은 사람만, 민감정보 금지, 보안 변경 사람 리뷰
   - 완화: `feat/*` 등 작업 브랜치 push 허용, `gh pr create --draft` 허용
2. **`.claude/settings.json`**: push deny 제거, `gh pr create *` allow (main push는 branch protection이 차단)
3. **`/pr` 커맨드 수정**: draft PR 생성까지 실행 + **에이전트 라벨** 부착 (`agent:claude-code` / `agent:codex`) — 어떤 도구가 만든 변경인지 추적
4. **`/feature` 워크플로우**: plan → impl(테스트 포함) → review → commit → push → draft PR 원스톱

---

## Phase 6 — 컨벤션 고도화 (반복 위반이 보일 때 순차 도입)

1. **AGENTS.md 확장**: JPA 규칙(연관관계 `LAZY` 필수, N+1 방지 fetch join 기준, `Pageable` 사용), 트랜잭션 경계(Controller `@Transactional` 금지), 로깅 규칙(민감정보 로깅 금지)
2. **Spotless**: 포맷 자동화 — 도입 비용 최저, ArchUnit보다 먼저
3. **ArchUnit**: Entity setter 금지, `IllegalArgumentException` 직접 throw 금지, DTO record + 네이밍, Controller 반환 타입 등 구조 규칙을 테스트로 강제
4. **Skills 시범 도입**: `new-domain` 보일러플레이트 skill — `.agents/skills/`(Codex 네이티브) + `.claude/skills/` 심링크
5. **Subagents** (Claude Code 전용, 선택): `code-reviewer`

---

## 검증 체크리스트

- [ ] Claude Code: CLAUDE.md import로 AGENTS.md 인식, `/plan` 등 커맨드 동작
- [ ] Codex: AGENTS.md + 커맨드 파일 읽기 동작
- [ ] main에서 `git commit` → pre-commit 차단 / main으로 push → branch protection 차단
- [ ] 시크릿 포함 커밋 → gitleaks 차단
- [ ] 테스트 없는 PR → patch coverage 게이트 실패
- [ ] 보안 파일 수정 PR → CODEOWNERS 리뷰 요구
- [ ] fresh clone에서 심링크 정상

---

## 실행 순서 요약

| 순서 | 작업 | 소요 | 비고 |
|---|---|---|---|
| 1 | Phase 1 단일소스화 | ~1시간 | 원래 목적, 즉시 |
| 2 | Phase 2 서버 방어선 | ~30분 | 가성비 최대 |
| 3 | Phase 3 로컬 하네스 | ~1시간 | |
| 4 | Phase 4 커버리지 게이트 | ~2시간 | Codecov 연동 포함 |
| 5 | Phase 5 push/PR 위임 | ~1시간 | Phase 2 완료가 조건 |
| 6 | Phase 6 고도화 | 순차 | 아프기 전에, 필요할 때 |

현 규모(소규모 팀) 기준 가성비 구간은 1~4. Phase 6은 컨벤션 위반이 실제 반복될 때 도입해도 늦지 않음.
