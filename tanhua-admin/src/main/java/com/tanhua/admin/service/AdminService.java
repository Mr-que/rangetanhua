package com.tanhua.admin.service;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.admin.exception.BusinessException;
import com.tanhua.admin.interceptor.AdminHolder;
import com.tanhua.admin.mapper.AdminMapper;
import com.tanhua.commons.utils.Constants;
import com.tanhua.commons.utils.JwtUtils;
import com.tanhua.model.domain.Admin;
import com.tanhua.model.vo.AdminVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    public Map login(Map map) {
        String username = (String) map.get("username");
        String password = (String) map.get("password");
        String verificationCode = (String) map.get("verificationCode");
        String uuid = (String) map.get("uuid");
        //2、判断用户名或者密码是否为空
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(username)){
            Map map1 = new HashMap();
            map1.put("message", "用户名或者密码为空");
            return map1;
        }
        //3、判断验证码是否正确
        String verity = redisTemplate.opsForValue().get(Constants.CAP_CODE + uuid);
        if (StringUtils.isEmpty(verity) || !verificationCode.equals(verity)){
            //验证码为空
            throw new BusinessException("验证码错误");
        }
        redisTemplate.delete(Constants.CAP_CODE + uuid);
        //4、根据用户名查询用户
        QueryWrapper<Admin> qw = new QueryWrapper<>();
        qw.eq("username", username);
        Admin admin = adminMapper.selectOne(qw);
        //5、判断用户是否存在，密码是否一致
        password = SecureUtil.md5(password);
        if (admin == null){
            throw new BusinessException("用户名错误");
        }
        if (!admin.getPassword().equals(password)){
            throw new BusinessException("密码错误");
        }
        //6、通过JWT生成token
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("username", username);
        claims.put("id", admin.getId());
        String token = JwtUtils.getToken(claims);

        Map res = new HashMap();
        res.put("token", token);
        return res;
    }

    public AdminVo profile() {
        Long userId = AdminHolder.getUserId();
        Admin admin = adminMapper.selectById(userId);
        return AdminVo.init(admin);
    }

    public void logout() {
        //将用户信息清空
        AdminHolder.remove();
    }
}
