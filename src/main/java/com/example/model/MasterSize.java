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

    public MasterSize() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getSizeName() { return sizeName; }
    public void setSizeName(String sizeName) { this.sizeName = sizeName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getEuSize() { return euSize; }
    public void setEuSize(BigDecimal euSize) { this.euSize = euSize; }
}