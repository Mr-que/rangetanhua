package com.tanhua.autoconfig.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("tanhua.huanxin")
public class HuanxinProperties {
    private String appkey;
    private String clientId;
    private String clientSecret;
}
