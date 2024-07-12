package com.example.oceanbase.demos.web.controller;


import com.example.oceanbase.demos.web.entity.LeaveRecordEntity;
import com.example.oceanbase.demos.web.service.BaseService;
import com.example.oceanbase.demos.web.service.FlowableService;
import com.example.oceanbase.demos.web.util.RespBean;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/flowable")
public class FlowableController {


    @Autowired
    private TaskService taskService;
    @Autowired
    private BaseService baseService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private FlowableService flowableService;

    //生成一个时间戳随机数并加上一个100以内的随机数

    /**
     * 通过接收用户的一个请求传入用户的ID和以及描述信息来开启一个请假流程，并返回给用户这个流程的Id
     */
    @PostMapping("/apply")
    public RespBean apply(@RequestBody LeaveRecordEntity leaveRecordEntity) {
        return flowableService.apply_leave(leaveRecordEntity);
    }


    @GetMapping("/re_apply")
    public RespBean re_apply(String taskId,String reason) {
        return flowableService.re_apply(taskId,reason);
    }

    /**
     * 查询流程列表，待办列表，通过代码获取出用户需要处理的流程
     * 获取审批管理列表
     */
    @GetMapping("/list")
    public Object list(String group) {
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup(group).orderByTaskCreateTime().desc().list();
        for (Task task : tasks) {
            System.out.println(task.toString());
        }
        return "task size: " + tasks.size() + " , 第一个：" + tasks.get(0).toString();
    }

    /**
     * 批准，通过前端传入的任务ID来对此流程进行同意处理
     *
     * @param taskId 任务ID
     */
    @GetMapping("/approve")
    public RespBean approve(String taskId, String department) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return RespBean.error("流程不存在");
        }
        //通过审核
        HashMap<String, Object> map = new HashMap<>();
        map.put("approval", true);
        if (department != null && !department.equals("")) {
            map.put("department", department);
        }

        taskService.complete(taskId, map);
        return RespBean.ok("审批成功");
    }

//    @GetMapping("/manager_approve")
//    public String manager_apply(String taskId) {
//        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
//        if (task == null) {
//            throw new RuntimeException("流程不存在");
//        }
//        //通过审核
//        HashMap<String, Object> map = new HashMap<>();
//        map.put("approval", true);
//        taskService.complete(taskId, map);
//        return "processed ok!";
//    }

    /**
     * 拒绝
     */
    @GetMapping("/reject")
    public RespBean reject(String taskId, String back) {
        try {
            HashMap<String, Object> map = new HashMap<>();
            map.put("back", back);
            map.put("approval", false);
            taskService.complete(taskId, map);
            return RespBean.ok("驳回成功");
        } catch (Exception e) {
            return RespBean.error("驳回失败");
        }
    }

        @GetMapping("/apply_history")
        public RespBean searchResult (String userId){
            return flowableService.searchHistory(userId);
        }


        @GetMapping("/apply_now")
        public RespBean findMyActiveTasks (String userId){
            return flowableService.findMyActiveTasks(userId);
        }
    }
