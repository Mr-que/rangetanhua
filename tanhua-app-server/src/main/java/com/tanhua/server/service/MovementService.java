package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.MovementApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VisitorsApi;
import com.tanhua.model.domain.PageResult;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.mongo.Movement;
import com.tanhua.model.mongo.Visitors;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.MovementsVo;
import com.tanhua.model.vo.VisitorsVo;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.tanhua.commons.utils.Constants.MOVEMENTS_RECOMMEND;

@Service
public class MovementService {

    @DubboReference
    private MovementApi movementApi;

    @DubboReference
    private UserInfoApi userInfoApi;

    @DubboReference
    private VisitorsApi visitorsApi;

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    public void publishMovement(Movement movement, MultipartFile[] imageContent) throws IOException {
        //1.判断发布的内容是否为空
        if (StringUtils.isEmpty(movement.getTextContent())){
            throw new BusinessException(ErrorResult.contentError());
        }
        //2.获取当前登录人的id
        Long userId = UserHolder.getUserId();
        //3.将文件上传到阿里云
        List<String> medias = new ArrayList<>();
        if (imageContent != null || imageContent.length > 0){
            for (MultipartFile multipartFile : imageContent) {
                String upload = ossTemplate.upload(multipartFile.getOriginalFilename(), multipartFile.getInputStream());
                medias.add(upload);
            }
        }
        //4.将数据封装到Movement对象
        movement.setUserId(userId);
        movement.setMedias(medias);
        movementApi.publish(movement);
    }

    public PageResult findByUserId(Long userId, Integer page, Integer pagesize) {
        //1、根据用户id，调用API查询个人动态内容
        PageResult pr = movementApi.findByUserId(userId,page,pagesize);
        //2、获取PageResult中的item列表对象
        List<Movement> items = (List<Movement>) pr.getItems();
        //3、非空判断
        if (items == null){
            return pr;
        }
        //4、循环数据列表
        List<MovementsVo> list = new ArrayList<>();
        UserInfo user = userInfoApi.findById(userId);
        for (Movement item : items) {
            //5、一个Movement构建一个Vo对象
            MovementsVo vo = MovementsVo.init(user, item);
            list.add(vo);
        }
        pr.setItems(list);
        return pr;
    }

    public PageResult findFriendMovements(Integer page, Integer pagesize) {
        //1.获取当前用户id
        Long userId = UserHolder.getUserId();
        //2.查询数据，根据用户id查询朋友圈中所有动态
        List<Movement> list = movementApi.findFriendMovements(page,pagesize,userId);
        //3.非空判断
        if (list == null || list.size() == 0){
            return new PageResult();
        }
        //4.获取好友id
        List<Long> userIds = CollUtil.getFieldValues(list, "userId", Long.class);
        //5.循环数据列表,获取所有好友的动态封装成MovementsVO
        Map<Long, UserInfo> userMaps = userInfoApi.findByIds(userIds, null);
        List<MovementsVo> vos = new ArrayList<>();
        for (Movement movement : list) {
            UserInfo userInfo = userMaps.get(movement.getUserId());
            MovementsVo vo = MovementsVo.init(userInfo, movement);
            vos.add(vo);
        }
        return new PageResult(page,pagesize,0l,vos);
    }

    //推荐动态
    public PageResult findRecommendMovements(Integer page, Integer pagesize) {
        String redisKey = MOVEMENTS_RECOMMEND + UserHolder.getUserId();
        String redisData = (String) redisTemplate.opsForValue().get(redisKey);
        List<Movement> list = Collections.EMPTY_LIST;
        if (StringUtils.isEmpty(redisData)){
            //随机推荐十条
            list = movementApi.randomMovements(pagesize);
        }else {
            //从redis中获取
            String[] split = redisData.split(",");
            if ((page - 1) * pagesize > split.length) {
                return new PageResult();
            }
            //获取推荐的pid集合
            List<Long> pids = Arrays.stream(split)
                    .skip((page - 1) * pagesize)
                    .limit(pagesize)
                    .map(s -> Convert.toLong(s))
                    .collect(Collectors.toList());
            //根据pid获取动态
            list = movementApi.findByPids(pids);
        }
        //构造VO对象
        List<Long> userIds = CollUtil.getFieldValues(list, "userId", Long.class);
        Map<Long, UserInfo> userMaps = userInfoApi.findByIds(userIds, null);
        List<MovementsVo> vos = new ArrayList<>();
        for (Movement movement : list) {
            UserInfo userInfo = userMaps.get(movement.getUserId());
            MovementsVo vo = MovementsVo.init(userInfo,movement);
            vos.add(vo);
        }
        return new PageResult(page,pagesize,0l,vos);
    }

    public MovementsVo findMovementById(String movementId) {
        Movement movement = movementApi.findById(movementId);
        if (movement == null){
            return null;
        }else {
            UserInfo userInfo = userInfoApi.findById(movement.getUserId());
            return MovementsVo.init(userInfo, movement);
        }
    }

    //查询谁看过我的列表集合
    public List<VisitorsVo> queryVisitorsList() {
        //从redis中查询数据（hash）
        String key = Constants.VISITOR_USER;
        String hashKey = String.valueOf(UserHolder.getUserId());
        String value = (String) redisTemplate.opsForHash().get(key, hashKey);
        Long date = StringUtils.isEmpty(value) ? null : Long.valueOf(value);

        List<Visitors> list = visitorsApi.queryMyVisitors(date,UserHolder.getUserId());
        if (CollUtil.isEmpty(list)){
            return new ArrayList<>();
        }

        List<Long> userIds = CollUtil.getFieldValues(list, "visitorUserId", Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);
        List<VisitorsVo> vos = new ArrayList<>();
        for (Visitors visitors : list) {
            //查询来访用户的用户详情
            UserInfo userInfo = map.get(visitors.getVisitorUserId());
            if (userInfo != null){
                VisitorsVo vo = VisitorsVo.init(userInfo, visitors);
                vos.add(vo);
            }
        }
        return vos;
    }
}

