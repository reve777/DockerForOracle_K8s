package com.portfolio.entity;

import java.util.Set;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
    @JsonIgnore 
    @ToString.Exclude
    @EqualsAndHashCode.Exclude // 💡 避免循環計算 hashCode
    private Set<Watch> watchs;

    @OneToMany(mappedBy = "investor", fetch = FetchType.EAGER) // 💡 必須為 EAGER
    @JsonIgnoreProperties("investor") 
    @ToString.Exclude
    @EqualsAndHashCode.Exclude // 💡 關鍵：Set 必須排除此欄位的雜湊計算
    private Set<Portfolio> portfolios; 
}