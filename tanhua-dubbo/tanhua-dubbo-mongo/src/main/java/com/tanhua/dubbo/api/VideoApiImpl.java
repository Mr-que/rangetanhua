package com.tanhua.dubbo.api;

import com.tanhua.dubbo.utils.IdWorker;
import com.tanhua.model.domain.PageResult;
import com.tanhua.model.mongo.Video;
import com.tanhua.model.vo.FocusUser;
import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@DubboService
public class VideoApiImpl implements VideoApi{

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IdWorker idWorker;

    @Override
    public void save(Video video) {
        video.setVid(idWorker.getNextId("video"));
        video.setCreated(System.currentTimeMillis());
        video.setId(ObjectId.get());
        mongoTemplate.save(video);
    }

    @Override
    public PageResult findAll(Integer page, Integer pagesize) {
        //查询视频总数
        long count = mongoTemplate.count(new Query(), Video.class);
        //查询数据列表
        Query query = new Query().skip((page - 1) * pagesize).limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        List<Video> list = mongoTemplate.find(query, Video.class);
        return new PageResult(page,pagesize,count,list);
    }

    @Override
    public void saveFocusUser(FocusUser focusUser) {
        focusUser.setId(ObjectId.get());
        focusUser.setCreated(System.currentTimeMillis());
        mongoTemplate.save(focusUser);
    }

    @Override
    public void deleteFocusUser(Long userId, Long followUserId) {
        Criteria criteria = Criteria.where("userId").is(userId).and("followUserId").is(followUserId);
        mongoTemplate.remove(Query.query(criteria), FocusUser.class);
    }

    @Override
    public PageResult findByUserId(Integer page, Integer pagesize, Long userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        //查询视频总数
        long count = mongoTemplate.count(query, Video.class);
        //查询数据列表
        query.skip((page - 1) * pagesize).limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        List<Video> list = mongoTemplate.find(query, Video.class);
        return new PageResult(page,pagesize,count,list);
    }
}
