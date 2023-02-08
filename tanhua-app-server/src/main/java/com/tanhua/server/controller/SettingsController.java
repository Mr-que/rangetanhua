package com.tanhua.server.controller;

import com.tanhua.model.domain.PageResult;
import com.tanhua.model.vo.SettingsVo;
import com.tanhua.server.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    /**
     * 返回设置详情页面
     * @return
     */
    @GetMapping("/settings")
    public ResponseEntity settings() {
        SettingsVo vo = settingsService.settings();
        return ResponseEntity.ok(vo);
    }

    /**
     * 设置陌生人问题
     */
    @PostMapping("/questions")
    public ResponseEntity questions(@RequestBody Map map) {
        String content = (String) map.get("content");
        settingsService.saveQuestion(content);
        return ResponseEntity.ok(null);
    }

    /**
     * 通知设置
     */
    @PostMapping("/notifications/setting")
    public ResponseEntity notifications(@RequestBody Map map) {
        settingsService.saveSettings(map);
        return ResponseEntity.ok(null);
    }

    /**
     * 分页查询黑名单列表
     */
    @GetMapping("/blacklist")
    public ResponseEntity backlist(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        PageResult pr = settingsService.backlist(page,size);
        return ResponseEntity.ok(pr);
    }

    /**
     * 取消黑名单
     */
    @DeleteMapping("/blacklist/{uid}")
    public ResponseEntity deleteBlackList(@PathVariable("uid") Long blackUserId) {
        settingsService.deleteBlackList(blackUserId);
        return ResponseEntity.ok(null);
    }
 }
