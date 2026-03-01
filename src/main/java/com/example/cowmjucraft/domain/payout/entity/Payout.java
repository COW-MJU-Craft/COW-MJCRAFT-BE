package com.example.cowmjucraft.domain.payout.entity;

import com.example.cowmjucraft.domain.project.entity.Project;
import com.example.cowmjucraft.domain.project.entity.ProjectStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payouts")
@Getter
@NoArgsConstructor
public class Payout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 20)
    private String semester;

    @Column(nullable = false)
    private long totalIncome;

    @Column(nullable = false)
    private long totalExpense;

    @Column(nullable = false)
    private long netProfit;

    @Column(nullable = false)
    private double profitRate;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    private Project project;

    @OneToMany(mappedBy = "payout", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PayoutItem> items = new ArrayList<>();

    public Payout(String title, String semester, Project project) {
        this.title = title;
        this.semester = semester;
        this.totalIncome = 0L;
        this.totalExpense = 0L;
        this.netProfit = 0L;
        this.profitRate = 0.0;
        this.project = project;
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeSemester(String semester) {
        this.semester = semester;
    }

    public void changeProject(Project project) {
        this.project = project;
    }

    public void addItem(PayoutItem item) {
        this.items.add(item);
        item.attach(this);
    }

    public void removeItem(PayoutItem item) {
        this.items.remove(item);
        item.detachPayout();
    }

    public void calculateSummary() {
        long income = 0L;
        long expense = 0L;

        for (PayoutItem item : items) {
            if (item.getType() == PayoutItemType.INCOME) {
                income += item.getAmount();
            } else {
                expense += item.getAmount();
            }
        }

        this.totalIncome = income;
        this.totalExpense = expense;
        this.netProfit = income - expense;

        if (income <= 0) {
            this.profitRate = 0.0;
        } else {
            this.profitRate = (this.netProfit * 100.0) / income;
        }
    }
}
