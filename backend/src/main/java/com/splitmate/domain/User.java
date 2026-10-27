package com.splitmate.domain;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="app_users") public class User {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,length=100) public String name;
 @Column(nullable=false,unique=true) public String email;
 @Column(name="password_hash",nullable=false) public String passwordHash;
 @Column(name="created_at",nullable=false) public Instant createdAt=Instant.now();
 protected User(){} public User(String name,String email,String passwordHash){this.name=name;this.email=email;this.passwordHash=passwordHash;}
}
