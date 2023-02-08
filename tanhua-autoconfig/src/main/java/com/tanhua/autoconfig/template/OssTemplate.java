package com.tanhua.autoconfig.template;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.tanhua.autoconfig.properties.OssProperties;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class OssTemplate {

    private OssProperties properties;

    public OssTemplate(OssProperties properties){
        this.properties = properties;
    }

    /**
     * 文件上传
     *   1：文件名称
     *   2：输入流
     */
    public String upload(String path, InputStream is) {

        String filename = new SimpleDateFormat("yyyy/MM/dd").format(new Date())
                + "/" + UUID.randomUUID().toString() + path.substring(path.lastIndexOf("."));

        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = properties.getEndpoint();
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = properties.getAccessKey();
        String accessKeySecret = properties.getSecret();
        // 填写Bucket名称，例如examplebucket。
        String bucketName = properties.getBucketName();
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        ossClient.putObject(bucketName, filename, is);

        ossClient.shutdown();
        String url = properties.getUrl() + "/" + filename;
        return url;
    }
}
