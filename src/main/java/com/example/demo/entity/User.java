package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user") // 绑定数据库表
public class User {
    @TableId(type = IdType.AUTO) // 自增主键
    private Long id;
    private String username;
    private String password;
}