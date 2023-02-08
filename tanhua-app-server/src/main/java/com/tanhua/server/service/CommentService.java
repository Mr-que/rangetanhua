package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.CommentApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.model.domain.PageResult;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.mongo.Comment;
import com.tanhua.model.mongo.CommentType;
import com.tanhua.model.vo.CommentVo;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CommentService {

    @DubboReference
    private CommentApi commentApi;

    @DubboReference
    private UserInfoApi userInfoApi;

    @Autowired
    private RedisTemplate redisTemplate;

    public PageResult findComments(String movementId, Integer page, Integer pagesize) {
        //查询评论列表
        List<Comment> list = commentApi.findComments(movementId, CommentType.COMMENT,page,pagesize);
        if (CollUtil.isEmpty(list)){
            return new PageResult();
        }
        //获取userInfo详情 封装vo
        List<Long> userIds = CollUtil.getFieldValues(list, "userId", Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);
        //构造vo对象
        List<CommentVo> vos = new ArrayList<>();
        for (Comment comment : list) {
            UserInfo userInfo = map.get(comment.getUserId());
            if (userInfo != null){
                CommentVo cvo = CommentVo.init(userInfo, comment);
                vos.add(cvo);
            }
        }
        return new PageResult(page,pagesize,0l,vos);
    }

    //发布评论
    public void saveComments(String movementId, String comment) {
        Long userId = UserHolder.getUserId();
        //构造评论详情类
        Comment cm = new Comment();
        cm.setPublishId(new ObjectId(movementId));
        cm.setCommentType(CommentType.COMMENT.getType());
        cm.setContent(comment);
        cm.setUserId(userId);
        cm.setCreated(System.currentTimeMillis());
        Integer commentCount = commentApi.save(cm);
        log.info("commentCount = ", commentCount);
    }

    //动态点赞
    public Integer likeMovement(String movementId) {
        Long userId = UserHolder.getUserId();
        //1、调用API查询用户是否已点赞
        Boolean hasComment = commentApi.hasComment(movementId,userId,CommentType.LIKE);
        if (hasComment){
            throw new BusinessException(ErrorResult.likeError());
        }
        //3、调用API保存数据到Mongodb
        Comment cm = new Comment();
        cm.setPublishId(new ObjectId(movementId));
        cm.setCommentType(CommentType.LIKE.getType());
        cm.setUserId(userId);
        cm.setCreated(System.currentTimeMillis());
        Integer count = commentApi.save(cm);
        //4、拼接redis的key，将用户的点赞状态存入redis
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        String hasKey = Constants.MOVEMENT_LIKE_HASHKEY + userId;
        redisTemplate.opsForHash().put(key, hasKey, "1");
        return count;
    }

    //取消动态点赞
    public Integer dislikeMovement(String movementId) {
        Long userId = UserHolder.getUserId();
        //1、调用API查询用户是否已点赞
        Boolean hasComment = commentApi.hasComment(movementId,userId,CommentType.LIKE);
        if (!hasComment){
            throw new BusinessException(ErrorResult.likeError());
        }
        Comment cm = new Comment();
        cm.setPublishId(new ObjectId(movementId));
        cm.setCommentType(CommentType.LIKE.getType());
        cm.setUserId(userId);
        cm.setCreated(System.currentTimeMillis());
        Integer count = commentApi.delete(cm);
        //4、拼接redis的key，将用户的点赞状态存入redis
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        String hasKey = Constants.MOVEMENT_LIKE_HASHKEY + userId;
        redisTemplate.opsForHash().delete(key, hasKey);
        return count;
    }

    //动态love
    public Integer loveMovement(String movementId) {
        Long userId = UserHolder.getUserId();
        //1、调用API查询用户是否已点赞
        Boolean hasComment = commentApi.hasComment(movementId,userId,CommentType.LOVE);
        if (hasComment){
            throw new BusinessException(ErrorResult.loveError());
        }
        //3、调用API保存数据到Mongodb
        Comment cm = new Comment();
        cm.setPublishId(new ObjectId(movementId));
        cm.setCommentType(CommentType.LOVE.getType());
        cm.setUserId(userId);
        cm.setCreated(System.currentTimeMillis());
        Integer count = commentApi.save(cm);
        //4、拼接redis的key，将用户的点赞状态存入redis
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        String hasKey = Constants.MOVEMENT_LOVE_HASHKEY + userId;
        redisTemplate.opsForHash().put(key, hasKey, "1");
        return count;
    }

    //取消动态love
    public Integer disloveMovement(String movementId) {
        Long userId = UserHolder.getUserId();
        //1、调用API查询用户是否已点赞
        Boolean hasComment = commentApi.hasComment(movementId,userId,CommentType.LOVE);
        if (!hasComment){
            throw new BusinessException(ErrorResult.likeError());
        }
        Comment cm = new Comment();
        cm.setPublishId(new ObjectId(movementId));
        cm.setCommentType(CommentType.LOVE.getType());
        cm.setUserId(userId);
        cm.setCreated(System.currentTimeMillis());
        Integer count = commentApi.delete(cm);
        //4、拼接redis的key，将用户的点赞状态存入redis
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        String hasKey = Constants.MOVEMENT_LOVE_HASHKEY + userId;
        redisTemplate.opsForHash().delete(key, hasKey);
        return count;
    }

    public Integer likeComment(String commentId) {
        Long userId = UserHolder.getUserId();

        //3、调用API保存数据到Mongodb
        Comment cm = new Comment();
        cm.setId(new ObjectId(commentId));
        cm.setCommentType(CommentType.LIKE.getType());
        cm.setUserId(userId);
        cm.setCreated(System.currentTimeMillis());
        Integer count = commentApi.saveComment(cm);
        //4、拼接redis的key，将用户的点赞状态存入redis
        String key = Constants.COMMENTS_INTERACT_KEY + commentId;
        String hasKey = Constants.COMMENT_LIKE_HASHKEY + userId;
        redisTemplate.opsForHash().put(key, hasKey, "1");
        return count;
    }

    public Integer dislikeComment(String commentId) {
        Long userId = UserHolder.getUserId();

        //3、调用API保存数据到Mongodb
        Comment cm = new Comment();
        cm.setId(new ObjectId(commentId));
        cm.setCommentType(CommentType.LIKE.getType());
        cm.setUserId(userId);
        cm.setCreated(System.currentTimeMillis());
        Integer count = commentApi.deleteComment(cm);
        //4、拼接redis的key，将用户的点赞状态存入redis
        String key = Constants.COMMENTS_INTERACT_KEY + commentId;
        String hasKey = Constants.COMMENT_LIKE_HASHKEY + userId;
        redisTemplate.opsForHash().delete(key, hasKey);
        return count;
    }
}
