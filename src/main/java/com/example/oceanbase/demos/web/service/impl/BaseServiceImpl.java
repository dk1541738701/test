package com.example.oceanbase.demos.web.service.impl;

import com.example.oceanbase.demos.web.entity.LeaveRecordEntity;
import com.example.oceanbase.demos.web.entity.User;
import com.example.oceanbase.demos.web.repository.LeaveRecordEntityRepository;
import com.example.oceanbase.demos.web.repository.UserEntityRepository;
import com.example.oceanbase.demos.web.service.BaseService;
import com.example.oceanbase.demos.web.util.RespBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;


@Service
public class BaseServiceImpl implements BaseService {
    @Autowired
    private UserEntityRepository userEntityRepository;

    @Autowired
    private LeaveRecordEntityRepository leaveRecordEntityRepository;

    //声明一个日志记录器
    private static final Logger logger = LoggerFactory.getLogger(BaseServiceImpl.class);

    @Override
    public User getUserById(int id) {
        return userEntityRepository.findById(id);

    }

    @Override
    public RespBean login(String username, String password) {
        User user = userEntityRepository.findByUsernameAndPassword(username, password);
        if (user == null) {
            return RespBean.error("查不到用户");
        } else
            return RespBean.ok("登录成功", user.getId());
    }

    @Override
    public void saveLeaveRecord(LeaveRecordEntity leaveRecordEntity) {
        try {
            leaveRecordEntityRepository.save(leaveRecordEntity);
        } catch (Exception e) {
            logger.error("保存请假记录失败", e);
        }
    }

    @Override
    public RespBean changeRecord(LeaveRecordEntity leaveRecordEntity) {
        try {
            LeaveRecordEntity newLeaveRecordEntity = leaveRecordEntityRepository.findByRecordId(leaveRecordEntity.getRecordId());
            if (leaveRecordEntity.getState() != null) {
                newLeaveRecordEntity.setState(leaveRecordEntity.getState());
            }
            if (leaveRecordEntity.getResponse() != null) {
                newLeaveRecordEntity.setResponse(leaveRecordEntity.getResponse());
            }
            if (leaveRecordEntity.getAuditor() != null) {
                newLeaveRecordEntity.setAuditor(leaveRecordEntity.getAuditor());
            }
            if (leaveRecordEntity.getDecision() != null) {
                newLeaveRecordEntity.setDecision(leaveRecordEntity.getDecision());
            }
            String updateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            newLeaveRecordEntity.setUpdateTime(updateTime);
            leaveRecordEntityRepository.save(newLeaveRecordEntity);
            return RespBean.ok("修改成功");
        } catch (Exception e) {
            return RespBean.error("修改失败" + e);
        }
    }
}
