package com.tanhua.dubbo.api;

import com.tanhua.model.domain.User;

public interface UserApi {

    //根据手机号码查询用户
    User findByMobile(String mobile);

    //保存用户，返回用户id
    Long save(User user);

    //修改用户，添加环信数据
    void update(User user);

    User finByHuanxin(String hxanxinId);

    User findById(Long id);
}
