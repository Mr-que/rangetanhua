package com.tanhua.server.controller;

import com.tanhua.model.domain.PageResult;
import com.tanhua.model.mongo.Movement;
import com.tanhua.model.vo.MovementsVo;
import com.tanhua.model.vo.VisitorsVo;
import com.tanhua.server.service.CommentService;
import com.tanhua.server.service.MovementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/movements")
public class MovementController {

    @Autowired
    private MovementService movementService;

    @Autowired
    private CommentService commentService;
    /**
     * 发布动态
     */
    @PostMapping
    public ResponseEntity movements(Movement movement, MultipartFile imageContent[]) throws IOException {
        movementService.publishMovement(movement,imageContent);
        return ResponseEntity.ok(null);
    }

    /**
     * 查询我的动态
     */
    @GetMapping("/all")
    public ResponseEntity findByUserId(Long userId,
                                       @RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult pr = movementService.findByUserId(userId,page,pagesize);
        return ResponseEntity.ok(pr);
    }

    /**
     * 查询好友动态
     */
    @GetMapping
    public ResponseEntity movements(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult pr = movementService.findFriendMovements(page,pagesize);
        return ResponseEntity.ok(pr);
    }

    /**
     * 推荐动态
     */
    @GetMapping("/recommend")
    public ResponseEntity recommend(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult pr = movementService.findRecommendMovements(page,pagesize);
        return ResponseEntity.ok(pr);
    }

    /**
     * 根据id查询动态
     */
    @GetMapping("/{id}")
    public ResponseEntity findById(@PathVariable("id") String movementId) {
        MovementsVo mvo = movementService.findMovementById(movementId);
        return ResponseEntity.ok(mvo);
    }

    /**
     * 点赞
     */
    @GetMapping("/{id}/like")
    public ResponseEntity like(@PathVariable("id") String movementId) {
        Integer likeCount = commentService.likeMovement(movementId);
        return ResponseEntity.ok(likeCount);
    }

    /**
     * 取消点赞
     */
    @GetMapping("/{id}/dislike")
    public ResponseEntity dislike(@PathVariable("id") String movementId) {
        Integer likeCount = commentService.dislikeMovement(movementId);
        return ResponseEntity.ok(likeCount);
    }

    /**
     * 喜欢
     */
    @GetMapping("/{id}/love")
    public ResponseEntity love(@PathVariable("id") String movementId) {
        Integer likeCount = commentService.loveMovement(movementId);
        return ResponseEntity.ok(likeCount);
    }

    /**
     * 取消喜欢
     */
    @GetMapping("/{id}/unlove")
    public ResponseEntity unlove(@PathVariable("id") String movementId) {
        Integer likeCount = commentService.disloveMovement(movementId);
        return ResponseEntity.ok(likeCount);
    }

    /**
     * 谁看过我
     */
    @GetMapping("visitors")
    public ResponseEntity queryVisitorsList(){
        List<VisitorsVo> list = movementService.queryVisitorsList();
        return ResponseEntity.ok(list);
    }
}
