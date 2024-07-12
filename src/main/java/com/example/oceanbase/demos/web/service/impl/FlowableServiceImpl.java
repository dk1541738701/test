package com.example.oceanbase.demos.web.service.impl;


import com.example.oceanbase.demos.web.entity.LeaveRecordEntity;
import com.example.oceanbase.demos.web.entity.User;
import com.example.oceanbase.demos.web.repository.LeaveRecordEntityRepository;
import com.example.oceanbase.demos.web.service.BaseService;
import com.example.oceanbase.demos.web.service.FlowableService;
import com.example.oceanbase.demos.web.util.RespBean;
import com.example.oceanbase.demos.web.util.util;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class FlowableServiceImpl implements FlowableService {
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private BaseService baseService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private LeaveRecordEntityRepository leaveRecordEntityRepository;

    Logger logger = LoggerFactory.getLogger(FlowableServiceImpl.class);

    public static Integer getRecordId() {
        Integer time = (int) (System.currentTimeMillis() / 1000);

        return (int) (time + Math.random() * 10000);
    }

    public RespBean apply_leave(LeaveRecordEntity leaveRecordEntity) {
        if (leaveRecordEntity.getUserId() == null) {
            return RespBean.error("用户Id不能为空");
        }
        Integer recordId = getRecordId(); //生成随机recordId
        leaveRecordEntity.setRecordId(recordId);
        List<LeaveRecordEntity> list = leaveRecordEntityRepository.findByUserIdAndState(leaveRecordEntity.getUserId(), 1);
        //将时间戳改为时间
        Long startTime = Long.parseLong(leaveRecordEntity.getStartTime());
        Long endTime = Long.parseLong(leaveRecordEntity.getEndTime());
        leaveRecordEntity.setStartTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(startTime)));
        leaveRecordEntity.setEndTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(endTime)));

        String applyTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        ;
        leaveRecordEntity.setApplyTime(applyTime);
        leaveRecordEntity.setUpdateTime(applyTime);
        leaveRecordEntity.setState(1);
        leaveRecordEntity.setDecision("等待审核");
        baseService.saveLeaveRecord(leaveRecordEntity);
        //启动流程
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", String.valueOf(leaveRecordEntity.getUserId()));
        map.put("recordId", recordId);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("apply_day", String.valueOf(leaveRecordEntity.getUserId()), map);
        System.out.println("流程Id为：" + processInstance.getId());
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        if (task == null) {
            throw new RuntimeException("流程不存在");
        }
        User user = baseService.getUserById(leaveRecordEntity.getUserId());
        HashMap<String, Object> map1 = new HashMap<>();
        map1.put("userId", String.valueOf(leaveRecordEntity.getUserId()));
        map1.put("recordId", recordId);
        map1.put("team", user.getTeam());
        map1.put("role", user.getRole());
        map1.put("department", user.getDepartment());
        try {
            taskService.complete(task.getId(), map1);
        } catch (Exception e) {
            logger.info("提交失败", e);
            return RespBean.error("提交失败");
        }
        return RespBean.ok("提交成功.流程Id为：" + processInstance.getId());
//        return RespBean.ok("提交成功");
    }

    public RespBean re_apply(String taskId, String reason) {
        //待修改
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
        String userId = taskService.getVariable(taskId, "userId").toString();

        String recordId = taskService.getVariable(taskId, "recordId").toString();
        LeaveRecordEntity leaveRecordEntity = leaveRecordEntityRepository.findByRecordId(Integer.valueOf(recordId));
        leaveRecordEntity.setUpdateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        leaveRecordEntity.setState(1);
        leaveRecordEntity.setReason(reason);
        leaveRecordEntity.setAuditor(null);
        leaveRecordEntity.setResponse(null);
        leaveRecordEntity.setDecision("等待审核");
        leaveRecordEntityRepository.save(leaveRecordEntity);
        User user = baseService.getUserById(Integer.parseInt(userId));
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("recordId", recordId);
        map.put("team", user.getTeam());
        map.put("role", user.getRole());
        map.put("department", user.getDepartment());
        taskService.complete(taskId, map);
        return RespBean.ok("重新提交成功");
    }

    public RespBean findMyActiveTasks(String userId) {
        try {
            List<ProcessInstance> myActiveProcess = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(String.valueOf(userId))
                    .orderByStartTime().desc().list();
            List<Map<String, Object>> res = new ArrayList();
            if(myActiveProcess.isEmpty()){
                return RespBean.ok("查询成功", res);
            }
            for (ProcessInstance processInstance : myActiveProcess) {
                Map<String, Object> map = new HashMap<>();
                Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
                String recordId = runtimeService.getVariable(processInstance.getId(), "recordId").toString();
                LeaveRecordEntity leaveRecordEntity = leaveRecordEntityRepository.findByRecordId(Integer.valueOf(recordId));
                map.put("LeaveRecord", leaveRecordEntity);
                map.put("taskName", task.getName());
                map.put("taskId", task.getId());
                res.add(map);
            }
            return RespBean.ok("查询成功", res);
        } catch (Exception e) {
            return RespBean.error("查询失败");
        }
    }


    public RespBean findTaskList(User user) {
        List<Task> tasks = new ArrayList<>();
        if (user.getId() != null) {
            tasks = taskService.createTaskQuery().taskAssignee(String.valueOf(user.getId())).orderByTaskCreateTime().desc().list();
        } else if (user.getDepartment() != null) {
            tasks = taskService.createTaskQuery().taskCandidateGroup(user.getDepartment()).orderByTaskCreateTime().desc().list();
        } else if (user.getTeam() != null) {
            tasks = taskService.createTaskQuery().taskCandidateGroup(user.getTeam()).orderByTaskCreateTime().desc().list();
        } else {
            return RespBean.error("参数错误", tasks);
        }
        List<Map<String, Object>> list = new ArrayList<>();

        for (Task task : tasks) {
            Map<String, Object> map = new HashMap<>();
            String recordId =  taskService.getVariable(task.getId(), "recordId").toString();
            map.put("taskId", task.getId());
//            map.put("taskName", task.getName());
//            map.put("createTime", task.getCreateTime());
            LeaveRecordEntity leaveRecordEntity = leaveRecordEntityRepository.findByRecordId(Integer.valueOf(recordId));
            User user1 = baseService.getUserById(leaveRecordEntity.getUserId());
            if(user1 == null){
                return RespBean.error("用户id不存在");
            }
            map.put("leaveRecord", leaveRecordEntity);
            map.put("applyName", user1.getName());
            map.put("department", user1.getDepartment());
            map.put("role", user1.getRole());
            map.put("job", user1.getJob());
            map.put("team", user1.getTeam());
            list.add(map);
//            System.out.println(task.toString());
        }
        return RespBean.ok("查询成功", list);
    }

    public RespBean searchHistory(String userId) {
        List<Map<String, Object>> historyInfos = new ArrayList<>();
        //根据key查询流程
        List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(userId).finished().orderByProcessInstanceEndTime().desc().list();
        for (HistoricProcessInstance historicProcessInstance : historicProcessInstances) {
            LeaveRecordEntity leaveRecordEntity = new LeaveRecordEntity();
            Map<String, Object> map = new HashMap<>();
            List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(historicProcessInstance.getId())
                    .list();
            //需要修改
            for (HistoricVariableInstance historicVariableInstance : historicVariableInstances) {
                String variableName = historicVariableInstance.getVariableName();
                String value = historicVariableInstance.getValue().toString();
                if ("recordId".equals(variableName)) {
                    leaveRecordEntity = leaveRecordEntityRepository.findByRecordId(Integer.valueOf(value));
                    try {
                        if (leaveRecordEntity != null)
                            map = util.getEntityMap(leaveRecordEntity);
                        map.put("processInstanceId", historicProcessInstance.getId());
                    } catch (IllegalAccessException e) {
                        return RespBean.error("查询记录失败", e);
                    }
                }
            }
            if (!map.isEmpty()) {
                historyInfos.add(map);
            }

        }
        return RespBean.ok("查询记录成功", historyInfos);
    }

    @Override
    public RespBean findTaskById(String taskId) {
        return RespBean.ok("查询成功", taskService.createTaskQuery().taskId(taskId).singleResult());
    }
}
