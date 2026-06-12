package com.DB.DBmarket.pojo.storage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoredObject {
    private String key;
    private String url;
    private Long size;
    private String contentType;
}
