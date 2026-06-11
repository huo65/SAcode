package com.DB.DBmarket.pojo.utils;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {
    // 解析字符串为 List<String>
    public static List<String> parseStringToList(String str) {
        List<String> list = new ArrayList<>();
        if (str != null && str.length() > 2) {
            // 去掉字符串的首尾括号 "[", "]"
            String[] strArray = str.substring(1, str.length() - 1).split(",");
            for (String s : strArray) {
                // 去掉可能的空格，并将字符串添加到列表中
                list.add(s.trim());
            }
        }
        return list;
    }
}
