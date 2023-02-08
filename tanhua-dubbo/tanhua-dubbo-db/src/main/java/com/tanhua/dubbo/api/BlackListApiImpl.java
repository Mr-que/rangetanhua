package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.dubbo.mappers.BlackListMapper;
import com.tanhua.dubbo.mappers.UserInfoMapper;
import com.tanhua.model.domain.BlackList;
import com.tanhua.model.domain.UserInfo;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class BlackListApiImpl implements BlackListApi {

    @Autowired
    private BlackListMapper blackListMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public IPage<UserInfo> findByUserId(Long userId, int page, int size) {
        Page pages = new Page(page, size);
        return userInfoMapper.findBlackList(pages,userId);
    }

    @Override
    public void delete(Long userId, Long blackUserId) {
        QueryWrapper<BlackList> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        qw.eq("black_user_id", blackUserId);
        blackListMapper.delete(qw);
    }
}