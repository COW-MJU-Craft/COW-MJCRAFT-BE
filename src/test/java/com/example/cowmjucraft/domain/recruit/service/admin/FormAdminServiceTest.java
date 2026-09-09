package com.example.cowmjucraft.domain.recruit.service.admin;

import com.example.cowmjucraft.domain.recruit.dto.admin.request.FormQuestionUpdateAdminRequest;
import com.example.cowmjucraft.domain.recruit.entity.AnswerType;
import com.example.cowmjucraft.domain.recruit.entity.Form;
import com.example.cowmjucraft.domain.recruit.entity.FormQuestion;
import com.example.cowmjucraft.domain.recruit.entity.Question;
import com.example.cowmjucraft.domain.recruit.entity.RecruitSettings;
import com.example.cowmjucraft.domain.recruit.entity.SectionType;
import com.example.cowmjucraft.domain.recruit.exception.RecruitException;
import com.example.cowmjucraft.domain.recruit.repository.ApplicationRepository;
import com.example.cowmjucraft.domain.recruit.repository.FormNoticeRepository;
import com.example.cowmjucraft.domain.recruit.repository.FormQuestionRepository;
import com.example.cowmjucraft.domain.recruit.repository.FormRepository;
import com.example.cowmjucraft.domain.recruit.repository.QuestionRepository;
import com.example.cowmjucraft.domain.recruit.repository.RecruitSettingsRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormAdminServiceTest {

    @Mock
    private FormRepository formRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private FormQuestionRepository formQuestionRepository;
    @Mock
    private FormNoticeRepository formNoticeRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private RecruitSettingsRepository recruitSettingsRepository;

    private FormAdminService formAdminService;

    @BeforeEach
    void setUp() {
        formAdminService = new FormAdminService(
                formRepository,
                questionRepository,
                formQuestionRepository,
                formNoticeRepository,
                applicationRepository,
                recruitSettingsRepository
        );
    }

    @Test
    void updateFormQuestion_순서중복_DUPLICATE_QUESTION_ORDER예외() {
        // given
        Form form = new Form("모집 폼", true);
        ReflectionTestUtils.setField(form, "id", 1L);
        FormQuestion formQuestion = formQuestion(form, 10L, 1);

        when(formQuestionRepository.findById(10L)).thenReturn(Optional.of(formQuestion));
        when(formQuestionRepository.existsByFormAndQuestionOrderAndIdNot(form, 2, 10L)).thenReturn(true);

        FormQuestionUpdateAdminRequest request = updateRequest(2);

        // when & then
        assertThatThrownBy(() -> formAdminService.updateFormQuestion(1L, 10L, request))
                .isInstanceOf(RecruitException.class);
    }

    @Test
    void updateFormQuestion_순서변경없음_정상수정() {
        // given
        Form form = new Form("모집 폼", true);
        ReflectionTestUtils.setField(form, "id", 1L);
        FormQuestion formQuestion = formQuestion(form, 10L, 1);

        when(formQuestionRepository.findById(10L)).thenReturn(Optional.of(formQuestion));
        when(formQuestionRepository.existsByFormAndQuestionOrderAndIdNot(form, 1, 10L)).thenReturn(false);

        FormQuestionUpdateAdminRequest request = updateRequest(1);

        // when
        formAdminService.updateFormQuestion(1L, 10L, request);

        // then
        assertThat(formQuestion.getQuestionOrder()).isEqualTo(1);
    }

    @Test
    void openForm_다른폼이열려있으면_닫고_대상폼을연다() {
        // given
        Form openForm = new Form("기존 열린 폼", true);
        ReflectionTestUtils.setField(openForm, "id", 1L);
        Form target = new Form("새로 열 폼", false);
        ReflectionTestUtils.setField(target, "id", 2L);

        RecruitSettings settings = new RecruitSettings(1L, null);

        when(formRepository.findById(2L)).thenReturn(Optional.of(target));
        when(recruitSettingsRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(settings));
        when(formRepository.findFirstByOpenTrue()).thenReturn(openForm);

        // when
        formAdminService.openForm(2L);

        // then
        assertThat(openForm.isOpen()).isFalse();
        assertThat(target.isOpen()).isTrue();
        assertThat(settings.getActiveFormId()).isEqualTo(2L);
    }

    @Test
    void openForm_설정행없음_RecruitException발생() {
        // given
        when(recruitSettingsRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> formAdminService.openForm(2L))
                .isInstanceOf(RecruitException.class);
    }

    private FormQuestionUpdateAdminRequest updateRequest(int questionOrder) {
        FormQuestionUpdateAdminRequest request = new FormQuestionUpdateAdminRequest();
        ReflectionTestUtils.setField(request, "label", "질문");
        ReflectionTestUtils.setField(request, "description", "설명");
        ReflectionTestUtils.setField(request, "questionOrder", questionOrder);
        ReflectionTestUtils.setField(request, "required", true);
        ReflectionTestUtils.setField(request, "answerType", AnswerType.TEXT);
        ReflectionTestUtils.setField(request, "sectionType", SectionType.COMMON);
        return request;
    }

    private FormQuestion formQuestion(Form form, Long id, int questionOrder) {
        Question question = new Question("질문", "설명");
        FormQuestion formQuestion = FormQuestion.builder()
                .form(form)
                .question(question)
                .questionOrder(questionOrder)
                .answerType(AnswerType.TEXT)
                .required(true)
                .sectionType(SectionType.COMMON)
                .build();
        ReflectionTestUtils.setField(formQuestion, "id", id);
        return formQuestion;
    }
}
