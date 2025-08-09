package com.mengnankk.file.service.impl;

import cn.hutool.core.lang.UUID;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.lang.reflect.Field;

@Service
@RequiredArgsConstructor
public class MinioServiceImpl {
    private final MinioClient minioClient;

    @Value("${minio.bucket-name:forum}")
    private String buctetName;

    @Value("${minio.endpoint}")
    private String endpoint;

    /**
     * 上传文件
     * @param file
     * @return
     */

    public String uploadFile(MultipartFile file){
        try {
            String fileName = UUID.randomUUID().toString()+"_"+file.getOriginalFilename();
            String objectName = "/upload/"+fileName;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(buctetName)
                            .object(objectName)
                            .stream(file.getInputStream(),file.getSize(),file.getSize())
                            .contentType(file.getContentType())
                            .build()
            );

            //TODO 保存文件信息到数据库，在上传之后

            return endpoint+"/"+buctetName+"/"+objectName;
        }catch (Exception e){
            throw  new RuntimeException("文件上传失败",e);
        }
    }

    /**
     * 下载文件
     * @param objectName
     * @return
     */
    public InputStream downloadFile(String objectName){
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(buctetName)
                            .object(objectName)
                            .build()
            );
        }catch (Exception e){
            throw new RuntimeException("文件下载失败",e);
        }
    }
}
