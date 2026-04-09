package com.cyz.task2.controller;

import com.cyz.task2.entity.User; // 导入User实体类，包名需对应
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {


    @GetMapping("/{id}")
    public String getUserById(@PathVariable Long id) {

        return "查询成功，正在返回ID为 " + id + " 的用户信息";
    }


    @PostMapping
    public String createUser(@RequestBody User user) {

        return "新增成功，接收到用户：" + user.getName() + "，年龄：" + user.getAge();
    }


    @PutMapping("/{id}")
    public String updateUser(@PathVariable Long id, @RequestBody User user) {

        return "更新成功，用户ID: " + id + "， 新姓名: " + user.getName() + "， 新年龄: " + user.getAge();
    }


    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {

        return "删除成功，被删除的用户ID为: " + id;
    }
}