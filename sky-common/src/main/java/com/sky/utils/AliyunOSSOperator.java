package com.sky.utils;


import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.sky.properties.AliyunOSSProperties;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


@Component


public class AliyunOSSOperator {

     AliyunOSSProperties aliyunOSSProperties;
     public AliyunOSSOperator(AliyunOSSProperties aliyunOSSProperties) {
        this.aliyunOSSProperties = aliyunOSSProperties;
    }

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    /**
     * 文件上传接口开发
     * @param content 文件字节内容
     * @param originalFilename 原始文件名
     * @return 上传后的文件完整路径
     * @throws Exception 异常
     */
    public String upload(byte[] content, String originalFilename) throws Exception {
        String endpoint = aliyunOSSProperties.getEndpoint();
        String bucketName = aliyunOSSProperties.getBucketName();
        String region = aliyunOSSProperties.getRegion();
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("文件内容不能为空");
        }

        if (content.length > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过10MB，当前大小：" + (content.length / 1024 / 1024) + "MB");
        }
        String accessKeyId = System.getenv("OSS_ACCESS_KEY_ID");
        String accessKeySecret = System.getenv("OSS_ACCESS_KEY_SECRET");

        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));

        String newFileName = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = dir + "/" + newFileName;

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content));

            return endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + objectName;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
