package com.DB.DBmarket.pojo.utils;

import java.util.Random;

public class RandomIdGenerator {

    public static String getRandomId() {
        Random random = new Random();
        // 生成八位随机数
        int randomNumberInt = random.nextInt(100000000);
        // 将随机数转换为字符串

        return String.format("%08d", randomNumberInt);
    }

}
