package com.example.cowmjucraft.domain.recruit.service.client;

import com.example.cowmjucraft.domain.recruit.dto.client.request.ApplicationUpdateRequest;
import com.example.cowmjucraft.domain.recruit.dto.client.response.ApplicationUpdateResponse;
import com.example.cowmjucraft.domain.recruit.entity.AnswerType;
import com.example.cowmjucraft.domain.recruit.entity.Application;
import com.example.cowmjucraft.domain.recruit.entity.DepartmentType;
import com.example.cowmjucraft.domain.recruit.entity.Form;
import com.example.cowmjucraft.domain.recruit.entity.FormQuestion;
import com.example.cowmjucraft.domain.recruit.entity.Question;
import com.example.cowmjucraft.domain.recruit.entity.SectionType;
import com.example.cowmjucraft.domain.recruit.exception.RecruitException;
import com.example.cowmjucraft.domain.recruit.repository.AnswerRepository;
import com.example.cowmjucraft.domain.recruit.repository.ApplicationRepository;
import com.example.cowmjucraft.domain.recruit.repository.FormNoticeRepository;
import com.example.cowmjucraft.domain.recruit.repository.FormQuestionRepository;
import com.example.cowmjucraft.domain.recruit.repository.FormRepository;
import com.example.cowmjucraft.domain.recruit.repository.QuestionRepository;
import com.example.cowmjucraft.global.cloud.S3PresignFacade;
import com.example.cowmjucraft.global.security.CredentialMatcher;
import com.example.cowmjucraft.global.security.PasswordPolicy;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private FormRepository formRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private FormQuestionRepository formQuestionRepository;
    @Mock
    private AnswerRepository answerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CredentialMatcher credentialMatcher;
    @Mock
    private PasswordPolicy passwordPolicy;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private S3PresignFacade s3PresignFacade;
    @Mock
    private FormNoticeRepository formNoticeRepository;

    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        applicationService = new ApplicationService(
                formRepository,
                applicationRepository,
                formQuestionRepository,
                answerRepository,
                passwordEncoder,
                credentialMatcher,
                passwordPolicy,
                questionRepository,
                s3PresignFacade,
                formNoticeRepository
        );
    }

    @Test
    void update_새답변저장중_UNIQUE충돌_DUPLICATE_ANSWER예외() {
        // given
        Form form = new Form("모집 폼", true);
        ReflectionTestUtils.setField(form, "id", 1L);
        Application application = application(form);
        FormQuestion formQuestion = formQuestion(form, 100L);

        when(formRepository.findById(1L)).thenReturn(Optional.of(form));
        when(applicationRepository.findByFormAndStudentId(form, "60123456")).thenReturn(Optional.of(application));
        when(credentialMatcher.matches("password1234", application.getPasswordHash())).thenReturn(true);
        when(formQuestionRepository.findAllByIdInAndForm_Id(List.of(100L), 1L)).thenReturn(List.of(formQuestion));
        when(answerRepository.findAllByApplicationFetchFormQuestion(application)).thenReturn(List.of());
        when(answerRepository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        ApplicationUpdateRequest request = updateRequest(1L, 100L, "답변 내용");

        // when & then
        assertThatThrownBy(() -> applicationService.update(request))
                .isInstanceOf(RecruitException.class);
    }

    @Test
    void update_새답변정상저장_성공() {
        // given
        Form form = new Form("모집 폼", true);
        ReflectionTestUtils.setField(form, "id", 1L);
        Application application = application(form);
        FormQuestion formQuestion = formQuestion(form, 100L);

        when(formRepository.findById(1L)).thenReturn(Optional.of(form));
        when(applicationRepository.findByFormAndStudentId(form, "60123456")).thenReturn(Optional.of(application));
        when(credentialMatcher.matches("password1234", application.getPasswordHash())).thenReturn(true);
        when(formQuestionRepository.findAllByIdInAndForm_Id(List.of(100L), 1L)).thenReturn(List.of(formQuestion));
        when(answerRepository.findAllByApplicationFetchFormQuestion(application)).thenReturn(List.of());

        ApplicationUpdateRequest request = updateRequest(1L, 100L, "답변 내용");

        // when
        ApplicationUpdateResponse response = applicationService.update(request);

        // then
        assertThat(response.applicationId()).isEqualTo(application.getId());
    }

    private Application application(Form form) {
        Application application = new Application(
                form,
                "60123456",
                "encoded-hash",
                DepartmentType.DESIGN,
                DepartmentType.MARKETING
        );
        ReflectionTestUtils.setField(application, "id", 1L);
        return application;
    }

    private FormQuestion formQuestion(Form form, Long id) {
        Question question = new Question("질문", "설명");
        FormQuestion formQuestion = FormQuestion.builder()
                .form(form)
                .question(question)
                .questionOrder(1)
                .answerType(AnswerType.TEXT)
                .required(false)
                .sectionType(SectionType.COMMON)
                .build();
        ReflectionTestUtils.setField(formQuestion, "id", id);
        return formQuestion;
    }

    private ApplicationUpdateRequest updateRequest(Long formId, Long formQuestionId, String value) {
        ApplicationUpdateRequest request = new ApplicationUpdateRequest();
        ReflectionTestUtils.setField(request, "formId", formId);
        ReflectionTestUtils.setField(request, "studentId", "60123456");
        ReflectionTestUtils.setField(request, "password", "password1234");

        ApplicationUpdateRequest.AnswerItemRequest answer = new ApplicationUpdateRequest.AnswerItemRequest();
        ReflectionTestUtils.setField(answer, "formQuestionId", formQuestionId);
        ReflectionTestUtils.setField(answer, "value", value);
        ReflectionTestUtils.setField(request, "answers", List.of(answer));

        return request;
    }
}
