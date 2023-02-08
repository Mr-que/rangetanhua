package com.itheima.test;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.server.AppServerApplication;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppServerApplication.class)
public class OssTest {

    @Autowired
    private OssTemplate ossTemplate;

    @Test
    public void testTemplate() throws FileNotFoundException {
        String path = "C:\\Users\\25222\\Pictures\\Saved Pictures\\1.png";
        FileInputStream inputStream = new FileInputStream(new File(path));
        String upload = ossTemplate.upload(path, inputStream);
        System.out.println(upload);
    }


    public void testOss() throws FileNotFoundException {
        String path = "C:\\Users\\25222\\Pictures\\ll\\backiee-187531.jpg";
        FileInputStream inputStream = new FileInputStream(new File(path));
        String filename = new SimpleDateFormat("yyyy/MM/dd").format(new Date())
                + "/" + UUID.randomUUID().toString() + path.substring(path.lastIndexOf("."));

        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = "https://oss-cn-hangzhou.aliyuncs.com";
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = "LTAI5t8ZFgCtAfWS7wpzhmtd";
        String accessKeySecret = "GiYdlBpPeSOUKAsXy2hCWbmsuA4etD";
        // 填写Bucket名称，例如examplebucket。
        String bucketName = "anko-th001";
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        ossClient.putObject(bucketName, filename, inputStream);
        ossClient.shutdown();

    }
}
