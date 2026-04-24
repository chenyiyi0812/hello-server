package com.example.demo.service;

import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.common.Result;
import com.example.demo.vo.UserDetailVO;

public interface UserService {
    Result<String> register(UserDTO userDTO);
    Result<String> login(UserDTO userDTO);
    Result<User> getUserById(Long id);
    Result<Object> getUserPage(Integer pageNum, Integer pageSize);
    Result<UserDetailVO> getUserDetail(Long userId);
}