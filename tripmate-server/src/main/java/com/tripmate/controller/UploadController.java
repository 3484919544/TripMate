package com.tripmate.controller;

import com.tripmate.common.Result;
import com.tripmate.security.UserContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传:目前只用于头像
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

  @Value("${app.upload-dir:uploads}")
  private String uploadDir;

  /** 上传头像,返回可访问的 URL(相对路径 /uploads/xxx.jpg) */
  @PostMapping("/avatar")
  public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return Result.error(400, "请选择文件");
    }
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      return Result.error(400, "只能上传图片");
    }
    // 限制 2MB
    if (file.getSize() > 2 * 1024 * 1024) {
      return Result.error(400, "图片不能超过 2MB");
    }

    // 确保目录存在
    File dir = new File(uploadDir);
    if (!dir.isAbsolute()) {
      dir = new File(System.getProperty("user.dir"), uploadDir);
    }
    if (!dir.exists()) {
      dir.mkdirs();
    }

    // 生成文件名:userId_uuid.ext
    String origName = file.getOriginalFilename();
    String ext = "";
    if (origName != null && origName.contains(".")) {
      ext = origName.substring(origName.lastIndexOf("."));
    }
    Long uid = UserContext.getUserId();
    String fileName = uid + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
    File dest = new File(dir, fileName);
    try {
      file.transferTo(dest);
    } catch (IOException e) {
      return Result.error(500, "保存失败: " + e.getMessage());
    }

    Map<String, String> data = new HashMap<>();
    data.put("url", "/uploads/" + fileName);
    return Result.ok(data);
  }
}
