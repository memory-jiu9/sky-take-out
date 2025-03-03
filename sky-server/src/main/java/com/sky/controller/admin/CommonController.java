package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api(tags = "通用接口-CommonController")
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    @ApiOperation("上传照片")
    public Result uploadImg(MultipartFile file) {
        log.info("接收到文件：{}", file);
        // 获取文件名
        String filename = file.getOriginalFilename();
        // 获取后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        // 重命名
        String objectName = UUID.randomUUID().toString() + suffix;
        // 上传到阿里云平台
        String url = null;
        try {
            url = aliOssUtil.upload(file.getBytes(), objectName);
        } catch (IOException e) {
            log.info("文件上传失败：{}", e);
        }
        // 返回网络路径
        return Result.success(url);
    }

}
