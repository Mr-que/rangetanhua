package com.tanhua.autoconfig;

import com.tanhua.autoconfig.properties.AipFaceProperties;
import com.tanhua.autoconfig.properties.HuanxinProperties;
import com.tanhua.autoconfig.properties.OssProperties;
import com.tanhua.autoconfig.properties.SmsProperties;
import com.tanhua.autoconfig.template.AipFaceTemplate;
import com.tanhua.autoconfig.template.HuanXinTemplate;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.autoconfig.template.SmsTemplate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties({
        SmsProperties.class,
        OssProperties.class,
        AipFaceProperties.class,
        HuanxinProperties.class
})
public class TanhuaAutoConfiguration {

    @Bean
    public SmsTemplate smsTemplate(SmsProperties properties) {
        return new SmsTemplate(properties);
    }

    @Bean
    public OssTemplate ossTemplate(OssProperties properties) {
        return new OssTemplate(properties);
    }

    @Bean
    public AipFaceTemplate faceTemplate() {
        return new AipFaceTemplate();
    }

    @Bean
    public HuanXinTemplate huanXinTemplate(HuanxinProperties properties) {
        return new HuanXinTemplate(properties);
    }
}
