package com.example.oceanbase.demos.web.service;

import com.example.oceanbase.demos.web.entity.LeaveRecordEntity;
import com.example.oceanbase.demos.web.entity.User;
import com.example.oceanbase.demos.web.util.RespBean;

public interface BaseService {
        User getUserById(int id);
        RespBean login(String username, String password);

        void saveLeaveRecord(LeaveRecordEntity leaveRecordEntity);

        RespBean changeRecord(LeaveRecordEntity leaveRecordEntity);
}
