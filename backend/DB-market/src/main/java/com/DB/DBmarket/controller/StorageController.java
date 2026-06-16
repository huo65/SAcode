package com.DB.DBmarket.controller;

import com.DB.DBmarket.pojo.Result;
import com.DB.DBmarket.pojo.storage.StoredObject;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.pojo.utils.CurrentUserHolder;
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
        CurrentUser currentUser = CurrentUserHolder.require();
        if (!isCategoryAllowed(currentUser, category)) {
            return Result.error("No permission to upload files for this category.");
        }
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
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must not exceed 5MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed.");
        }
        try {
            return objectStorageService.store(file, category);
        } catch (IOException e) {
            throw new IllegalArgumentException("Store file failed.");
        }
    }

    private boolean isCategoryAllowed(CurrentUser currentUser, String category) {
        if (currentUser == null) {
            return false;
        }
        if (currentUser.isAdmin()) {
            return true;
        }
        String normalized = !StringUtils.hasText(category) ? "common" : category.trim().toLowerCase();
        if (currentUser.isMerchant()) {
            return "common".equals(normalized) || "avatar".equals(normalized)
                    || "product".equals(normalized) || "restaurant".equals(normalized);
        }
        return "common".equals(normalized) || "avatar".equals(normalized);
    }
}
