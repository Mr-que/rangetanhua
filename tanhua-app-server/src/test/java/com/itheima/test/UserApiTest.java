package com.itheima.test;

import com.tanhua.dubbo.api.UserApi;
import com.tanhua.model.domain.User;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.Test;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = AppServerApplication.class)
public class UserApiTest {

    @DubboReference
    private UserApi userApi;

    @Test
    public void testFindByMobile() {
        User user = userApi.findByMobile("13800138000");
        System.out.println(user);
    }
}
