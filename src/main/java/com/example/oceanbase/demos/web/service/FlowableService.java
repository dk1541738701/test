package com.example.oceanbase.demos.web.service;

import com.example.oceanbase.demos.web.entity.LeaveRecordEntity;
import com.example.oceanbase.demos.web.entity.User;
import com.example.oceanbase.demos.web.util.RespBean;

public interface FlowableService {
    RespBean apply_leave(LeaveRecordEntity leaveRecordEntity);

    RespBean re_apply(String taskId,String reason);
    RespBean findTaskList(User user);
    RespBean searchHistory(String userId);
    RespBean findTaskById(String taskId);

    RespBean findMyActiveTasks(String userId);
}
