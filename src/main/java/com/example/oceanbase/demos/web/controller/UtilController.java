/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.oceanbase.demos.web.controller;

import com.example.oceanbase.demos.web.entity.LeaveRecordEntity;
import com.example.oceanbase.demos.web.entity.User;
import com.example.oceanbase.demos.web.service.BaseService;
import com.example.oceanbase.demos.web.service.FlowableService;
import com.example.oceanbase.demos.web.util.RespBean;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@CrossOrigin
@RestController
@RequestMapping("/util")
public class UtilController {


    //    @Autowired
//    UserEntityRepository userEntityRepository;
    @Autowired
    RuntimeService runtimeService;
    @Autowired
    HistoryService historyService;
    @Autowired
    TaskService taskService;
    @Autowired
    FlowableService flowableService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private BaseService baseService;


    @GetMapping("/findTaskById")
    public RespBean findTaskById(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return RespBean.error("查不到任务");
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("taskId", String.valueOf(task.getId()));
            map.put("taskName", task.getName());
            map.put("taskCreateTime", task.getCreateTime());
            map.put("TaskVariables", task.getTaskLocalVariables());
            map.put("ProcessVariables", task.getProcessVariables());
            map.put("owner", task.getOwner());

            return RespBean.ok("查询成功", map);
        }
    }

    @Transactional
    @PostMapping("/deleteProcessInstanceById")
    public RespBean deleteProcessInstanceById(@RequestBody  String processInstanceId) {
        if (processInstanceId == null || StringUtils.isBlank(processInstanceId)) {
            return RespBean.error("流程实例id不能为空，请检查!!!");
        }
        try {
            //根据流程实例id 去ACT_RU_EXECUTION与ACT_RE_PROCDEF关联查询流程实例数据
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            if (null != processInstance) {
                runtimeService.deleteProcessInstance(processInstanceId, "流程实例删除");
            } else {
                historyService.deleteHistoricProcessInstance(processInstanceId);
            }
            return RespBean.ok("删除成功");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return RespBean.error("失败!原因为" + e.getMessage());
        }
    }

    @Transactional
    @PostMapping("/deleteProcessInstanceByTaskId")
    public RespBean deleteProcessInstanceByTaskId(@RequestBody String taskId) {
        Task task=taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task==null)
        {
            return RespBean.error("任务不存在");
        }
        String processInstanceId=task.getProcessInstanceId();
        if (processInstanceId == null || StringUtils.isBlank(processInstanceId)) {
            return RespBean.error("流程实例id不能为空，请检查!!!");
        }
        try {
            //根据流程实例id 去ACT_RU_EXECUTION与ACT_RE_PROCDEF关联查询流程实例数据
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            if (null != processInstance) {
                runtimeService.deleteProcessInstance(processInstanceId, "流程实例删除");
            } else {
                historyService.deleteHistoricProcessInstance(processInstanceId);
            }
            return RespBean.ok("删除成功");
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return RespBean.error("失败!原因为" + e.getMessage());
        }
    }


    /**
     * 查询流程列表，待办列表，通过代码获取出用户需要处理的流程
     * 获取审批管理列表
     */
    @PostMapping("/findTaskList")
    public RespBean findTaskList(@RequestBody User user) {
        return flowableService.findTaskList(user);
    }

    @GetMapping("/findAllProcess")
    public RespBean findAllProcess(Integer state) {
        List<ProcessInstance> processInstances = new ArrayList<>();
        if (state == 0)  //查询全部
            processInstances = runtimeService.createProcessInstanceQuery().list();
        else if (state == 1)
            processInstances = runtimeService.createProcessInstanceQuery().active().list();
        else if (state == 2)
            processInstances = runtimeService.createProcessInstanceQuery().suspended().list();
        List<Map<String, Object>> processInstanceList = new ArrayList<>();
        for (ProcessInstance processInstance : processInstances) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("processInstanceId", processInstance.getId());
            map.put("businessKey", processInstance.getBusinessKey());
            map.put("processVariables", processInstance.getProcessVariables());
            map.put("startTime", processInstance.getStartTime());
            processInstanceList.add(map);
        }
        return RespBean.ok("查询成功", processInstanceList);
    }

    @PostMapping("/changeRecord")
    public RespBean changeRecord(@RequestBody LeaveRecordEntity leaveRecordEntity) {

         return   baseService.changeRecord(leaveRecordEntity);

    }

    /**
     * 生成当前流程图表
     * 生成流程图
     *
     * @param processId 任务ID
     */
    @GetMapping("/processDiagram")
    public void genProcessDiagram(HttpServletResponse httpServletResponse, String processId) throws Exception {
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();

        //流程走完的不显示图
        if (pi == null) {
            return;
        }
        Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        System.out.println(task.getId());
        //使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
        String InstanceId = task.getProcessInstanceId();
        List<Execution> executions = runtimeService
                .createExecutionQuery()
                .processInstanceId(InstanceId)
                .list();

        //得到正在执行的Activity的Id
        List<String> activityIds = new ArrayList<>();
        List<String> flows = new ArrayList<>();
        for (Execution exe : executions) {
            List<String> ids = runtimeService.getActiveActivityIds(exe.getId());
            activityIds.addAll(ids);
        }

        //图片输出要加上这个不会显示二进制数据，有些浏览器正常，有些浏览器是直接显示二进制数据,因此修改
        httpServletResponse.setContentType("image/png".concat(";charset=UTF-8"));

        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
        ProcessEngineConfiguration engconf = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", activityIds, flows, engconf.getActivityFontName(), engconf.getLabelFontName(), engconf.getAnnotationFontName(), engconf.getClassLoader(), 1.0, true);
        OutputStream out = null;
        byte[] buf = new byte[1024];
        int legth = 0;
        try {
            out = httpServletResponse.getOutputStream();
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

}
