package com.tanhua.server.controller;

import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.vo.UserInfoVo;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.service.UserInfoService;
import com.tanhua.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserService userService;

    /**
     * 查询用户资料
     */
    @GetMapping
    public ResponseEntity users(Long userID, @RequestHeader("Authorization") String token) {
        if (userID == null)
            userID = UserHolder.getUserId();

        UserInfoVo userInfo = userInfoService.findById(userID);
        return ResponseEntity.ok(userInfo);
    }

    /**
     * 更新用户资料
     */
    @PutMapping
    public ResponseEntity updateUserInfo(@RequestBody UserInfo userInfo, @RequestHeader("Authorization") String token) {

        userInfo.setId(UserHolder.getUserId());
        userInfoService.update(userInfo);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/header")
    public ResponseEntity headUp(MultipartFile headPhoto,@RequestHeader("Authorization") String token) throws IOException {

        userInfoService.updateHead(headPhoto, UserHolder.getUserId());
        return ResponseEntity.ok(null);
    }

    @PostMapping("/phone/sendVerificationCode")
    public ResponseEntity sendVerificationCode(@RequestHeader("Authorization") String token) {
        String phone = UserHolder.getUserMobile();
        userService.sendMsg(phone);
        return ResponseEntity.ok(null);
    }


}
