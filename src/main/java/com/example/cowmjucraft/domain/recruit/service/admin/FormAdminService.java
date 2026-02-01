package com.example.cowmjucraft.domain.recruit.service.admin;

import com.example.cowmjucraft.domain.recruit.dto.admin.*;
import com.example.cowmjucraft.domain.recruit.entity.*;
import com.example.cowmjucraft.domain.recruit.repository.FormQuestionRepository;
import com.example.cowmjucraft.domain.recruit.repository.FormRepository;
import com.example.cowmjucraft.domain.recruit.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


import java.util.ArrayList;
import java.util.List;

@Service
public class FormAdminService {

    private final FormRepository formRepository;
    private final QuestionRepository questionRepository;
    private final FormQuestionRepository formQuestionRepository;

    public FormAdminService(
            FormRepository formRepository,
            QuestionRepository questionRepository,
            FormQuestionRepository formQuestionRepository
    ) {
        this.formRepository = formRepository;
        this.questionRepository = questionRepository;
        this.formQuestionRepository = formQuestionRepository;
    }

    @Transactional
    public FormCreateAdminResponse createForm(FormCreateAdminRequest request) {

        if (request.isOpen()) {
            Form openForm = formRepository.findFirstByOpenTrue();
            if (openForm != null) {
                openForm.close();
                formRepository.save(openForm);
            }
        }

        Form form = new Form(request.getTitle(),request.isOpen());
        formRepository.save(form);

        return new FormCreateAdminResponse(form.getId(), form.isOpen());
    }


    @Transactional
    public void openForm(Long formId) {
        Form target = formRepository.findById(formId)
                .orElseThrow(() -> notFound("FORM_NOT_FOUND"));

        Form openForm = formRepository.findFirstByOpenTrue();
        if (openForm != null && !openForm.getId().equals(target.getId())) {
            openForm.close();
            formRepository.save(openForm);
        }

        target.open();
        formRepository.save(target);
    }

    @Transactional
    public void closeForm(Long formId) {
        Form target = formRepository.findById(formId)
                .orElseThrow(() -> notFound("FORM_NOT_FOUND"));
        target.close();
        formRepository.save(target);
    }

    @Transactional
    public AddQuestionAdminResponse addQuestion(Long formId, AddQuestionAdminRequest request) {

        Form form = formRepository.findById(formId)
                .orElseThrow(() -> notFound("FORM_NOT_FOUND"));

        if (formQuestionRepository.existsByFormAndQuestionOrder(form, request.getQuestionOrder())) {
            throw badRequest("DUPLICATE_QUESTION_ORDER");
        }

        if (request.getSectionType() == SectionType.COMMON && request.getDepartmentType() != null) {
            throw badRequest("COMMON_SECTION_CANNOT_HAVE_DEPARTMENT");
        }

        if (request.getAnswerType() != AnswerType.SELECT && request.getSelectOptions() != null) {
            throw badRequest("SELECT_OPTIONS_ONLY_FOR_SELECT");
        }

        Question q = new Question(request.getLabel(), request.getDescription());
        questionRepository.save(q);

        FormQuestion fq = new FormQuestion(
                form,
                q,
                request.getQuestionOrder(),
                request.getAnswerType(),
                request.isRequired(),
                request.getSectionType(),
                request.getDepartmentType(),
                request.getSelectOptions()
        );
        formQuestionRepository.save(fq);

        return new AddQuestionAdminResponse(q.getId(), fq.getId());
    }

    @Transactional(readOnly = true)
    public List<FormListAdminResponse> listForms() {

        List<Form> forms = formRepository.findAllByOrderByIdDesc();
        List<FormListAdminResponse> result = new ArrayList<>();

        for (Form form : forms) {
            result.add(
                    new FormListAdminResponse(
                            form.getId(),
                            form.getTitle(),
                            form.isOpen()
                    )
            );
        }

        return result;
    }

    @Transactional(readOnly = true)
    public FormDetailAdminResponse getForm(Long formId) {

        Form form = formRepository.findById(formId)
                .orElseThrow(() -> notFound("FORM_NOT_FOUND"));

        return new FormDetailAdminResponse(
                form.getId(),
                form.getTitle(),
                form.isOpen()
        );
    }

    @Transactional(readOnly = true)
    public List<FormQuestionListAdminResponse> listFormQuestions(Long formId) {

        Form form = formRepository.findById(formId)
                .orElseThrow(() -> notFound("FORM_NOT_FOUND"));

        List<FormQuestion> formQuestions =
                formQuestionRepository.findAllByFormOrderByQuestionOrderAsc(form);

        List<FormQuestionListAdminResponse> result = new ArrayList<>();

        for (FormQuestion fq : formQuestions) {
            Question q = fq.getQuestion();

            result.add(new FormQuestionListAdminResponse(
                    fq.getId(),
                    q.getId(),
                    q.getLabel(),
                    q.getDescription(),
                    fq.getQuestionOrder(),
                    fq.isRequired(),
                    fq.getAnswerType(),
                    fq.getSelectOptions(),
                    fq.getSectionType(),
                    fq.getDepartmentType()
            ));
        }

        return result;
    }

    @Transactional
    public void deleteFormQuestion(Long formId, Long formQuestionId) {

        FormQuestion fq = formQuestionRepository.findById(formQuestionId)
                .orElseThrow(() -> notFound("FORM_QUESTION_NOT_FOUND"));

        if (!fq.getForm().getId().equals(formId)) {
            throw notFound("FORM_QUESTION_NOT_IN_THIS_FORM");
        }

        formQuestionRepository.delete(fq);
    }

    @Transactional
    public void updateFormQuestion(Long formId, Long formQuestionId, FormQuestionUpdateAdminRequest request) {

        FormQuestion fq = formQuestionRepository.findById(formQuestionId)
                .orElseThrow(() -> notFound("FORM_QUESTION_NOT_FOUND"));

        if (!fq.getForm().getId().equals(formId)) {
            throw notFound("FORM_QUESTION_NOT_IN_THIS_FORM");
        }

        if (request.getSectionType() == SectionType.COMMON && request.getDepartmentType() != null) {
            throw badRequest("COMMON_SECTION_CANNOT_HAVE_DEPARTMENT");
        }
        if (request.getAnswerType() != AnswerType.SELECT && request.getSelectOptions() != null) {
            throw badRequest("SELECT_OPTIONS_ONLY_FOR_SELECT");
        }

        Question q = fq.getQuestion();
        q.update(request.getLabel(), request.getDescription());

        fq.update(
                request.getQuestionOrder(),
                request.isRequired(),
                request.getAnswerType(),
                request.getSelectOptions(),
                request.getSectionType(),
                request.getDepartmentType()
        );
    }

    @Transactional
    public FormCopyAdminResponse copyFormQuestionsOverwrite(Long targetFormId, FormCopyAdminRequest request) {

        Long sourceFormId = request.getSourceFormId();
        if (sourceFormId == null) {
            throw badRequest("SOURCE_FORM_ID_REQUIRED");
        }
        if (sourceFormId.equals(targetFormId)) {
            throw badRequest("SOURCE_AND_TARGET_CANNOT_BE_SAME");
        }

        Form targetForm = formRepository.findById(targetFormId)
                .orElseThrow(() -> notFound("TARGET_FORM_NOT_FOUND"));

        Form sourceForm = formRepository.findById(sourceFormId)
                .orElseThrow(() -> notFound("SOURCE_FORM_NOT_FOUND"));

        formQuestionRepository.deleteAllByForm(targetForm);

        List<FormQuestion> sourceQuestions =
                formQuestionRepository.findAllByFormOrderByQuestionOrderAsc(sourceForm);

        int copied = 0;

        for (FormQuestion srcFq : sourceQuestions) {
            Question srcQ = srcFq.getQuestion();

            Question newQ = new Question(srcQ.getLabel(), srcQ.getDescription());
            questionRepository.save(newQ);

            FormQuestion newFq = new FormQuestion(
                    targetForm,
                    newQ,
                    srcFq.getQuestionOrder(),
                    srcFq.getAnswerType(),
                    srcFq.isRequired(),
                    srcFq.getSectionType(),
                    srcFq.getDepartmentType(),
                    srcFq.getSelectOptions()
            );
            formQuestionRepository.save(newFq);

            copied++;
        }

        return new FormCopyAdminResponse(targetFormId, sourceFormId, copied);
    }

    private ResponseStatusException badRequest(String reason) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
    }

    private ResponseStatusException unauthorized(String reason) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, reason);
    }

    private ResponseStatusException forbidden(String reason) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, reason);
    }

    private ResponseStatusException notFound(String reason) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, reason);
    }

    private ResponseStatusException conflict(String reason) {
        return new ResponseStatusException(HttpStatus.CONFLICT, reason);
    }

}
