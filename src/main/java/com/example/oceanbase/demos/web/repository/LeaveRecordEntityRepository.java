package com.example.oceanbase.demos.web.repository;


import com.example.oceanbase.demos.web.entity.LeaveRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRecordEntityRepository extends JpaRepository<LeaveRecordEntity, String> {
    LeaveRecordEntity findByRecordId(Integer recordId);

    @Override
    LeaveRecordEntity getById(String s);

    List<LeaveRecordEntity> findByUserIdAndState(Integer userId, int state);
}
