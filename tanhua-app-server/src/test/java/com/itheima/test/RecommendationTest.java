package com.itheima.test;

import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.server.AppServerApplication;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppServerApplication.class)
public class RecommendationTest {

    @DubboReference
    private UserInfoApi userApi;

    @Test
    public void testFindByMobile() {
        List<Long> list = new ArrayList<>();
        list.add(1l);
        list.add(2l);
        list.add(3l);
        Map<Long, UserInfo> map = userApi.findByIds(list, null);
        map.forEach((k,v) -> System.out.println(map.get(k)));
    }
}
