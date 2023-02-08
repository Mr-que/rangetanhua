package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import com.tanhua.autoconfig.template.HuanXinTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.FriendApi;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.model.domain.PageResult;
import com.tanhua.model.domain.User;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.mongo.Friend;
import com.tanhua.model.vo.ContactVo;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.UserInfoVo;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MessagesService {

    @DubboReference
    private UserApi userApi;

    @DubboReference
    private UserInfoApi userInfoApi;

    @DubboReference
    private FriendApi friendApi;

    @Autowired
    private HuanXinTemplate template;

    /**
     * 根据环信id查询用户详情
     */
    public UserInfoVo findUserInfoByHuanxin(String huanxinId) {
        //1.根据环信id查询user用户详情
        User user = userApi.finByHuanxin(huanxinId);
        //2.根据userId查询userInfo详情
        UserInfo userInfo = userInfoApi.findById(user.getId());
        UserInfoVo vo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, vo);
        if (userInfo.getAge() != null){
            vo.setAge(userInfo.getAge().toString());
        }
        return vo;
    }

    //添加好友关系
    public void contacts(Long friendId) {
        Boolean aBoolean = template
                .addContact(Constants.HX_USER_PREFIX + UserHolder.getUserId(), Constants.HX_USER_PREFIX + friendId);
        if (!aBoolean){
            throw new BusinessException(ErrorResult.error());
        }
        friendApi.save(UserHolder.getUserId(),friendId);
    }

    //删除好友关系
    public void deleteContact(Long friendId) {
        Boolean aBoolean = template
                .deleteContact(Constants.HX_USER_PREFIX + UserHolder.getUserId(), Constants.HX_USER_PREFIX + friendId);
        if (!aBoolean){
            throw new BusinessException(ErrorResult.error());
        }
        friendApi.delete(UserHolder.getUserId(),friendId);
    }

    public PageResult findFriends(Integer page, Integer pagesize, String keyword) {
        //1、调用API查询当前用户的好友数据 -- List<Friend>
        List<Friend> list = friendApi.findByUserId(UserHolder.getUserId(),page,pagesize);
        if (CollUtil.isEmpty(list)){
            return new PageResult();
        }
        //2、提取数据列表中的好友id
        List<Long> userIds = CollUtil.getFieldValues(list, "friendId", Long.class);
        //3、调用UserInfoAPI查询好友的用户详情
        UserInfo info = new UserInfo();
        info.setNickname(keyword);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, info);
        List<ContactVo> vos = new ArrayList<>();
        for (Friend friend : list) {
            UserInfo userInfo = map.get(friend.getFriendId());
            if (userInfo != null){
                ContactVo vo = ContactVo.init(userInfo);
                vos.add(vo);
            }
        }
        return new PageResult(page,pagesize,0l,vos);
    }
}
