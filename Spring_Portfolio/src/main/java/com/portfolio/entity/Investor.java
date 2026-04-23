package com.portfolio.entity;

import java.util.Set;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "investor")
@Getter
@Setter
@ToString(exclude = {"watches", "portfolios"})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
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
    @JsonIgnoreProperties("investor") // 💡 確保轉 JSON 時不會回頭抓 investor
    private Set<Watch> watches;

    @OneToMany(mappedBy = "investor", fetch = FetchType.EAGER)
    @JsonIgnoreProperties("investor") // 💡 確保轉 JSON 時不會回頭抓 investor
    private Set<Portfolio> portfolios; 
}