package com.example.oceanbase.demos.web.controller;


import com.example.oceanbase.demos.web.entity.User;
import com.example.oceanbase.demos.web.service.BaseService;
import com.example.oceanbase.demos.web.util.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    BaseService baseService;

    @PostMapping("/login")
    public RespBean login(@RequestBody User user) {
        return baseService.login(user.getUsername(), user.getPassword());

    }

    @GetMapping("/findUserInfo")
    public RespBean findUserInfo(Integer userId) {
        User user = baseService.getUserById(userId);
        user.setPassword(null);
        return RespBean.ok("查询成功", user);

    }
}
