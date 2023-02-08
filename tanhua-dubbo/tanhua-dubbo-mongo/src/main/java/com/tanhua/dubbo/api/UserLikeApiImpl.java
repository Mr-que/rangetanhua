package com.tanhua.dubbo.api;

import com.tanhua.model.vo.UserLike;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@DubboService
public class UserLikeApiImpl implements UserLikeApi{

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 用户喜欢或者不喜欢的详情保存或者更新Mongo中的数据
     * @param userId 用户id
     * @param likeUserId 选择id
     * @param isLike 是否喜欢
     */
    @Override
    public Boolean saveOrUpdate(Long userId, Long likeUserId, boolean isLike) {
        try {
            Query query = Query.query(Criteria.where("userId").is(userId).and("likeUserId").is(likeUserId));
            UserLike userLike = mongoTemplate.findOne(query, UserLike.class);
            if (userLike == null) {
                //保存
                userLike = new UserLike();
                userLike.setUserId(userId);
                userLike.setLikeUserId(likeUserId);
                userLike.setIsLike(isLike);
                userLike.setCreated(System.currentTimeMillis());
                userLike.setUpdated(System.currentTimeMillis());
                mongoTemplate.save(userLike);
            }else {
                //更新
                mongoTemplate.updateFirst(query
                        , Update.update("isLike", isLike).set("updated", System.currentTimeMillis())
                        , UserLike.class);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
