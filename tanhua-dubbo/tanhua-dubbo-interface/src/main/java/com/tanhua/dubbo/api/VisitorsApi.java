package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Visitors;

import java.util.List;

public interface VisitorsApi {

    /**
     * 保存访客数据
     * @return
     */
    void save(Visitors visitors);

    /**
     * 查询我的访客数据，存在2种情况：
     * 1. 我没有看过我的访客数据，返回前5个访客信息
     * 2. 之前看过我的访客，从上一次查看的时间点往后查询5个访客数据
     */
    List<Visitors> queryMyVisitors(Long date, Long userId);
}