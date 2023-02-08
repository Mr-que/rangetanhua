package com.tanhua.server.service;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSON;
import com.tanhua.commons.utils.Constants;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.server.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserFreezeService {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 冻结3天 冻结7天 永久冻结
     * 冻结登录 冻结发言 冻结永久发布动态
     * @param status
     * @param userId
     */
    public void checkUserStatus(String status,Long userId){
        String key = Constants.FREEZE_USER + userId;
        String value = redisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(value)){
            Map map = JSON.parseObject(value, Map.class);
            String freezingRange = (String) map.get("freezingRange");
            if (status.equals(freezingRange)){
                throw new BusinessException(ErrorResult.builder().errMessage("用户已被冻结").build());
            }
        }
    }
}
