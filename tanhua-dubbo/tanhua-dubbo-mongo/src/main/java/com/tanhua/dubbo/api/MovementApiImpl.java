package com.tanhua.dubbo.api;

import cn.hutool.core.collection.CollUtil;
import com.tanhua.dubbo.utils.IdWorker;
import com.tanhua.dubbo.utils.TimeLineService;
import com.tanhua.model.domain.PageResult;
import com.tanhua.model.mongo.Movement;
import com.tanhua.model.mongo.MovementTimeLine;
import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;
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
public class MovementApiImpl implements MovementApi{

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private TimeLineService timeLineService;

    @Override
    public void publish(Movement movement) {
        //1.保存动态详情
        try {
            movement.setPid(idWorker.getNextId("movement"));
            movement.setCreated(System.currentTimeMillis());
            mongoTemplate.save(movement);
            timeLineService.saveTimeLine(movement.getUserId(), movement.getId());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public PageResult findByUserId(Long userId, Integer page, Integer pagesize) {
        Criteria criteria = Criteria.where("userId").is(userId);
        Query query = Query.query(criteria).skip((page - 1) * pagesize).limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        List<Movement> movements = mongoTemplate.find(query, Movement.class);

        return new PageResult(page,pagesize,0l,movements);
    }

    @Override
    public List<Movement> findFriendMovements(Integer page, Integer pagesize, Long friendId) {
        //1.查询好友的时间线
        Query query = Query.query(Criteria.where("friendId").in(friendId))
                .skip((page - 1) * pagesize).limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        List<MovementTimeLine> timeLines = mongoTemplate.find(query, MovementTimeLine.class);
        //2.提取出所有的动态id
        List<ObjectId> movementIds = CollUtil.getFieldValues(timeLines, "movementId",ObjectId.class);
        //3.根据动态id提取动态
        Query queryM = Query.query(Criteria.where("id").in(movementIds));
        return mongoTemplate.find(queryM, Movement.class);
    }

    //随机获取动态
    @Override
    public List<Movement> randomMovements(Integer counts) {
        TypedAggregation aggregation = Aggregation.newAggregation(Movement.class,
                Aggregation.sample(counts));
        AggregationResults<Movement> movements = mongoTemplate.aggregate(aggregation, Movement.class);
        return movements.getMappedResults();
    }

    @Override
    public List<Movement> findByPids(List<Long> pids) {
        Query query = Query.query(Criteria.where("pid").in(pids));
        return mongoTemplate.find(query, Movement.class);
    }

    @Override
    public Movement findById(String movementId) {
        return mongoTemplate.findById(movementId, Movement.class);
    }

    @Override
    public PageResult findByUserId(Long userId, Integer state, Integer page, Integer pagesize) {
        Query query = new Query();
        if (userId != null){
            query.addCriteria(Criteria.where("userId").is(userId));
        }
        if (state != null){
            query.addCriteria(Criteria.where("state").is(state));
        }
        long count = mongoTemplate.count(query, Movement.class);
        query.with(Sort.by(Sort.Order.desc("created"))).skip((page - 1)*pagesize).limit(pagesize);
        List<Movement> movements = mongoTemplate.find(query, Movement.class);
        return new PageResult(page,pagesize,count,movements);
    }
}
