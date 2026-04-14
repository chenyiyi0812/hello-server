package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.common.Result;

public interface UserService extends IService<User> {
    Result<String> register(UserDTO userDTO);
    Result<String> login(UserDTO userDTO);
    Result<String> getUserById(Long id);
}