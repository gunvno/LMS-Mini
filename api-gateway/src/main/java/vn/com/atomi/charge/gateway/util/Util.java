package vn.com.atomi.charge.gateway.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

public class Util {
    private static final String NUMBER_SOURCE = "0123456789";
    private static final String CHAR_SOURCE = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER_SOURCE = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String genRandomNumber(int length) {
        return genRandomString(length, NUMBER_SOURCE);
    }

    public static String genRandomString(int length) {
        return genRandomString(length, CHAR_SOURCE);
    }

    public static String genRandomStringUpperCase(int length) {
        return genRandomString(length, CHAR_UPPER_SOURCE);
    }

    private static String genRandomString(int length, String source) {
        SecureRandom rnd = new SecureRandom();

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            sb.append(source.charAt(rnd.nextInt(source.length())));
        return sb.toString();
    }

    public static Boolean isNotNull(String string) {
        if (string == null)
            return false;
        string = string.trim();
        return !string.isEmpty() && !string.equalsIgnoreCase("null");
    }

    public static String removeAccent(String s) {
        if (!isNotNull(s))
            return s;
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }

    public static String toLowerCase(String s) {
        if (!isNotNull(s))
            return s;
        return s.toLowerCase();
    }

    public static String removeAccentAndConvertLowerCase(String s) {
        if (!isNotNull(s))
            return s;
        return removeAccent(s).toLowerCase();
    }

    public static String beautyError(Exception e) {
        return ExceptionUtils.getStackTrace(e);
    }

    public static Boolean isContainEmpty(List<String> listInfo) {
        return listInfo.stream().anyMatch(StringUtils::isEmpty);
    }

    public static Boolean isContainAlphabet(String s) {
        return s.matches(".*[a-zA-Z]+.*");
    }

    public static String removeSpace(String input) {
        return input.replaceAll("\\s+", "");
    }

    public static String encodeBase64(String input) {
        byte[] encodedBytes = Base64.getEncoder().encode(input.getBytes(StandardCharsets.UTF_8));
        return new String(encodedBytes, StandardCharsets.UTF_8);
    }

    public static String decodeBase64ToString(String input) {
        byte[] decodedBytes = Base64.getDecoder().decode(input);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    public static <T> T decodeBase64ToObject(String input, Class<T> typeParameterClass) {
        String decodedString = decodeBase64ToString(input);
        return JsonUtil.parseJsonToObject(decodedString, typeParameterClass);
    }

    public static <T> List<T> convertArrayToList(T[] array) {
        return new ArrayList<>(Arrays.asList(array));
    }

    public static <T> T[] listToArray(List<T> list, Class<T> clazz) {
        @SuppressWarnings("unchecked")
        T[] array = (T[]) Array.newInstance(clazz, list.size());
        return list.toArray(array);
    }

}
