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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ApplicationService {

    private final FormRepository formRepository;
    private final ApplicationRepository applicationRepository;
    private final FormQuestionRepository formQuestionRepository;
    private final AnswerRepository answerRepository;
    private final PasswordEncoder passwordEncoder;
    private final QuestionRepository questionRepository;

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

        String passwordHash = passwordEncoder.encode(request.getPassword());

        Application application = new Application(
                form,
                request.getStudentId(),
                passwordHash,
                request.getFirstDepartment(),
                request.getSecondDepartment()
        );
        applicationRepository.save(application);

        if (request.getAnswers() != null) {
            for (ApplicationCreateRequest.AnswerItemRequest a : request.getAnswers()) {
                FormQuestion formQuestion = formQuestionRepository.findById(a.getFormQuestionId())
                        .orElseThrow(() -> notFound("FORM_QUESTION_NOT_FOUND"));

                if (!formQuestion.getForm().getId().equals(form.getId())) {
                    throw badRequest("FORM_QUESTION_NOT_IN_THIS_FORM");
                }

                Answer answer = new Answer(application, formQuestion, a.getValue());
                answerRepository.save(answer);
            }
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

        List<ApplicationReadResponse.AnswerItem> common = new ArrayList<>();
        List<ApplicationReadResponse.AnswerItem> firstDepartment = new ArrayList<>();
        List<ApplicationReadResponse.AnswerItem> secondDepartment = new ArrayList<>();

        for (Answer answer : answers) {
            FormQuestion formQuestion = answer.getFormQuestion();

            if (formQuestion.getSectionType() == SectionType.COMMON) {
                common.add(new ApplicationReadResponse.AnswerItem(formQuestion.getId(), answer.getValue()));
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

        return new ApplicationReadResponse(
                editable,
                application.getId(),
                application.getStudentId(),
                application.getFirstDepartment(),
                application.getSecondDepartment(),
                application.getCreatedAt(),
                application.getUpdatedAt(),
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

            List<Answer> existingAnswers = answerRepository.findAllByApplication(application);
            java.util.Map<Long, Answer> answerMap = new java.util.HashMap<>();
            for (Answer answer : existingAnswers) {
                answerMap.put(answer.getFormQuestion().getId(), answer);
            }

            for (ApplicationUpdateRequest.AnswerItemRequest answer : request.getAnswers()) {

                FormQuestion formQuestion = formQuestionRepository.findById(answer.getFormQuestionId())
                        .orElseThrow(() -> notFound("FORM_QUESTION_NOT_FOUND"));

                if (!formQuestion.getForm().getId().equals(form.getId())) {
                    throw badRequest("FORM_QUESTION_NOT_IN_THIS_FORM");
                }

                Answer existingAnswer = answerMap.get(formQuestion.getId());

                if (answer.getValue() == null) {
                    if (existingAnswer != null) {
                        answerRepository.delete(existingAnswer);
                    }
                    continue;
                }

                if (existingAnswer != null) {
                    existingAnswer.updateValue(answer.getValue());
                } else {
                    Answer newAnswer = new Answer(application, formQuestion, answer.getValue());
                    answerRepository.save(newAnswer);
                }
            }
        }

        applicationRepository.save(application);

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
