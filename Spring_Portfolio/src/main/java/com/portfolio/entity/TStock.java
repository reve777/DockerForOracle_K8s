package com.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "tstock")
@Getter
@Setter
@ToString(exclude = {"classify", "watches"}) 
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class TStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    @Column(unique = true)
    private String symbol;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "change_price", precision = 10, scale = 2)
    private BigDecimal changePrice;

    @Column(name = "change_in_percent", precision = 10, scale = 2)
    private BigDecimal changeInPercent;

    @Column(name = "pre_closed", precision = 10, scale = 2)
    private BigDecimal preClosed;

    @Column
    private Long volumn; 

    @Column(name = "transaction_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "classify_id")
    @JsonIgnoreProperties("tStocks") 
    private Classify classify;

    @ManyToMany(mappedBy = "tStocks")
    @JsonIgnore // 💡 徹底切斷反向路徑，這能保證 Jackson 絕對不會繞回 Watch
    private Set<Watch> watches = new LinkedHashSet<>();
}