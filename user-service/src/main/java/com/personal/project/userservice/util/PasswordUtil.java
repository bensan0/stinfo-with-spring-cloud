package com.personal.project.userservice.util;


import cn.hutool.core.util.HexUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;

public class PasswordUtil {

    public static Pair<String, String> generateUserPassword(String origin) {
        String salt = RandomStringUtils.random(16, true, true);
        String hash = HexUtil.encodeHexStr(origin + salt);

        return Pair.of(salt, hash);
    }

    public static String generateUserPassword(String origin, String salt) {
        return HexUtil.encodeHexStr(origin + salt);
    }
}
