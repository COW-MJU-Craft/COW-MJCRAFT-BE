package com.example.cowmjucraft.domain.recruit.dto.admin.response;

import com.example.cowmjucraft.domain.recruit.entity.*;

import java.util.ArrayList;
import java.util.List;

public class AnswerGroupsAdmin {

    private final List<ApplicationDetailAdminResponse.AnswerItem> common;
    private final List<ApplicationDetailAdminResponse.AnswerItem> firstDepartment;
    private final List<ApplicationDetailAdminResponse.AnswerItem> secondDepartment;

    public AnswerGroupsAdmin(Application application, List<Answer> answers) {
        this.common = new ArrayList<>();
        this.firstDepartment = new ArrayList<>();
        this.secondDepartment = new ArrayList<>();

        DepartmentType first = application.getFirstDepartment();
        DepartmentType second = application.getSecondDepartment();

        for (Answer answer : answers) {
            FormQuestion fq = answer.getFormQuestion();

            if (fq.getSectionType() == SectionType.COMMON) {
                common.add(new ApplicationDetailAdminResponse.AnswerItem(fq.getId(), answer.getValue()));
                continue;
            }

            if (fq.getSectionType() == SectionType.DEPARTMENT) {
                DepartmentType dt = fq.getDepartmentType();
                if (dt == first) {
                    firstDepartment.add(new ApplicationDetailAdminResponse.AnswerItem(fq.getId(), answer.getValue()));
                } else if (dt == second) {
                    secondDepartment.add(new ApplicationDetailAdminResponse.AnswerItem(fq.getId(), answer.getValue()));
                }
            }
        }
    }

    public List<ApplicationDetailAdminResponse.AnswerItem> getCommon() {
        return common;
    }

    public List<ApplicationDetailAdminResponse.AnswerItem> getFirstDepartment() {
        return firstDepartment;
    }

    public List<ApplicationDetailAdminResponse.AnswerItem> getSecondDepartment() {
        return secondDepartment;
    }
}
