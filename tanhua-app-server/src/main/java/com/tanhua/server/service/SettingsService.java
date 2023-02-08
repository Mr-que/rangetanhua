package com.tanhua.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.dubbo.api.BlackListApi;
import com.tanhua.dubbo.api.QuestionApi;
import com.tanhua.dubbo.api.SettingsApi;
import com.tanhua.model.domain.*;
import com.tanhua.model.vo.SettingsVo;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SettingsService {

    @DubboReference
    private QuestionApi questionApi;

    @DubboReference
    private SettingsApi settingsApi;

    @DubboReference
    private BlackListApi blackListApi;

    public SettingsVo settings() {
        SettingsVo svo = new SettingsVo();
        //1、获取用户id
        Long userId = UserHolder.getUserId();
        svo.setId(userId);
        //2、获取用户的手机号码
        svo.setPhone(UserHolder.getUserMobile());
        //3、获取用户的陌生人问题
        Question question = questionApi.findByUserId(userId);
        String txt = question==null ? "你喜欢什么？" : question.getTxt();
        svo.setStrangerQuestion(txt);
        //4、获取用户的APP通知开关数据
        Settings settings = settingsApi.findByUserId(userId);
        if (settings != null){
            svo.setGonggaoNotification(settings.getGonggaoNotification());
            svo.setLikeNotification(settings.getLikeNotification());
            svo.setPinglunNotification(settings.getPinglunNotification());
        }
        return svo;
    }

    public void saveQuestion(String content) {
        //1、获取当前用户id
        Long userId = UserHolder.getUserId();
        //2、调用api查询当前用户的陌生人问题
        Question question = questionApi.findByUserId(userId);
        //3、判断问题是否存在
        if (question == null){
            //3.1 如果不存在，保存
            question = new Question();
            question.setTxt(content);
            question.setUserId(userId);
            questionApi.save(question);
        }
        //3.2 如果存在，更新
        question.setTxt(content);
        questionApi.update(question);
    }

    public void saveSettings(Map map) {
        Long userId = UserHolder.getUserId();
        Settings settings = settingsApi.findByUserId(userId);
        boolean likeNotification = (Boolean) map.get("likeNotification");
        boolean pinglunNotification = (Boolean) map.get("pinglunNotification");
        boolean gonggaoNotification = (Boolean)  map.get("gonggaoNotification");
        if (settings == null){
            settings = new Settings();
            settings.setUserId(userId);
            settings.setGonggaoNotification(gonggaoNotification);
            settings.setLikeNotification(likeNotification);
            settings.setPinglunNotification(pinglunNotification);
            settingsApi.save(settings);
        }
        settings.setGonggaoNotification(gonggaoNotification);
        settings.setLikeNotification(likeNotification);
        settings.setPinglunNotification(pinglunNotification);
        settingsApi.update(settings);
    }

    public PageResult backlist(int page, int size) {
        Long userId = UserHolder.getUserId();
        //2、调用API查询用户的黑名单分页列表 IPage对象
        IPage<UserInfo> iPage = blackListApi.findByUserId(userId,page,size);
        //3、对象转化，将查询的IPage对象的内容封装到PageResult中
        PageResult pr = new PageResult(page,size, iPage.getTotal(),iPage.getRecords());
        return pr;
    }

    public void deleteBlackList(Long blackUserId) {
        Long userId = UserHolder.getUserId();
        //调用api删除
        blackListApi.delete(userId,blackUserId);
    }
}
