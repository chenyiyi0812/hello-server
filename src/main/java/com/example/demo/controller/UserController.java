package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService; // 注入真实数据库服务

    /**
     * 注册接口：真正写入 PostgreSQL
     */
    @PostMapping
    public Result<String> register(@RequestBody User user) {
        // 1. 校验用户名是否已存在（查数据库）
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        User existUser = userService.getOne(wrapper);

        if (existUser != null) {
            return Result.error(400, "用户名已存在");
        }

        // 2. 真正插入数据库（核心！）
        boolean saveSuccess = userService.save(user);
        if (!saveSuccess) {
            return Result.error(500, "注册失败，数据库异常");
        }

        return Result.success("注册成功");
    }

    /**
     * 登录接口：从数据库校验
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody User user) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        User existUser = userService.getOne(wrapper);

        if (existUser == null) {
            return Result.error(400, "用户不存在");
        }
        if (!existUser.getPassword().equals(user.getPassword())) {
            return Result.error(400, "密码错误");
        }

        return Result.success("登录成功");
    }

    /**
     * 查询用户详情：从数据库查
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(user);
    }
}