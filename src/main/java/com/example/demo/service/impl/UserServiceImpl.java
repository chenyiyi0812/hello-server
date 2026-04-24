package com.example.demo.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.entity.UserInfo;
import com.example.demo.mapper.UserInfoMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import com.example.demo.common.Result;
import com.example.demo.common.ResultCode;
import com.example.demo.utils.TokenUtils;
import com.example.demo.vo.UserDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private static final String CACHE_KEY_PREFIX = "user:detail:";

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Result<String> register(UserDTO userDTO) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userDTO.getUsername());
        User dbUser = userMapper.selectOne(queryWrapper);
        if (dbUser != null) {
            return Result.error(ResultCode.USER_HAS_EXISTED);
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());
        save(user);

        return Result.success("注册成功！");
    }

    @Override
    public Result<String> login(UserDTO userDTO) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userDTO.getUsername());
        User dbUser = userMapper.selectOne(queryWrapper);
        if (dbUser == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        if (!dbUser.getPassword().equals(userDTO.getPassword())) {
            return Result.error(ResultCode.PASSWORD_ERROR);
        }

        String token = TokenUtils.generateToken();
        return Result.success(token);
    }

    @Override
    public Result<User> getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }
        return Result.success(user);
    }

    @Override
    public Result<Object> getUserPage(Integer pageNum, Integer pageSize) {
        Page<User> pageParam = new Page<>(pageNum, pageSize);
        Page<User> resultPage = userMapper.selectPage(pageParam, null);
        return Result.success(resultPage);
    }

    @Override
    public Result<UserDetailVO> getUserDetail(Long userId) {
        String key = CACHE_KEY_PREFIX + userId;

        // 1. 先查缓存
        String json = redisTemplate.opsForValue().get(key);
        if (json != null && !json.trim().isEmpty()) {
            try {
                UserDetailVO cacheVO = JSONUtil.toBean(json, UserDetailVO.class);
                return Result.success(cacheVO);
            } catch (Exception e) {
                // 缓存数据异常，删掉脏缓存，继续查数据库
                redisTemplate.delete(key);
            }
        }

        // 2. 查数据库
        UserDetailVO userDetail = userInfoMapper.getUserDetail(userId);
        if (userDetail == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        // 3. 写回缓存
        try {
            String userJson = JSONUtil.toJsonStr(userDetail);
            redisTemplate.opsForValue().set(
                key,
                userJson,
                10,
                java.util.concurrent.TimeUnit.MINUTES
            );
        } catch (Exception e) {
            // 缓存写入失败，不影响返回结果
        }

        return Result.success(userDetail);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Result<String> updateUserInfo(UserInfo userInfo) {
        // 参数校验,userInfo 不能为空，并且 userId 不能为空,后面删除 Redis 缓存时
        if (userInfo == null || userInfo.getUserId() == null) {
            return Result.error(ResultCode.OPERATION_FAILED);
        }

        // 先操作数据库
        int count = userInfoMapper.updateById(userInfo);
        if (count == 0) {
            // 如果更新失败，可能是记录不存在，尝试插入
            userInfoMapper.insert(userInfo);
        }

        // 清除缓存
        String key = CACHE_KEY_PREFIX + userInfo.getUserId();
        redisTemplate.delete(key);

        return Result.success("更新成功");
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Result<String> deleteUser(Long userId) {
        // 先操作数据库
        userMapper.deleteById(userId);
        
        // 删除用户扩展信息
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserInfo> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getUserId, userId);
        userInfoMapper.delete(queryWrapper);

        // 清除缓存
        String key = CACHE_KEY_PREFIX + userId;
        redisTemplate.delete(key);

        return Result.success("删除成功");
    }
}