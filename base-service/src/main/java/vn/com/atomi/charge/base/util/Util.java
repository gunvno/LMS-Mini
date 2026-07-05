package vn.com.atomi.charge.base.util;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.com.atomi.charge.base.model.enums.CustomHeader;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Util {
    private static final String NUMBER_SOURCE = "0123456789";
    private static final String CHAR_SOURCE = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER_SOURCE = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String PASSWORD_VALID_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,}$";
    private static final String PASSWORD_VALID_SHA256_HEX = "^[a-fA-F0-9]{64}$";

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

    public static String removeDupSpace(String input) {
        return input.replaceAll("\\s+", " ").trim().replaceAll("\u200B", "").replaceAll("\u00a0", "");
    }

    public static String encodeBase64(String input) {
        byte[] encodedBytes = Base64.getEncoder().encode(input.getBytes(StandardCharsets.UTF_8));
        return new String(encodedBytes, StandardCharsets.UTF_8);
    }

    public static String encodeBase64(Object input) {
        String inputString = JsonUtil.convertObjectToJson(input);
        return encodeBase64(inputString);
    }

    public static String decodeBase64ToString(String input) {
        byte[] decodedBytes = Base64.getDecoder().decode(input);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    public static <T> T decodeBase64ToObject(String input, Class<T> typeParameterClass) {
        String decodedString = decodeBase64ToString(input);
        return JsonUtil.convertJsonToObject(decodedString, typeParameterClass);
    }

    public static <T> List<T> convertArrayToList(T[] array) {
        return new ArrayList<>(Arrays.asList(array));
    }

    public static <T> T[] listToArray(List<T> list, Class<T> clazz) {
        @SuppressWarnings("unchecked")
        T[] array = (T[]) Array.newInstance(clazz, list.size());
        return list.toArray(array);
    }

    public static boolean isValidPassword(String password) {
        return password.matches(PASSWORD_VALID_SHA256_HEX);
    }

    public static String getHeaderValue(String headerName) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return null; // No request context available
        }

        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        return request.getHeader(headerName);
    }

    public static boolean isContainSpecialCharacter(String input) {
        if (StringUtils.isBlank(input)) {
            return true;
        }
        String pattern = "^[A-Za-z0-9 ]+$";
        return !input.matches(pattern);
    }

    public static String formatWithZeros(long number, int totalLength) {
        return String.format("%0" + totalLength + "d", number);
    }

    public static boolean compareString(String str1, String str2) {
        return (str1 == null ? str2 == null : str1.equalsIgnoreCase(str2));
    }

    public static <T> T getFeignExceptionError(Exception exception, Class<T> typeParameterClass) {
        if (!(exception instanceof FeignException feignEx)) {
            return null;
        }

        String message = feignEx.contentUTF8();
        if (StringUtils.isBlank(message)) {
            return null;
        }

        if (typeParameterClass == String.class) {
            return typeParameterClass.cast(message);
        }

        return JsonUtil.convertJsonToObject(message, typeParameterClass);
    }

    public static long calculateDateDiff(Date startDate, Date endDate, TimeUnit unit) {
        long diffInMilliSecond = endDate.getTime() - startDate.getTime();
        return unit.convert(diffInMilliSecond, TimeUnit.MILLISECONDS);
    }

    public static String beautyError(Throwable e) {
        return ExceptionUtils.getStackTrace(e);
    }

    public static long randomWithRange(long min, long max) {
        // Using ThreadLocalRandom to generate a bounded long value (inclusive)
        return ThreadLocalRandom.current().nextLong(min, max + 1);
    }

    public static boolean isProdEnv(String env) {
        return env.contains("prod");
    }

    public static boolean isDevTestEnvironment(String env) {
        return env.contains("dev") || env.contains("test") || env.contains("local");
    }

    public static String convertBetweenPattern(String dateTime, String oldPatternString, String newPatternString) {
        if (StringUtils.isNotBlank(dateTime)) {
            DateTimeFormatter oldPattern = DateTimeFormatter.ofPattern(oldPatternString);
            LocalDate parsedDate = LocalDate.parse(dateTime, oldPattern);
            DateTimeFormatter newPattern = DateTimeFormatter.ofPattern(newPatternString);
            return parsedDate.format(newPattern);
        }
        return null;
    }

    public static String removeAccentAndLowerCase(String s) {
        String removeAccent = removeAccent(s);
        if (StringUtils.isNotBlank(removeAccent)) {
            return removeAccent.trim().toLowerCase();
        }
        return s;
    }

    public static String formatTime(long unixTime, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return Instant.ofEpochSecond(unixTime)
                .atZone(ZoneId.of("GMT+7"))
                .format(formatter);
    }

    public static long getUnixTime(String dateString, String pattern) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(pattern);
            Date date = formatter.parse(dateString);
            return date.getTime() / 1000;
        } catch (Exception e) {
            return 0;
        }
    }

    public static long genUnixTime() {
        return System.currentTimeMillis() / 1000L;
    }

    public static String genUnixTimeString() {
        return String.valueOf(System.currentTimeMillis() / 1000L);
    }

    public static int getMonth(Long unixTime) {
        if (unixTime == null) {
            unixTime = genUnixTime();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(unixTime * 1000);
        return cal.get(Calendar.MONTH) + 1;
    }

    public static int getYear(Long unixTime) {
        if (unixTime == null) {
            unixTime = genUnixTime();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(unixTime * 1000);
        return cal.get(Calendar.YEAR);
    }

    public static int getHour(Long unixTime) {
        if (unixTime == null) {
            unixTime = genUnixTime();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(unixTime * 1000);
        return cal.get(Calendar.HOUR_OF_DAY) + 1;
    }

    public static int getMinute(Long unixTime) {
        if (unixTime == null) {
            unixTime = genUnixTime();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(unixTime * 1000);
        return cal.get(Calendar.MINUTE) + 1;
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            T key = entry.getKey();
            if (map.get(key).equals(value)) {
                return key;
            }
        }
        return null;
    }

    public static <T> List<T> commonElements(Iterable<? extends Set<? extends T>> lists) {
        Iterator<? extends Set<? extends T>> iterator = lists.iterator();
        Map<T, Integer> multiplicities = count(iterator.next());
        while (iterator.hasNext()) {
            Map<T, Integer> listCount = count(iterator.next());
            for (Iterator<Map.Entry<T, Integer>> it = multiplicities.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<T, Integer> e = it.next();
                T key = e.getKey();
                Integer count = listCount.get(key);
                if (count == null)
                    it.remove();
                else
                    e.setValue(Math.min(count, e.getValue()));
            }
        }
        List<T> result = new ArrayList<>();
        for (Map.Entry<T, Integer> e : multiplicities.entrySet())
            result.addAll(Collections.nCopies(e.getValue(), e.getKey()));
        return result;
    }

    static <T> Map<T, Integer> count(Set<? extends T> list) {
        Map<T, Integer> result = new HashMap<>();
        for (T t : list)
            result.merge(t, 1, Integer::sum);
        return result;
    }

    public static boolean containsItemFromArray(String inputString, String[] items) {
        // Convert the array of String items as a Stream
        // For each element of the Stream call inputString.contains(element)
        // If you have any match returns true, false otherwise
        return Arrays.stream(items).anyMatch(removeAccentAndLowerCase(inputString)::contains);
    }

    public static boolean containsItemFromList(String inputString, List<String> items) {
        // Convert the array of String items as a Stream
        // For each element of the Stream call inputString.contains(element)
        // If you have any match returns true, false otherwise
        return items.stream().anyMatch(removeAccentAndLowerCase(inputString)::contains);
    }

    public static boolean matchesAnyPattern(String input, String[] patterns) {
        return Arrays.stream(patterns)
                .map(Util::convertToRegex)
                .anyMatch(input::matches);
    }

    public static boolean matchesAnyPattern(String input, List<String> patterns) {
        return patterns.stream()
                .map(Util::convertToRegex)
                .anyMatch(input::matches);
    }

    private static String convertToRegex(String pattern) {
        return pattern
                .replace(".", "\\.")
                .replace("**", ".*")
                .replace("*", "[^/]*");
    }

}
