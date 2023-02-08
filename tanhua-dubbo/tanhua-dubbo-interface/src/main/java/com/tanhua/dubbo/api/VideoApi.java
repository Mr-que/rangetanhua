package com.tanhua.dubbo.api;

import com.tanhua.model.domain.PageResult;
import com.tanhua.model.mongo.Video;
import com.tanhua.model.vo.FocusUser;

public interface VideoApi {

    void save(Video video);

    PageResult findAll(Integer page, Integer pagesize);

    void saveFocusUser(FocusUser focusUser);

    void deleteFocusUser(Long userId, Long followUserId);

    PageResult findByUserId(Integer page, Integer pagesize, Long uid);
}
