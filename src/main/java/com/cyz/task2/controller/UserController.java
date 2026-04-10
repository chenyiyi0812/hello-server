package com.cyz.task2.controller;

import com.cyz.task2.common.Result; // 导入统一响应类
import com.cyz.task2.entity.User; // 导入User实体类
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * 根据ID查询用户 - GET
     */
    @GetMapping("/{id}")
    public Result<String> getUserById(@PathVariable Long id) {
        String data = "查询成功，正在返回ID为 " + id + " 的用户信息";
        return Result.success(data);
    }

    /**
     * 新增用户 - POST
     */
    @PostMapping
    public Result<String> createUser(@RequestBody User user) {
        String data = "新增成功，接收到用户：" + user.getName() + "，年龄：" + user.getAge();
        return Result.success(data);
    }

    /**
     * 更新用户 - PUT
     */
    @PutMapping("/{id}")
    public Result<String> updateUser(@PathVariable Long id, @RequestBody User user) {
        String data = "更新成功，用户ID: " + id + "，新姓名: " + user.getName() + "，新年龄: " + user.getAge();
        return Result.success(data);
    }

    /**
     * 删除用户 - DELETE
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        String data = "删除成功，被删除的用户ID为: " + id;
        return Result.success(data);
    }
}