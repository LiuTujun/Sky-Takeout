package com.sky.controller.admin;

import cn.hutool.core.lang.UUID;
import com.sky.constant.MessageConstant;
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

/**
 * 通用接口
 */

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传...");

        try {
            String originalFileName = file.getOriginalFilename();
            String exten = originalFileName.substring(originalFileName.lastIndexOf('.'));
            // 文件名
            String fileName = UUID.randomUUID() + exten;

            //文件请求路径
            String filePath = aliOssUtil.upload(file.getBytes(), fileName);

            return Result.success(filePath);
        } catch (IOException e) {
            log.info("文件上传失败");
        }

        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
