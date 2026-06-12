package com.DB.DBmarket.controller;

import com.DB.DBmarket.pojo.Result;
import com.DB.DBmarket.pojo.storage.StoredObject;
import com.DB.DBmarket.service.ObjectStorageService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/storage")
public class StorageController {
    @Resource
    private ObjectStorageService objectStorageService;

    @PostMapping("/upload")
    public Result upload(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(defaultValue = "common") String category
    ) {
        if (files == null || files.isEmpty()) {
            return Result.error("Please select at least one file.");
        }
        try {
            List<StoredObject> storedObjects = files.stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(file -> storeSingleFile(file, category))
                    .collect(Collectors.toList());
            if (storedObjects.isEmpty()) {
                return Result.error("Please select at least one file.");
            }
            Map<String, Object> data = new HashMap<>();
            data.put("files", storedObjects);
            data.put("urls", storedObjects.stream().map(StoredObject::getUrl).collect(Collectors.toList()));
            return Result.success(data);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    private StoredObject storeSingleFile(MultipartFile file, String category) {
        if (!StringUtils.hasText(file.getOriginalFilename())) {
            throw new IllegalArgumentException("File name is required.");
        }
        try {
            return objectStorageService.store(file, category);
        } catch (IOException e) {
            throw new IllegalArgumentException("Store file failed.");
        }
    }
}
