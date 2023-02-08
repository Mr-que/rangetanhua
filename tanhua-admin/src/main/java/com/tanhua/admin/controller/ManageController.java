package com.tanhua.admin.controller;

import com.tanhua.admin.service.ManagerService;
import com.tanhua.model.domain.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/manage")
public class ManageController {

    @Autowired
    private ManagerService managerService;

    @GetMapping("/users")
    public ResponseEntity users(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult result = managerService.findAllUsers(page,pagesize);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据id查询用户详情
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity findById(@PathVariable("userId") Long userId) {
        return managerService.findById(userId);
    }

    /**
     * 查询指定用户发布的所有视频列表
     */
    @GetMapping("/videos")
    public ResponseEntity videos(@RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer pagesize,
                                 Long userId ) {
        PageResult result = managerService.findAllVideos(page,pagesize,userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/messages")
    public ResponseEntity messages(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pagesize,
                                   Long userId,Integer state ) {
        PageResult result = managerService.findAllMovements(page,pagesize,userId,state);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/users/freeze")
    public ResponseEntity freeze(@RequestBody Map param){
        Map map = managerService.userFreeze(param);
        return ResponseEntity.ok(map);
    }

    @PostMapping("/users/unfreeze")
    public ResponseEntity unfreeze(@RequestBody Map param){
        Map map = managerService.userUnFreeze(param);
        return ResponseEntity.ok(map);
    }
}
