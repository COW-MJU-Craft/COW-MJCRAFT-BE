package com.example.cowmjucraft.domain.sns.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "sns_links")
public class SnsLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SnsType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean active;

    public SnsLink(SnsType type, String title, String url, int sortOrder, boolean active) {
        this.type = type;
        this.title = title;
        this.url = url;
        this.sortOrder = sortOrder;
        this.active = active;
    }
}
