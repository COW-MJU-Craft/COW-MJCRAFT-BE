package com.example.cowmjucraft.concurrency;

import com.example.cowmjucraft.domain.item.dto.request.AdminItemImageOrderPatchRequestDto;
import com.example.cowmjucraft.domain.item.entity.ItemImage;
import com.example.cowmjucraft.domain.item.entity.ItemSaleType;
import com.example.cowmjucraft.domain.item.entity.ItemStatus;
import com.example.cowmjucraft.domain.item.entity.ItemType;
import com.example.cowmjucraft.domain.item.entity.ProjectItem;
import com.example.cowmjucraft.domain.item.repository.ItemImageRepository;
import com.example.cowmjucraft.domain.item.repository.ProjectItemRepository;
import com.example.cowmjucraft.domain.item.service.AdminItemService;
import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.entity.ProjectCategory;
import com.example.cowmjucraft.domain.project.entity.ProjectStatus;
import com.example.cowmjucraft.domain.project.repository.ProjectRepository;
import com.example.cowmjucraft.domain.recruit.dto.client.request.ApplicationUpdateRequest;
import com.example.cowmjucraft.domain.recruit.entity.Answer;
import com.example.cowmjucraft.domain.recruit.entity.AnswerType;
import com.example.cowmjucraft.domain.recruit.entity.Application;
import com.example.cowmjucraft.domain.recruit.entity.DepartmentType;
import com.example.cowmjucraft.domain.recruit.entity.Form;
import com.example.cowmjucraft.domain.recruit.entity.FormQuestion;
import com.example.cowmjucraft.domain.recruit.entity.Question;
import com.example.cowmjucraft.domain.recruit.entity.RecruitSettings;
import com.example.cowmjucraft.domain.recruit.entity.SectionType;
import com.example.cowmjucraft.domain.recruit.exception.RecruitErrorType;
import com.example.cowmjucraft.domain.recruit.exception.RecruitException;
import com.example.cowmjucraft.domain.recruit.repository.AnswerRepository;
import com.example.cowmjucraft.domain.recruit.repository.ApplicationRepository;
import com.example.cowmjucraft.domain.recruit.repository.FormQuestionRepository;
import com.example.cowmjucraft.domain.recruit.repository.FormRepository;
import com.example.cowmjucraft.domain.recruit.repository.QuestionRepository;
import com.example.cowmjucraft.domain.recruit.repository.RecruitSettingsRepository;
import com.example.cowmjucraft.domain.recruit.service.admin.FormAdminService;
import com.example.cowmjucraft.domain.recruit.service.client.ApplicationService;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * 이슈 #135 — 실제 MySQL(UNIQUE 제약, 비관적 락)에서만 검증 가능한 동시성 시나리오.
 * Mockito 단위 테스트는 제약 위반 타이밍이나 락 대기를 재현할 수 없어 별도 Testcontainers로 검증한다.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class UniqueConstraintsConcurrencyTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");

    @DynamicPropertySource
    static void overrideDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQLDialect");
    }

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectItemRepository projectItemRepository;
    @Autowired
    private ItemImageRepository itemImageRepository;
    @Autowired
    private AdminItemService adminItemService;

    @Autowired
    private FormRepository formRepository;
    @Autowired
    private RecruitSettingsRepository recruitSettingsRepository;
    @Autowired
    private FormAdminService formAdminService;

    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private FormQuestionRepository formQuestionRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private AnswerRepository answerRepository;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void item_images_스왑재배치가_UNIQUE제약충돌없이_성공한다() {
        // given
        Project project = projectRepository.save(new Project(
                "프로젝트", "요약", "설명", "thumb.png", List.of(),
                LocalDate.now().plusDays(7), ProjectStatus.OPEN, ProjectCategory.GOODS
        ));
        ProjectItem item = projectItemRepository.save(new ProjectItem(
                project, "상품", "요약", "설명", 1000,
                ItemSaleType.NORMAL, ItemStatus.OPEN, ItemType.PHYSICAL, "thumb.png",
                null, null, null, 10
        ));

        ItemImage image1 = itemImageRepository.save(new ItemImage(item, "key1", 0));
        ItemImage image2 = itemImageRepository.save(new ItemImage(item, "key2", 1));
        ItemImage image3 = itemImageRepository.save(new ItemImage(item, "key3", 2));

        // 완전한 회전(rotate) 재배치: 중간 상태에서 (item_id, sort_order) 충돌이 발생할 수 있는 케이스
        AdminItemImageOrderPatchRequestDto request = new AdminItemImageOrderPatchRequestDto(
                List.of(image3.getId(), image1.getId(), image2.getId())
        );

        // when & then
        assertThatCode(() -> adminItemService.patchImageOrder(item.getId(), request))
                .doesNotThrowAnyException();

        List<ItemImage> reordered = itemImageRepository.findByItemIdOrderBySortOrderAsc(item.getId());
        assertThat(reordered).extracting(ItemImage::getId)
                .containsExactly(image3.getId(), image1.getId(), image2.getId());
    }

    @Test
    void openForm_두관리자가_동시에_서로다른폼을열어도_정확히하나만_열린다() throws Exception {
        // given
        Form formA = formRepository.save(new Form("폼 A", false));
        Form formB = formRepository.save(new Form("폼 B", false));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Void> openA = raceTask(ready, start, () -> formAdminService.openForm(formA.getId()));
        Callable<Void> openB = raceTask(ready, start, () -> formAdminService.openForm(formB.getId()));

        try {
            Future<Void> futureA = executor.submit(openA);
            Future<Void> futureB = executor.submit(openB);

            ready.await();
            start.countDown();

            futureA.get(10, TimeUnit.SECONDS);
            futureB.get(10, TimeUnit.SECONDS);
        } finally {
            executor.shutdown();
        }

        // then
        List<Form> openForms = formRepository.findAllByOrderByIdDesc().stream()
                .filter(Form::isOpen)
                .toList();
        assertThat(openForms).hasSize(1);

        RecruitSettings settings = recruitSettingsRepository.findById(1L).orElseThrow();
        assertThat(settings.getActiveFormId()).isEqualTo(openForms.get(0).getId());
    }

    @Test
    void 동시_신규답변저장_중_하나만_성공하고_나머지는_DUPLICATE_ANSWER이다() throws Exception {
        // given
        Form form = formRepository.save(new Form("모집 폼", true));
        Question question = questionRepository.save(new Question("질문", "설명"));
        FormQuestion formQuestion = formQuestionRepository.save(FormQuestion.builder()
                .form(form)
                .question(question)
                .questionOrder(1)
                .answerType(AnswerType.TEXT)
                .required(false)
                .sectionType(SectionType.COMMON)
                .build());

        String rawPassword = "password1234";
        Application application = applicationRepository.save(new Application(
                form, "60123456", passwordEncoder.encode(rawPassword),
                DepartmentType.DESIGN, DepartmentType.MARKETING
        ));

        ApplicationUpdateRequest request = updateRequest(
                form.getId(), "60123456", rawPassword, formQuestion.getId(), "답변 내용"
        );

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger duplicateCount = new AtomicInteger();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Void> task = raceTask(ready, start, () -> {
            try {
                applicationService.update(request);
                successCount.incrementAndGet();
            } catch (RecruitException e) {
                if (e.getErrorCode() == RecruitErrorType.DUPLICATE_ANSWER) {
                    duplicateCount.incrementAndGet();
                } else {
                    throw e;
                }
            }
        });

        try {
            Future<Void> future1 = executor.submit(task);
            Future<Void> future2 = executor.submit(task);

            ready.await();
            start.countDown();

            future1.get(10, TimeUnit.SECONDS);
            future2.get(10, TimeUnit.SECONDS);
        } finally {
            executor.shutdown();
        }

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(duplicateCount.get()).isEqualTo(1);
        assertThat(answerRepository.findAllByApplication(application)).hasSize(1);
    }

    private interface RaceAction {
        void run() throws Exception;
    }

    private Callable<Void> raceTask(CountDownLatch ready, CountDownLatch start, RaceAction action) {
        return () -> {
            ready.countDown();
            start.await();
            action.run();
            return null;
        };
    }

    private ApplicationUpdateRequest updateRequest(
            Long formId, String studentId, String password, Long formQuestionId, String value
    ) {
        ApplicationUpdateRequest request = new ApplicationUpdateRequest();
        ReflectionTestUtils.setField(request, "formId", formId);
        ReflectionTestUtils.setField(request, "studentId", studentId);
        ReflectionTestUtils.setField(request, "password", password);

        ApplicationUpdateRequest.AnswerItemRequest answer = new ApplicationUpdateRequest.AnswerItemRequest();
        ReflectionTestUtils.setField(answer, "formQuestionId", formQuestionId);
        ReflectionTestUtils.setField(answer, "value", value);
        ReflectionTestUtils.setField(request, "answers", List.of(answer));

        return request;
    }
}
