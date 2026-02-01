package com.example.cowmjucraft.domain.recruit.service.admin;

import com.example.cowmjucraft.domain.recruit.dto.admin.ApplicationDetailAdminResponse;
import com.example.cowmjucraft.domain.recruit.dto.admin.ApplicationListAdminResponse;
import com.example.cowmjucraft.domain.recruit.dto.admin.ApplicationResultUpdateAdminRequest;
import com.example.cowmjucraft.domain.recruit.entity.*;
import com.example.cowmjucraft.domain.recruit.repository.AnswerRepository;
import com.example.cowmjucraft.domain.recruit.repository.ApplicationRepository;
import com.example.cowmjucraft.domain.recruit.repository.FormRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


import java.util.ArrayList;
import java.util.List;

@Service
public class ApplicationAdminService {

    private final FormRepository formRepository;
    private final ApplicationRepository applicationRepository;
    private final AnswerRepository answerRepository;

    public ApplicationAdminService(
            FormRepository formRepository,
            ApplicationRepository applicationRepository,
            AnswerRepository answerRepository
    ) {
        this.formRepository = formRepository;
        this.applicationRepository = applicationRepository;
        this.answerRepository = answerRepository;
    }



    @Transactional(readOnly = true)
    public List<ApplicationListAdminResponse> listApplications(Long formId) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> notFound("FORM_NOT_FOUND"));

        List<Application> apps = applicationRepository.findAllByForm(form);
        List<ApplicationListAdminResponse> result = new ArrayList<>();

        for (Application a : apps) {
            result.add(new ApplicationListAdminResponse(
                    a.getId(),
                    a.getStudentId(),
                    a.getFirstDepartment(),
                    a.getSecondDepartment(),
                    a.getResultStatus(),
                    a.getSubmittedAt(),
                    a.getUpdatedAt()
            ));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public ApplicationDetailAdminResponse getApplication(Long formId, Long applicationId) {

        Form form = formRepository.findById(formId)
                .orElseThrow(() -> notFound("FORM_NOT_FOUND"));

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> notFound("APPLICATION_NOT_FOUND"));

        if (!application.getForm().getId().equals(form.getId())) {
            throw badRequest("APPLICATION_NOT_IN_THIS_FORM");
        }

        List<Answer> answers = answerRepository.findAllByApplication(application);

        List<ApplicationDetailAdminResponse.AnswerItem> common = new ArrayList<>();
        List<ApplicationDetailAdminResponse.AnswerItem> first = new ArrayList<>();
        List<ApplicationDetailAdminResponse.AnswerItem> second = new ArrayList<>();

        for (Answer a : answers) {
            FormQuestion fq = a.getFormQuestion();

            if (fq.getSectionType() == SectionType.COMMON) {
                common.add(new ApplicationDetailAdminResponse.AnswerItem(fq.getId(), a.getValue()));
                continue;
            }

            if (fq.getSectionType() == SectionType.DEPARTMENT) {
                DepartmentType dept = fq.getDepartmentType();
                if (dept == application.getFirstDepartment()) {
                    first.add(new ApplicationDetailAdminResponse.AnswerItem(fq.getId(), a.getValue()));
                } else if (dept == application.getSecondDepartment()) {
                    second.add(new ApplicationDetailAdminResponse.AnswerItem(fq.getId(), a.getValue()));
                }
            }
        }

        return new ApplicationDetailAdminResponse(
                application.getId(),
                application.getStudentId(),
                application.getFirstDepartment(),
                application.getSecondDepartment(),
                application.getResultStatus(),
                application.getSubmittedAt(),
                application.getUpdatedAt(),
                common,
                first,
                second
        );
    }

    @Transactional
    public void deleteApplication(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> notFound("APPLICATION_NOT_FOUND"));

        answerRepository.deleteAllByApplication(application);
        applicationRepository.delete(application);
    }

    @Transactional
    public void updateResult(Long applicationId, ApplicationResultUpdateAdminRequest request) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> notFound("APPLICATION_NOT_FOUND"));

        application.setResultStatus(request.getResultStatus());
    }

    private ResponseStatusException badRequest(String reason) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
    }

    private ResponseStatusException notFound(String reason) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, reason);
    }

}

