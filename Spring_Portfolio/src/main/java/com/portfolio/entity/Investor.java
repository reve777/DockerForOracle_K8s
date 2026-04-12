package com.portfolio.entity;

import java.util.Set;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "investor")
@Data
public class Investor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String username;

    @Column
    private String email;

    @Column
    private Long balance;

    @OneToMany(mappedBy = "investor", fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    @JsonIgnore // 💡 終極方案：直接忽略，不讓 Jackson 進入循環
    private Set<Watch> watchs;

    @OneToMany(mappedBy = "investor")
    @JsonIgnore // 💡 終極方案：直接忽略
    @ToString.Exclude
    private Set<Portfolio> portfolios;
}