package com.personal.project.authservice.util;


import cn.hutool.crypto.SecureUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;

public class PasswordUtil {

    public static Pair<String, String> generateUserPassword(String origin) {
        String salt = RandomStringUtils.random(16, true, true);
        String hash = SecureUtil.md5(origin + salt);

        return Pair.of(salt, hash);
    }

    public static String generateUserPassword(String origin, String salt) {
        return SecureUtil.md5(origin + salt);
    }
}
