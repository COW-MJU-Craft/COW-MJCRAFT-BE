package com.example.cowmjucraft.domain.recruit.service.user;

import com.example.cowmjucraft.domain.recruit.dto.User.*;
import com.example.cowmjucraft.domain.recruit.entity.*;
import com.example.cowmjucraft.domain.recruit.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApplicationService {

    private final FormRepository formRepository;
    private final ApplicationRepository applicationRepository;
    private final FormQuestionRepository formQuestionRepository;
    private final AnswerRepository answerRepository;
    private final PasswordEncoder passwordEncoder;
    private final QuestionRepository questionRepository;

    public ApplicationService(
            FormRepository formRepository,
            ApplicationRepository applicationRepository,
            FormQuestionRepository formQuestionRepository,
            AnswerRepository answerRepository,
            PasswordEncoder passwordEncoder,
            QuestionRepository questionRepository
    ) {
        this.formRepository = formRepository;
        this.applicationRepository = applicationRepository;
        this.formQuestionRepository = formQuestionRepository;
        this.answerRepository = answerRepository;
        this.passwordEncoder = passwordEncoder;
        this.questionRepository = questionRepository;
    }

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
                FormQuestion fq = formQuestionRepository.findById(a.getFormQuestionId())
                        .orElseThrow(() -> notFound("FORM_QUESTION_NOT_FOUND"));

                if (!fq.getForm().getId().equals(form.getId())) {
                    throw badRequest("FORM_QUESTION_NOT_IN_THIS_FORM");
                }

                Answer answer = new Answer(application, fq, a.getValue());
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
        List<ApplicationReadResponse.AnswerItem> firstDept = new ArrayList<>();
        List<ApplicationReadResponse.AnswerItem> secondDept = new ArrayList<>();

        for (Answer a : answers) {
            FormQuestion fq = a.getFormQuestion();

            if (fq.getSectionType() == SectionType.COMMON) {
                common.add(new ApplicationReadResponse.AnswerItem(fq.getId(), a.getValue()));
                continue;
            }

            if (fq.getSectionType() == SectionType.DEPARTMENT) {
                DepartmentType dept = fq.getDepartmentType();

                if (dept == application.getFirstDepartment()) {
                    firstDept.add(new ApplicationReadResponse.AnswerItem(fq.getId(), a.getValue()));
                } else if (dept == application.getSecondDepartment()) {
                    secondDept.add(new ApplicationReadResponse.AnswerItem(fq.getId(), a.getValue()));
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
                application.getSubmittedAt(),
                application.getUpdatedAt(),
                common,
                firstDept,
                secondDept
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

        DepartmentType first = request.getFirstDepartment();
        DepartmentType second = request.getSecondDepartment();

        if (first != null || second != null) {
            if (first == null || second == null) {
                throw badRequest("BOTH_DEPARTMENTS_REQUIRED");
            }
            if (first == second) {
                throw badRequest("FIRST_SECOND_DEPARTMENT_MUST_BE_DIFFERENT");
            }
            application.changeDepartments(first, second);
        }

        if (request.getAnswers() != null) {

            List<Answer> existingAnswers = answerRepository.findAllByApplication(application);
            java.util.Map<Long, Answer> answerMap = new java.util.HashMap<>();
            for (Answer ans : existingAnswers) {
                answerMap.put(ans.getFormQuestion().getId(), ans);
            }

            for (ApplicationUpdateRequest.AnswerItemRequest a : request.getAnswers()) {

                FormQuestion fq = formQuestionRepository.findById(a.getFormQuestionId())
                        .orElseThrow(() -> notFound("FORM_QUESTION_NOT_FOUND"));

                if (!fq.getForm().getId().equals(form.getId())) {
                    throw badRequest("FORM_QUESTION_NOT_IN_THIS_FORM");
                }

                Answer existing = answerMap.get(fq.getId());

                if (a.getValue() == null) {
                    if (existing != null) {
                        answerRepository.delete(existing);
                    }
                    continue;
                }

                if (existing != null) {
                    existing.updateValue(a.getValue());
                } else {
                    Answer newAnswer = new Answer(application, fq, a.getValue());
                    answerRepository.save(newAnswer);
                }
            }
        }

        application.markUpdated();
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
