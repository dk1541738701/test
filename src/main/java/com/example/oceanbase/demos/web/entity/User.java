package com.example.oceanbase.demos.web.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "user")
@Data
public class User {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Integer id;
    @Basic
    @Column(name = "username")
    private String username;
    @Basic
    @Column(name = "password")
    private String password;
    @Basic
    @Column(name = "role")
    private String role;
    @Basic
    @Column(name = "department")
    private String department;
    @Basic
    @Column(name = "job")
    private String job;
    @Basic
    @Column(name = "team")
    private String team;
    @Basic
    @Column(name = "name")
    private String name;

}
