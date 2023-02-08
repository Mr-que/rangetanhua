package com.tanhua.server.controller;

import com.tanhua.model.domain.PageResult;
import com.tanhua.server.service.SmallVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/smallVideos")
public class SmallVideoController {

    @Autowired
    private SmallVideoService smallVideoService;

    @PostMapping()
    public ResponseEntity saveVideos(MultipartFile videoThumbnail,MultipartFile videoFile) throws IOException {
        smallVideoService.saveVideos(videoThumbnail,videoFile);
        return ResponseEntity.ok(null);
    }

    /**
     * 视频列表
     */
    @GetMapping
    public ResponseEntity queryVideoList(@RequestParam(defaultValue = "1")  Integer page,
                                         @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult result = smallVideoService.queryVideoList(page, pagesize);
        return ResponseEntity.ok(result);
    }

    /**
     * 关注视频作者
     */
    @PostMapping("/{id}/userFocus")
    public ResponseEntity userFocus(@PathVariable("id") Long followUserId) {
        smallVideoService.userFocus(followUserId);
        return ResponseEntity.ok(null);
    }

    /**
     * 取消关注视频作者
     */
    @PostMapping("/{id}/userUnFocus")
    public ResponseEntity userUnFocus(@PathVariable("id") Long followUserId) {
        smallVideoService.userUnFocus(followUserId);
        return ResponseEntity.ok(null);
    }
}
