package com.example.oceanbase.demos.web.listener;

import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;

public class ManagerTaskHandler implements TaskListener {
    private static final long serialVersionUID = -326245510542194886L;
    @Override
    public void notify(DelegateTask delegateTask) {
        delegateTask.setAssignee("经理");
    }
}
