package com.example.cowmjucraft.domain.recruit.service.client;

import com.example.cowmjucraft.domain.recruit.dto.client.request.ApplicationCreateRequest;
import com.example.cowmjucraft.domain.recruit.dto.client.request.ApplicationReadRequest;
import com.example.cowmjucraft.domain.recruit.dto.client.request.ApplicationUpdateRequest;
import com.example.cowmjucraft.domain.recruit.dto.client.request.ResultReadRequest;
import com.example.cowmjucraft.domain.recruit.dto.client.response.ApplicationCreateResponse;
import com.example.cowmjucraft.domain.recruit.dto.client.response.ApplicationReadResponse;
import com.example.cowmjucraft.domain.recruit.dto.client.response.ApplicationUpdateResponse;
import com.example.cowmjucraft.domain.recruit.dto.client.response.ResultReadResponse;
import com.example.cowmjucraft.domain.recruit.entity.*;
import com.example.cowmjucraft.domain.recruit.repository.*;
import com.example.cowmjucraft.global.cloud.S3PresignFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ApplicationService {

    private final FormRepository formRepository;
    private final ApplicationRepository applicationRepository;
    private final FormQuestionRepository formQuestionRepository;
    private final AnswerRepository answerRepository;
    private final PasswordEncoder passwordEncoder;
    private final QuestionRepository questionRepository;
    private final S3PresignFacade s3PresignFacade;
    private final FormNoticeRepository formNoticeRepository;

    @Transactional
    public ApplicationCreateResponse create(ApplicationCreateRequest request) {

        Form form = formRepository.findFirstByOpenTrue();
        if (form == null || !form.isOpen()) {
            throw conflict("RECRUITMENT_CLOSED");
        }

        if (applicationRepository.existsByFormAndStudentId(form, request.getStudentId())) {
            throw conflict("DUPLICATE_STUDENT_ID");
        }

        if (request.getFirstDepartment() == request.getSecondDepartment()) {
            throw badRequest("FIRST_SECOND_DEPARTMENT_MUST_BE_DIFFERENT");
        }

        DepartmentType firstDepartment = request.getFirstDepartment();
        DepartmentType secondDepartment = request.getSecondDepartment();

        List<FormQuestion> formQuestions = formQuestionRepository.findAllByForm(form);
        if (formQuestions == null || formQuestions.isEmpty()) {
            throw notFound("FORM_QUESTION_NOT_FOUND");
        }

        java.util.Map<Long, FormQuestion> formQuestionMap = new java.util.HashMap<>();
        java.util.Set<Long> requiredCommonIds = new java.util.HashSet<>();
        java.util.Set<Long> requiredDeptIds = new java.util.HashSet<>();

        for (FormQuestion formQuestion : formQuestions) {
            formQuestionMap.put(formQuestion.getId(), formQuestion);

            if (formQuestion.isRequired()) {
                if (formQuestion.getSectionType() == SectionType.COMMON) {
                    requiredCommonIds.add(formQuestion.getId());
                } else if (formQuestion.getSectionType() == SectionType.DEPARTMENT) {
                    if (formQuestion.getDepartmentType() == firstDepartment || formQuestion.getDepartmentType() == secondDepartment) {
                        requiredDeptIds.add(formQuestion.getId());
                    }
                }
            }
        }

        List<ApplicationCreateRequest.AnswerItemRequest> requestAnswers = (request.getAnswers() == null) ? java.util.List.of() : request.getAnswers();

        java.util.Map<Long, String> answerValueMap = new java.util.HashMap<>();

        for (ApplicationCreateRequest.AnswerItemRequest answer : requestAnswers) {
            Long formQuestionId = answer.getFormQuestionId();
            if (formQuestionId == null) {
                throw badRequest("FORM_QUESTION_ID_REQUIRED");
            }

            if (!formQuestionMap.containsKey(formQuestionId)) {
                throw notFound("FORM_QUESTION_NOT_FOUND");
            }

            if (answerValueMap.containsKey(formQuestionId)) {
                throw badRequest("DUPLICATE_ANSWER");
            }

            String value = answer.getValue();
            if (value != null && value.isBlank()) {
                value = null;
            }

            answerValueMap.put(formQuestionId, value);
        }


        for (Long requiredId : requiredCommonIds) {
            String value = answerValueMap.get(requiredId);
            if (value == null) {
                throw badRequest("REQUIRED_ANSWER_MISSING");
            }
        }

        for (Long requiredId : requiredDeptIds) {
            String value = answerValueMap.get(requiredId);
            if (value == null) {
                throw badRequest("REQUIRED_ANSWER_MISSING");
            }
        }

        for (java.util.Map.Entry<Long, String> e : answerValueMap.entrySet()) {
            Long formQuestionId = e.getKey();
            FormQuestion formQuestion = formQuestionMap.get(formQuestionId);

            if (formQuestion.getSectionType() == SectionType.DEPARTMENT) {
                DepartmentType departmentType = formQuestion.getDepartmentType();
                if (departmentType != firstDepartment && departmentType != secondDepartment) {
                    throw badRequest("ANSWER_FOR_UNSELECTED_DEPARTMENT");
                }
            }

            if (formQuestion.isRequired() && e.getValue() == null) {
                throw badRequest("REQUIRED_ANSWER_MISSING");
            }
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());

        Application application = new Application(
                form,
                request.getStudentId(),
                passwordHash,
                firstDepartment,
                secondDepartment
        );
        applicationRepository.save(application);

        for (java.util.Map.Entry<Long, String> e : answerValueMap.entrySet()) {
            String value = e.getValue();
            if (value == null) {
                continue;
            }
            FormQuestion formQuestion = formQuestionMap.get(e.getKey());
            Answer answer = new Answer(application, formQuestion, value);
            answerRepository.save(answer);
        }

        return new ApplicationCreateResponse(application.getId());
    }


    @Transactional(readOnly = true)
    public ApplicationReadResponse read(ApplicationReadRequest request) {

         Form form = formRepository.findFirstByOpenTrue();
        if (form == null) {
            form = formRepository.findTopByOrderByIdDesc();
        }
        if (form == null) {
            throw notFound("FORM_NOT_FOUND");
        }

        Application application = applicationRepository.findByFormAndStudentId(form, request.getStudentId())
                .orElseThrow(() -> notFound("APPLICATION_NOT_FOUND"));

        if (!passwordEncoder.matches(request.getPassword(), application.getPasswordHash())) {
            throw unauthorized("INVALID_CREDENTIALS");
        }

        boolean editable = form.isOpen();

        List<Answer> answers = answerRepository.findAllByApplication(application);

        List<String> fileKeys = answers.stream()
                .filter(a -> a.getFormQuestion().getAnswerType() == AnswerType.FILE)
                .map(Answer::getValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<String, String> urlMap = fileKeys.isEmpty() ? Map.of() : s3PresignFacade.presignGet(fileKeys);

        List<ApplicationReadResponse.AnswerItem> common = new ArrayList<>();
        List<ApplicationReadResponse.AnswerItem> firstDepartment = new ArrayList<>();
        List<ApplicationReadResponse.AnswerItem> secondDepartment = new ArrayList<>();

        for (Answer answer : answers) {
            FormQuestion formQuestion = answer.getFormQuestion();
            String value = answer.getValue();

            if (formQuestion.getAnswerType() == AnswerType.FILE && value != null) {
                value = urlMap.getOrDefault(value, value);
            }

            if (formQuestion.getSectionType() == SectionType.COMMON) {
                common.add(new ApplicationReadResponse.AnswerItem(formQuestion.getId(), value));
                continue;
            }

            if (formQuestion.getSectionType() == SectionType.DEPARTMENT) {
                DepartmentType departmentType = formQuestion.getDepartmentType();

                if (departmentType == application.getFirstDepartment()) {
                    firstDepartment.add(new ApplicationReadResponse.AnswerItem(formQuestion.getId(), answer.getValue()));
                } else if (departmentType == application.getSecondDepartment()) {
                    secondDepartment.add(new ApplicationReadResponse.AnswerItem(formQuestion.getId(), answer.getValue()));
                } else{
                    throw badRequest("INVALID_SECTION_OR_DEPARTMENT_TYPE");
                }
            }
        }

        List<FormNotice> notices = formNoticeRepository.findAllByForm(form);

        ApplicationReadResponse.NoticeItem commonNotice = null;
        ApplicationReadResponse.NoticeItem firstDepartmentNotice = null;
        ApplicationReadResponse.NoticeItem secondDepartmentNotice = null;

        for (FormNotice notice : notices) {
            ApplicationReadResponse.NoticeItem item =
                    new ApplicationReadResponse.NoticeItem(notice.getTitle(), notice.getContent());

            if (notice.getSectionType() == SectionType.COMMON) {
                commonNotice = item;
            }
            else if (notice.getSectionType() == SectionType.DEPARTMENT) {
                if (notice.getDepartmentType() == application.getFirstDepartment()) {
                    firstDepartmentNotice = item;
                } else if (notice.getDepartmentType() == application.getSecondDepartment()) {
                    secondDepartmentNotice = item;
                }
            }
        }

        return new ApplicationReadResponse(
                editable,
                application.getId(),
                application.getStudentId(),
                application.getFirstDepartment(),
                application.getSecondDepartment(),
                application.getCreatedAt(),
                application.getUpdatedAt(),
                commonNotice,
                firstDepartmentNotice,
                secondDepartmentNotice,
                common,
                firstDepartment,
                secondDepartment
        );
    }

    @Transactional
    public ApplicationUpdateResponse update(ApplicationUpdateRequest request) {

        Form form = formRepository.findFirstByOpenTrue();
        if (form == null || !form.isOpen()) {
            throw conflict("RECRUITMENT_CLOSED");
        }

        Application application = applicationRepository.findByFormAndStudentId(form, request.getStudentId())
                .orElseThrow(() -> notFound("APPLICATION_NOT_FOUND"));

        if (!passwordEncoder.matches(request.getPassword(), application.getPasswordHash())) {
            throw unauthorized("INVALID_CREDENTIALS");
        }

        DepartmentType firstDepartment = request.getFirstDepartment();
        DepartmentType secondDepartment = request.getSecondDepartment();

        if (firstDepartment != null || secondDepartment != null) {
            if (firstDepartment == null || secondDepartment == null) {
                throw badRequest("BOTH_DEPARTMENTS_REQUIRED");
            }
            if (firstDepartment == secondDepartment) {
                throw badRequest("FIRST_SECOND_DEPARTMENT_MUST_BE_DIFFERENT");
            }
            application.changeDepartments(firstDepartment, secondDepartment);
        }

        if (request.getAnswers() != null) {

            List<ApplicationUpdateRequest.AnswerItemRequest> reqAnswers = request.getAnswers();

            java.util.Set<Long> seen = new java.util.HashSet<>();
            java.util.List<Long> fqIds = new java.util.ArrayList<>();

            for (ApplicationUpdateRequest.AnswerItemRequest a : reqAnswers) {
                Long id = a.getFormQuestionId();
                if (id == null) {
                    throw badRequest("FORM_QUESTION_ID_REQUIRED");
                }
                if (!seen.add(id)) {
                    throw badRequest("DUPLICATE_ANSWER");
                }
                fqIds.add(id);
            }

            java.util.List<FormQuestion> fetched = formQuestionRepository.findAllByIdInAndForm_Id(fqIds, form.getId());
            if (fetched.size() != fqIds.size()) {
                throw badRequest("FORM_QUESTION_NOT_IN_THIS_FORM");
            }

            java.util.Map<Long, FormQuestion> fqMap = new java.util.HashMap<>();
            for (FormQuestion fq : fetched) {
                fqMap.put(fq.getId(), fq);
            }

            List<Answer> existingAnswers = answerRepository.findAllByApplicationFetchFormQuestion(application);
            java.util.Map<Long, Answer> answerMap = new java.util.HashMap<>();
            for (Answer ans : existingAnswers) {
                answerMap.put(ans.getFormQuestion().getId(), ans);
            }

            DepartmentType currentFirst = application.getFirstDepartment();
            DepartmentType currentSecond = application.getSecondDepartment();

            for (ApplicationUpdateRequest.AnswerItemRequest a : reqAnswers) {

                FormQuestion fq = fqMap.get(a.getFormQuestionId());
                if (fq == null) {
                    throw notFound("FORM_QUESTION_NOT_FOUND");
                }

                if (fq.getSectionType() == SectionType.DEPARTMENT) {
                    DepartmentType dt = fq.getDepartmentType();
                    if (dt != currentFirst && dt != currentSecond) {
                        throw badRequest("ANSWER_FOR_UNSELECTED_DEPARTMENT");
                    }
                }

                String value = a.getValue();
                if (value != null && value.isBlank()) {
                    value = null;
                }

                Answer existing = answerMap.get(fq.getId());

                if (value == null) {
                    if (fq.isRequired()) {
                        throw badRequest("REQUIRED_ANSWER_CANNOT_BE_DELETED");
                    }
                    if (existing != null) {
                        if (fq.getAnswerType() == AnswerType.FILE) {
                            s3PresignFacade.deleteByKeys(List.of(existing.getValue()));
                        }
                        answerRepository.delete(existing);
                    }
                    continue;
                }

                if (existing != null) {
                    if (fq.getAnswerType() == AnswerType.FILE && !existing.getValue().equals(value)) {
                        s3PresignFacade.deleteByKeys(List.of(existing.getValue()));
                    }
                    existing.updateValue(value);
                } else {
                    answerRepository.save(new Answer(application, fq, value));
                }
            }
        }

        return new ApplicationUpdateResponse(application.getId(), application.getUpdatedAt());
    }

    @Transactional(readOnly = true)
    public ResultReadResponse readResult(ResultReadRequest request) {

        Form form = formRepository.findFirstByOpenTrue();
        if (form == null) {
            form = formRepository.findTopByOrderByIdDesc();
        }
        if (form == null) {
            throw notFound("FORM_NOT_FOUND");
        }

        Application application = applicationRepository.findByFormAndStudentId(form, request.getStudentId())
                .orElseThrow(() -> notFound("APPLICATION_NOT_FOUND"));

        if (!passwordEncoder.matches(request.getPassword(), application.getPasswordHash())) {
            throw unauthorized("INVALID_CREDENTIALS");
        }

        return new ResultReadResponse(application.getResultStatus());
    }

    public S3PresignFacade.PresignPutBatchResult createAnswerFilePresignPut(List<S3PresignFacade.PresignPutFile> files) {
        return s3PresignFacade.createPresignPutBatch("uploads/recruit/answers", files);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException unauthorized(String message) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
