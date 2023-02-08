package com.tanhua.dubbo.api;

import com.tanhua.model.domain.PageResult;
import com.tanhua.model.mongo.Movement;

import java.util.List;

public interface MovementApi {

    void publish(Movement movement);

    PageResult findByUserId(Long userId, Integer page, Integer pagesize);

    List<Movement> findFriendMovements(Integer page, Integer pagesize, Long userId);

    List<Movement> randomMovements(Integer pagesize);

    List<Movement> findByPids(List<Long> pids);

    Movement findById(String movementId);

    PageResult findByUserId(Long userId, Integer state, Integer page, Integer pagesize);
}