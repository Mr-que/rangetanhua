package com.tanhua.dubbo.api;

import cn.hutool.core.collection.CollUtil;
import com.tanhua.model.domain.PageResult;
import com.tanhua.model.mongo.RecommendUser;
import com.tanhua.model.vo.UserLike;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@DubboService
public class RecommendUserApiImpl implements RecommendUserApi{

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public RecommendUser queryWithMaxScore(Long toUserId) {
        //根据toUserId查询，根据评分score排序，获取第一条
        //构建Criteria
        Criteria criteria = Criteria.where("toUserId").is(toUserId);
        //构建Query对象
        Query query = Query.query(criteria).with(Sort.by(Sort.Order.desc("score"))).limit(1);
        //调用mongoTemplate查询
        return mongoTemplate.findOne(query, RecommendUser.class);
    }

    /**
     * 返回用户的推荐人集合，主要获得推荐人id
     * @param page 分页
     * @param pagesize  分页大小
     * @param userId    用户id
     * @return  PageResult
     */
    @Override
    public PageResult queryRecommendUserList(Integer page, Integer pagesize, Long userId) {
        //1.构建Criteria对象,查询id为用户的推荐人集合(主要是id)
        Criteria criteria = Criteria.where("toUserId").is(userId);
        //2.创建Query对象,查询条件按score降序
        Query query = Query.query(criteria).with(Sort.by(Sort.Order.desc("score")))
                .limit(pagesize).skip((page - 1) * pagesize);
        //3.调用mongoTemplate查询符合条件的推荐人集合和个数，构建PageResult返回
        List<RecommendUser> list = mongoTemplate.find(query, RecommendUser.class);
        long count = mongoTemplate.count(query, RecommendUser.class);
        return new PageResult(page,pagesize,count,list);
    }

    @Override
    public RecommendUser queryByUserId(Long userId, Long toUserId) {
        Criteria criteria = Criteria.where("toUserId").is(toUserId).and("userId").is(userId);
        Query query = Query.query(criteria);
        RecommendUser user = mongoTemplate.findOne(query, RecommendUser.class);
        if (user == null){
            user = new RecommendUser();
            user.setUserId(userId);
            user.setToUserId(toUserId);
            user.setScore(90d);
        }
        return user;
    }

    /**
     * 查询卡片列表，查询时需要排除喜欢和不喜欢的用户
     * 1、排除喜欢，不喜欢的用户
     * 2、随机展示
     * 3、指定数量
     */
    @Override
    public List<RecommendUser> queryCardsList(Long userId, int count) {
        //1.查询喜欢或不喜欢的用户
        List<UserLike> likeList = mongoTemplate.find(Query.query(Criteria.where("userId").is(userId)), UserLike.class);
        //1.1 userId喜欢或者不喜欢的用户id
        List<Long> likeUserIds = CollUtil.getFieldValues(likeList, "likeUserId", Long.class);
        //2.构造查询推荐用户的条件 匹配 userId and likeUserIds进行排除选择过了的用户
        Criteria criteria = Criteria.where("toUserId").is(userId).
                and("userId").nin(likeUserIds);
        //3、使用统计函数，随机获取推荐的用户列表
        TypedAggregation<RecommendUser> aggregation = TypedAggregation.newAggregation(
                RecommendUser.class, Aggregation.match(criteria),Aggregation.sample(count));
        AggregationResults<RecommendUser> results = mongoTemplate.aggregate(aggregation,RecommendUser.class);
        return results.getMappedResults();
    }
}
