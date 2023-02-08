package com.tanhua.admin.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.MovementApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VideoApi;
import com.tanhua.model.domain.PageResult;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.mongo.Movement;
import com.tanhua.model.mongo.Video;
import com.tanhua.model.vo.MovementsVo;
import com.tanhua.model.vo.VideoVo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ManagerService {

    @DubboReference
    private UserInfoApi userInfoApi;

    @DubboReference
    private VideoApi videoApi;

    @DubboReference
    private MovementApi movementApi;

    @Autowired
    private RedisTemplate redisTemplate;

    public PageResult findAllUsers(Integer page, Integer pagesize) {
        //1、调用API分页查询数据列表IPage<UserInfo>
        IPage<UserInfo> iPage = userInfoApi.findAll(page,pagesize);
        //2、需要将IPage转化为PageResult
        List<UserInfo> users = iPage.getRecords();
        for (UserInfo user : users) {
            String key = Constants.FREEZE_USER + user.getId();
            if (redisTemplate.hasKey(key)){
                user.setUserStatus("2");
            }
        }
        return new PageResult(page, pagesize, iPage.getTotal(), iPage.getRecords());
    }

    public ResponseEntity findById(Long userId) {
        UserInfo userInfo = userInfoApi.findById(userId);
        //判断用户冻结状态，从redis中查询
        String key = Constants.FREEZE_USER + userId;
        if (redisTemplate.hasKey(key)){
            userInfo.setUserStatus("2");
        }
        return ResponseEntity.ok(userInfo);
    }

    public PageResult findAllVideos(Integer page, Integer pagesize, Long userId) {
        //1、调用API查询视频列表（PageResult<video>）
        PageResult res = videoApi.findByUserId(page, pagesize, userId);
        List<Video> items = (List<Video>) res.getItems();
        //2、获取到分页对象中的List List<Video>
        UserInfo userInfo = userInfoApi.findById(userId);
        System.out.println(userInfo);
        //3、一个Video转化成一个VideoVo
        List<VideoVo> list = new ArrayList<>();
        for (Video item : items) {
            VideoVo vo = VideoVo.init(userInfo, item);
            list.add(vo);
        }
        return new PageResult(page,pagesize,0l,list);
    }

    //错误：把id当成了userId查询userInfo 无数据
    public PageResult findAllMovements(Integer page, Integer pagesize, Long userId, Integer state) {
        PageResult res = movementApi.findByUserId(userId,state, page, pagesize);
        List<MovementsVo> list = new ArrayList<>();
        List<Movement> items = (List<Movement>) res.getItems();
        for (Movement item : items) {
            UserInfo userInfo = userInfoApi.findById(item.getUserId());
            MovementsVo vo = MovementsVo.init(userInfo, item);
            list.add(vo);
        }
        res.setItems(list);
        return res;
    }

    //用户冻结操作
    public Map userFreeze(Map param) {
        Integer freezingTime = Integer.valueOf(param.get("freezingTime").toString());
        Long userId = (Long) param.get("userId");
        int days = 0;
        if (freezingTime == 1){
            days = 3;
        }
        if (freezingTime == 2){
            days = 7;
        }
        if (freezingTime == 3){
            days = -1;
        }
        String value = JSON.toJSONString(param);
        if (days > 0){
            redisTemplate.opsForValue().set(Constants.FREEZE_USER+userId, value,days, TimeUnit.MINUTES);
        }else {
            redisTemplate.opsForValue().set(Constants.FREEZE_USER+userId, value);
        }
        Map map = new HashMap();
        map.put("message","冻结成功");
        return map;
    }

    public Map userUnFreeze(Map param) {
        String userId = param.get("userId").toString();
        String key = Constants.FREEZE_USER + userId;
        redisTemplate.delete(key);
        Map map = new HashMap();
        map.put("message","解冻成功");
        return map;
    }
}
