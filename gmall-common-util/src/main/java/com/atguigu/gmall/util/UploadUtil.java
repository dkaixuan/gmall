package com.atguigu.gmall.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class UploadUtil {

    public static String uploadImage(MultipartFile multipartFile) {
        //图片服务器地址
        String imgUrl = GmallConstant.IMG_URL;

        //上传图片到服务器
        //配置fdfsd额全局链接地址
        String path = UploadUtil.class.getResource("/tracker.conf").getPath();

        try {
            ClientGlobal.init(path);

        } catch (Exception e) {
            e.printStackTrace();
        }

        TrackerClient trackerClient = new TrackerClient();


        TrackerServer trackerServer = null;
        try {
            //获得一个trackerServer的实例
            trackerServer=trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //通过该tracker获得一个StorageClient 客户端
        StorageClient storageClient = new StorageClient(trackerServer, null);
        try {
            //获得上传得二进制对象
            byte[] bytes = multipartFile.getBytes();
            //获取原始文件名 工具类截取扩展名
            String originalFilename = multipartFile.getOriginalFilename();
            String extendsionName = GmallUtils.generateExtensionName(originalFilename);
            String[] uploadFileInfos = storageClient.upload_file(bytes, extendsionName, null);


            for (String uploadFileInfo : uploadFileInfos) {
                imgUrl += "/" + uploadFileInfo;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return imgUrl;
    }

}
