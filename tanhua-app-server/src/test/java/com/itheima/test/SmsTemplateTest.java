package com.itheima.test;

import com.tanhua.autoconfig.template.SmsTemplate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = AppServerApplication.class)
public class SmsTemplateTest {

    //注入
    @Autowired
    private SmsTemplate smsTemplate;

    //测试
    @Test
    public void testSendSms() {
        smsTemplate.sendSms("18618412321","4567");
    }
}
