package com.example.oceanbase.demos.web.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "leave_record")
@Data
public class LeaveRecordEntity {

    @Id
    @Column(name = "id")
    private Integer recordId;
    @Basic
    @Column(name = "user_id")
    private Integer userId;
    @Basic
    @Column(name = "start_time")
    private String startTime;
    @Basic
    @Column(name = "end_time")
    private String endTime;
    @Basic
    @Column(name = "reason")
    private String reason;
    @Basic
    @Column(name = "type")
    private String type;
    @Basic
    @Column(name = "response")
    private String response;
    @Basic
    @Column(name = "decision")
    private String decision;
    @Basic
    @Column(name = "auditor")
    private String auditor;
    @Basic
    @Column(name = "or_depart")
    private String orDepart;
    @Basic
    @Column(name = "state")
    private Integer state;
    @Basic
    @Column(name = "update_time")
    private String updateTime;
    @Basic
    @Column(name = "apply_time")
    private String applyTime;

}
