package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Comment;
import com.tanhua.model.mongo.CommentType;

import java.util.List;

public interface CommentApi {
    List<Comment> findComments(String movementId, CommentType comment, Integer page, Integer pagesize);

    Integer save(Comment cm);

    Boolean hasComment(String movementId, Long userId, CommentType like);

    Integer delete(Comment cm);

    Integer saveComment(Comment cm);

    Integer deleteComment(Comment cm);
}
