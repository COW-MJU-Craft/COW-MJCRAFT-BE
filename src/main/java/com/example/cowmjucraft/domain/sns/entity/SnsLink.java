package com.example.cowmjucraft.domain.sns.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "sns_links",
        uniqueConstraints = @UniqueConstraint(name = "uk_sns_links_type", columnNames = "type")
)
public class SnsLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SnsType type;

    @Column(nullable = false)
    private String url;

    public SnsLink(SnsType type, String url) {
        this.type = type;
        this.url = url;
    }

    public void updateUrl(String url) {
        this.url = url;
    }
}
