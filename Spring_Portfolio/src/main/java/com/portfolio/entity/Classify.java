package com.portfolio.entity;

import java.util.Set;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "classify")
@Getter
@Setter
@ToString(exclude = "tStocks")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id") // 關鍵：避免計算 hashCode 時觸發集合
public class Classify {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    @Column
    private Boolean tx;

    @OneToMany(mappedBy = "classify")
    @JsonIgnoreProperties("classify")
    private Set<TStock> tStocks;
}