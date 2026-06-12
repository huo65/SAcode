package com.DB.DBmarket.service;

import com.DB.DBmarket.pojo.storage.StoredObject;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ObjectStorageService {
    StoredObject store(MultipartFile file, String category) throws IOException;
}
