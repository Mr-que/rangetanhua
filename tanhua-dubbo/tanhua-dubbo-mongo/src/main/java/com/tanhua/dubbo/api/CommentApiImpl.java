package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Comment;
import com.tanhua.model.mongo.CommentType;
import com.tanhua.model.mongo.Movement;
import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@DubboService
public class CommentApiImpl implements CommentApi{

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<Comment> findComments(String movementId, CommentType comment, Integer page, Integer pagesize) {
        Query query = Query.query(Criteria.where("publishId").is(new ObjectId(movementId))
                .and("commentType").is(comment.getType()))
                .skip((page - 1) * pagesize)
                .limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        return mongoTemplate.find(query, Comment.class);
    }

    //发布评论，并获取评论数量
    @Override
    public Integer save(Comment cm) {
        //1.查询动态 publishId是发布动态人id
        Movement movement = mongoTemplate.findById(cm.getPublishId(), Movement.class);
        //2.向comment对象设置被评论人属性
        if (movement != null){
            cm.setPublishUserId(movement.getUserId());
        }
        mongoTemplate.save(cm);
        //3.更新动态表中的对应字段
        Query query = Query.query(Criteria.where("id").is(cm.getPublishId()));
        Update update = new Update();
        if (cm.getCommentType() == CommentType.LIKE.getType()){
            update.inc("likeCount",1);
        }else if (cm.getCommentType() == CommentType.COMMENT.getType()){
            update.inc("commentCount",1);
        }else {
            update.inc("loveCount",1);
        }
        //设置更新参数
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);//获取更新后的最新数据
        Movement modify = mongoTemplate.findAndModify(query, update, options, Movement.class);
        //获取最新的评论数量，并返回
        Integer count = modify.statisCount(cm.getCommentType());
        return count;
    }

    @Override
    public Boolean hasComment(String movementId, Long userId, CommentType like) {
        //找到用户id和评论id表中的like 判断是否为1
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("publishId").is(new ObjectId(movementId))
                .and("commentType").is(like.getType());
        Query query = Query.query(criteria);
        return mongoTemplate.exists(query, Comment.class);
    }

    @Override
    public Integer delete(Comment cm) {
        //找到用户id和评论id表中的like 判断是否为1 删除Comment表数据
        Criteria criteria = Criteria.where("userId").is(cm.getUserId())
                .and("publishId").is(cm.getPublishId())
                .and("commentType").is(cm.getCommentType());
        Query query = Query.query(criteria);
        mongoTemplate.remove(query,Comment.class);
        //2、修改动态表中的总数量
        Query movementQuery = Query.query(Criteria.where("id").is(cm.getPublishId()));
        Update update = new Update();
        if(cm.getCommentType() == CommentType.LIKE.getType()) {
            update.inc("likeCount",-1);
        }else if (cm.getCommentType() == CommentType.COMMENT.getType()){
            update.inc("commentCount",-1);
        }else {
            update.inc("loveCount",-1);
        }
        //设置更新参数
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true) ;//获取更新后的最新数据
        Movement modify = mongoTemplate.findAndModify(movementQuery, update, options, Movement.class);
        return modify.statisCount(cm.getCommentType());
    }

    @Override
    public Integer saveComment(Comment cm) {
        Query query = Query.query(Criteria.where("id").is(cm.getId()));
        Update update = new Update();
        update.inc("likeCount",1);
        //设置更新参数
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);//获取更新后的最新数据
        Comment modify = mongoTemplate.findAndModify(query, update, options, Comment.class);
        //获取最新的评论数量，并返回
        Integer count = modify.statisCount();
        return count;
    }

    @Override
    public Integer deleteComment(Comment cm) {
        //找到用户id和评论id表中的like 判断是否为1 删除Comment表数据
        Query query = Query.query(Criteria.where("id").is(cm.getId()));
        Update update = new Update();
        update.inc("likeCount",-1);
        //设置更新参数
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true) ;//获取更新后的最新数据
        Comment modify = mongoTemplate.findAndModify(query, update, options, Comment.class);
        return modify.statisCount();
    }
}
