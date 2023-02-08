package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.tanhua.autoconfig.template.HuanXinTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.*;
import com.tanhua.model.domain.PageResult;
import com.tanhua.model.domain.Question;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.mongo.RecommendUser;
import com.tanhua.model.mongo.Visitors;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.NearUserVo;
import com.tanhua.model.vo.RecommendUserDto;
import com.tanhua.model.vo.TodayBest;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TanhuaService {

    @DubboReference
    private RecommendUserApi recommendUserApi;

    @DubboReference
    private QuestionApi questionApi;

    @DubboReference
    private UserInfoApi userInfoApi;

    @DubboReference
    private UserLikeApi userLikeApi;

    @DubboReference
    private UserLocationApi userLocationApi;

    @DubboReference
    private VisitorsApi visitorsApi;

    @Autowired
    private MessagesService messagesService;

    @Autowired
    private HuanXinTemplate template;

    @Autowired
    private RedisTemplate redisTemplate;

    //今日佳人
    public TodayBest todayBest() {
        //1、获取用户id
        Long userId = UserHolder.getUserId();
        //2、调用API查询
        RecommendUser recommendUser = recommendUserApi.queryWithMaxScore(userId);
        if (recommendUser == null){
            recommendUser = new RecommendUser();
            recommendUser.setUserId(1l);
            recommendUser.setScore(99d);
        }
        //3、将RecommendUser转化为TodayBest对象
        UserInfo userInfo = userInfoApi.findById(recommendUser.getUserId());
        TodayBest vo = TodayBest.init(userInfo, recommendUser);
        return vo;
    }

    //查询分页推荐好友列表
    public PageResult recommendation(RecommendUserDto dto) {
        //1.获取用户id
        Long userId = UserHolder.getUserId();
        //2.调用recommendUserApi分页查询数据列表（PageResult -- RecommendUser）
        PageResult pr = recommendUserApi.queryRecommendUserList(dto.getPage(),dto.getPagesize(),userId);
        //3、获取分页中的RecommendUser数据列表
        List<RecommendUser> items = (List<RecommendUser>) pr.getItems();
        //4、判断列表是否为空
        if (items == null || items.size() <=0){
            return pr;
        }
        //5、提取所有推荐的用户id列表
        List<Long> ids = CollUtil.getFieldValues(items, "userId", Long.class);
        UserInfo userInfo = new UserInfo();
        userInfo.setAge(dto.getAge());
        userInfo.setGender(dto.getGender());
        //6、构建查询条件，批量查询所有的用户详情
        Map<Long,UserInfo> map = userInfoApi.findByIds(ids,userInfo);
        //7、循环推荐的数据列表，构建vo对象
        List<TodayBest> list = new ArrayList<>();
        for (RecommendUser item : items) {
            UserInfo user = map.get(item.getUserId());
            if (user != null){
                TodayBest tb = TodayBest.init(user, item);
                list.add(tb);
            }
        }
        pr.setItems(list);
        return pr;
    }

    //查看佳人详情
    public TodayBest personalInfo(Long userId) {
        //1、根据用户id查询，用户详情
        UserInfo userInfo = userInfoApi.findById(userId);
        //2、根据操作人id和查看的用户id，查询两者的推荐数据
        RecommendUser user = recommendUserApi.queryByUserId(userId,UserHolder.getUserId());

        //构造访客数据，保存
        Visitors visitors = new Visitors();
        visitors.setUserId(userId);
        visitors.setVisitorUserId(UserHolder.getUserId());
        visitors.setDate(System.currentTimeMillis());
        visitors.setVisitDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        visitors.setFrom("首页");
        visitors.setScore(user.getScore());
        visitorsApi.save(visitors);

        return TodayBest.init(userInfo, user);
    }

    //查看陌生人问题
    public String strangerQuestions(Long userId) {
        Question question = questionApi.findByUserId(userId);
        return question == null ? "你喜欢什么" : question.getTxt();
    }

    /**
     * 回复陌生人问题
     * 通过接受方的环信id回复消息
     */
    public void replyQuestions(Long userId, String reply) {
        Long currentUserId = UserHolder.getUserId();
        UserInfo userInfo = userInfoApi.findById(currentUserId);
        Map map = new HashMap();
        map.put("userId", currentUserId);
        map.put("huanXinId", Constants.HX_USER_PREFIX + currentUserId);
        map.put("nickname", userInfo.getNickname());
        map.put("strangerQuestion", strangerQuestions(userId));
        map.put("reply", reply);
        String message = JSON.toJSONString(map);
        Boolean aBoolean = template.sendMsg(Constants.HX_USER_PREFIX + userId, message);
        if (!aBoolean){
            throw new BusinessException(ErrorResult.error());
        }
    }

    //默认的推荐列表
    @Value("${tanhua.default.recommend.users}")
    private String recommendUser;

    public List<TodayBest> queryCardsList() {
        //1、调用推荐API查询数据列表（排除喜欢/不喜欢的用户，数量限制）
        List<RecommendUser> users = recommendUserApi.queryCardsList(UserHolder.getUserId(), 10);
        //2、判断数据是否存在，如果不存在，使用构造的默认数据 recommendUser
        if (CollUtil.isEmpty(users)) {
            users = new ArrayList<>();
            String[] userIds = recommendUser.split(",");
            for (String userId : userIds) {
                RecommendUser recommendUser = new RecommendUser();
                recommendUser.setUserId(Convert.toLong(userId));
                recommendUser.setToUserId(UserHolder.getUserId());
                recommendUser.setScore(RandomUtil.randomDouble(60,90));
                users.add(recommendUser);
            }
        }
        //3.构造vo -> 通过userId获取卡片列表中用户详情(userInfo) 由userInfo and RecommendUser 构造 TodayBest
        List<Long> ids = CollUtil.getFieldValues(users, "userId", Long.class);
        Map<Long, UserInfo> infos = userInfoApi.findByIds(ids, null);
        List<TodayBest> vos = new ArrayList<>();

        for (RecommendUser user : users) {
            UserInfo userInfo = infos.get(user.getUserId());
            if (userInfo != null){
                TodayBest tb = new TodayBest();
                vos.add(tb);
            }
        }
        return vos;
    }

    public void likeUser(Long likeUserId) {
        //1、调用API，保存喜欢数据(保存到MongoDB中)
        Boolean save = userLikeApi.saveOrUpdate(UserHolder.getUserId(), likeUserId, true);
        if (!save) {
            throw new BusinessException(ErrorResult.error());
        }
        //2、操作redis，写入喜欢的数据，删除不喜欢的数据 (喜欢的集合，不喜欢的集合)
        redisTemplate.opsForSet().add(Constants.USER_LIKE_KEY + UserHolder.getUserId(),likeUserId.toString());
        redisTemplate.opsForSet().remove(Constants.USER_NOT_LIKE_KEY + UserHolder.getUserId(), likeUserId.toString());
        //3、判断是否是双向喜欢
        if (isLike(likeUserId,UserHolder.getUserId())){
            messagesService.contacts(likeUserId);
        }
    }

    public void notLikeUser(Long likeUserId) {
        Boolean save = userLikeApi.saveOrUpdate(UserHolder.getUserId(), likeUserId, false);
        if (!save) {
            throw new BusinessException(ErrorResult.error());
        }
        //2、操作redis，写入喜欢的数据，删除不喜欢的数据 (喜欢的集合，不喜欢的集合)
        redisTemplate.opsForSet().add(Constants.USER_NOT_LIKE_KEY + UserHolder.getUserId(),likeUserId.toString());
        redisTemplate.opsForSet().remove(Constants.USER_LIKE_KEY + UserHolder.getUserId(), likeUserId.toString());
        //3、判断是否是双向喜欢
        if (isNotLike(likeUserId,UserHolder.getUserId())){
            messagesService.deleteContact(likeUserId);
        }
    }

    //是否双向喜欢 判断redis喜欢集合中是否二者相互包含
    private boolean isLike(Long likeUserId, Long userId) {
        return redisTemplate.opsForSet().
                isMember(Constants.USER_LIKE_KEY + likeUserId,userId.toString());
    }

    private boolean isNotLike(Long likeUserId, Long userId) {
        return redisTemplate.opsForSet().
                isMember(Constants.USER_NOT_LIKE_KEY + likeUserId,userId.toString());
    }

    public List<NearUserVo> queryNearUser(String gender, String distance) {
        //1、调用API查询附近的用户（返回的是附近的人的所有用户id，包含当前用户的id）
        List<Long> userIds = userLocationApi.queryNearUser(UserHolder.getUserId(), Double.valueOf(distance));
        //2、判断集合是否为空
        if(CollUtil.isEmpty(userIds)) {
            return new ArrayList<>();
        }
        //3、调用UserInfoApi根据用户id查询用户详情
        UserInfo userInfo = new UserInfo();
        userInfo.setGender(gender);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, userInfo);
        //4、构造返回值。
        List<NearUserVo> vos = new ArrayList<>();
        for (Long userId : userIds) {
            //排除当前用户
            if(userId == UserHolder.getUserId()) {
                continue;
            }
            UserInfo info = map.get(userId);
            if(info != null) {
                NearUserVo vo = NearUserVo.init(info);
                vos.add(vo);
            }
        }
        return vos;
    }
}
