package com.tanhua.dubbo.api;

public interface UserLikeApi {

    //保存或者更新数据
    Boolean saveOrUpdate(Long userId,Long likeUserId,boolean isLike);
}
