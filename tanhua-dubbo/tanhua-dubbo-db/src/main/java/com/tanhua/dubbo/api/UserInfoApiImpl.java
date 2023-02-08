package com.tanhua.dubbo.api;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.dubbo.mappers.UserInfoMapper;
import com.tanhua.model.domain.UserInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@DubboService
public class UserInfoApiImpl implements UserInfoApi{

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public void save(UserInfo userInfo) {
        userInfoMapper.insert(userInfo);
    }

    @Override
    public void update(UserInfo userInfo) {
        userInfoMapper.updateById(userInfo);
    }

    @Override
    public UserInfo findById(Long id) {
        QueryWrapper<UserInfo> qw = new QueryWrapper<>();
        qw.eq("id", id);
        return userInfoMapper.selectOne(qw);
    }

    @Override
    public Map<Long, UserInfo> findByIds(List<Long> ids, UserInfo userInfo) {
        //一次查询所有用户减少压力
        QueryWrapper<UserInfo> qw = new QueryWrapper<>();
        qw.in("id", ids);
        if (userInfo != null){
            if (!StringUtils.isEmpty(userInfo.getGender()))
                qw.eq("gender", userInfo.getGender());
            if (userInfo.getAge() != null)
                qw.lt("age", userInfo.getAge());
            if (userInfo.getNickname() != null)
                qw.like("nickname", userInfo.getNickname());
        }
        List<UserInfo> lists = userInfoMapper.selectList(qw);
        Map<Long, UserInfo> map = CollUtil.fieldValueMap(lists, "id");
        return map;
    }

    @Override
    public IPage<UserInfo> findAll(Integer page, Integer pagesize) {
        return userInfoMapper.selectPage(new Page<UserInfo>(page,pagesize),null);
    }
}
