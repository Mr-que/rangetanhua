package com.tanhua.server.service;

import com.tanhua.dubbo.api.UserApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.model.domain.User;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.vo.HuanXinUserVo;
import com.tanhua.model.vo.UserInfoVo;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class HuanXinService {

    @DubboReference
    private UserApi userApi;

    public HuanXinUserVo findHuanXinUser() {
        User user = userApi.findById(UserHolder.getUserId());
        if (user == null){
            return new HuanXinUserVo();
        }
        HuanXinUserVo vo = new HuanXinUserVo();
        vo.setUsername(user.getHxUser());
        vo.setPassword(user.getHxPassword());
        return vo;
    }

}
