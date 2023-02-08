package com.tanhua.server.controller;

import com.tanhua.model.domain.PageResult;
import com.tanhua.server.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    //分页查询评论列表
    @GetMapping
    public ResponseEntity findComments(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer pagesize,
                                       String movementId) {
        PageResult pr = commentService.findComments(movementId,page,pagesize);
        return ResponseEntity.ok(pr);
    }

    /**
     * 发布评论
     */
    @PostMapping
    public ResponseEntity saveComments(@RequestBody Map map) {
        String movementId = (String) map.get("movementId");     //获取评论的动态id
        String comment = (String) map.get("comment");           //获取评论详情
        commentService.saveComments(movementId,comment);
        return ResponseEntity.ok(null);
    }

    /**
     * 点赞
     */
    @GetMapping("/{id}/like")
    public ResponseEntity like(@PathVariable("id") String commentId) {
        Integer likeCount = commentService.likeComment(commentId);
        return ResponseEntity.ok(likeCount);
    }

    /**
     * 取消点赞
     */
    @GetMapping("/{id}/dislike")
    public ResponseEntity dislike(@PathVariable("id") String commentId) {
        Integer likeCount = commentService.dislikeComment(commentId);
        return ResponseEntity.ok(likeCount);
    }
}
