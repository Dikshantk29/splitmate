package com.splitmate.domain;
import jakarta.persistence.*; import java.time.Instant;
@Entity @Table(name="households") public class Household { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; @Column(nullable=false,length=120) public String name; @Column(name="created_by",nullable=false) public Long createdBy; @Column(name="created_at",nullable=false) public Instant createdAt=Instant.now(); protected Household(){} public Household(String name,Long createdBy){this.name=name;this.createdBy=createdBy;} }
