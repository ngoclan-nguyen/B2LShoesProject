package com.example.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "master_size")
public class MasterSize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "size_name", nullable = false)
    private String sizeName;

    @Column(name = "type")
    private String type; // 'SHOES' or 'SOCK'

    @Column(name = "eu_size")
    private BigDecimal euSize;
    
    @Column(name = "uk_size")
    private BigDecimal ukSize;
    
    @Column(name = "us_men_size")
    private BigDecimal usMenSize;
    
    @Column(name = "us_women_size")
    private BigDecimal usWomenSize;
    
    @Column(name = "heel_toe_cm")
    private BigDecimal heelToeCm;

    public MasterSize() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getSizeName() { return sizeName; }
    public void setSizeName(String sizeName) { this.sizeName = sizeName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getEuSize() { return euSize; }
    public void setEuSize(BigDecimal euSize) { this.euSize = euSize; }
    
    public BigDecimal getUkSize() { return ukSize; }
    public void setUkSize(BigDecimal ukSize) { this.ukSize = ukSize; }
    
    public BigDecimal getUsMenSize() { return usMenSize; }
    public void setUsMenSize(BigDecimal usMenSize) { this.usMenSize = usMenSize; }
    
    public BigDecimal getUsWomenSize() { return usWomenSize; }
    public void setusWomenSize(BigDecimal usWomenSize) { this.usWomenSize = usWomenSize; }
    
    public BigDecimal getHeelToeCm() { return heelToeCm; };
    public void setHeelToeCm(BigDecimal heelToeCm) { this.heelToeCm = heelToeCm; }
    
}