package com.itheima.test;

import com.tanhua.autoconfig.template.AipFaceTemplate;
import com.tanhua.server.AppServerApplication;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppServerApplication.class)
public class FaceTest {

    @Autowired
    private AipFaceTemplate aipFaceTemplate;

    @Test
    public void detectTest() {
        String imgUrl = "https://anko-th001.oss-cn-hangzhou.aliyuncs.com/2022/09/22/01763ec2-8eba-4da2-beb4-2f0ff9553f82.png";
        boolean detect = aipFaceTemplate.detect(imgUrl);
        System.out.println(detect);

    }
}
