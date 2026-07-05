package vn.com.atomi.charge.base.util;

import lombok.experimental.UtilityClass;
import org.springframework.cglib.core.Local;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

@UtilityClass
public class DateUtil {

    public static final String DEFAULT_TIME_ZONE = "UTC+7";

    public static final String YMD_DASH_PATTERN = "yyyy-MM-dd";

    public static final String YMD_PATTERN = "yyyyMMdd";

    public static final String DMY_DASH_PATTERN = "dd-MM-yyyy";

    public static final String YMD_SLASH_PATTERN = "yyyy/MM/dd";

    public static final String DMY_SLASH_PATTERN = "dd/MM/yyyy";

    public static final String DMY_HMS_SLASH_PATTERN = "dd/MM/yyyy HH:mm:ss";

    public static final String DMY_HMS_DASH_PATTERN = "dd-MM-yyyy HH:mm:ss";

    public static final String YMD_HMS_DASH_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final String YMD_HMS_MS_SLASH_PATTERN = "yyyy/MM/dd HH:mm:ss:SSSS";

    public static final String YMD_HMS_DASH_WITH_TIME_ZONE = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public static final String YMD_HMS_DASH_WITH_TIME = "yyyy-MM-dd'T'HH:mm:ss";

    public static final String YMD_HMS_MS_TIMEZONE_SLASH_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public static final String DATE_TIME_REGEX = "^$|^(29/02/((\\d{2}(0[48]|[2468][048]|[13579][26]))|(0[48]|[2468][048]|[13579][26])00))\\s+((2[0-3]|[01]?[0-9]):[0-5][0-9](:[0-5][0-9])?(?:\\s?[APap][Mm])?)$|((0?[1-9]|[12][0-9]|3[01])/(0?[13578]|1[02])/\\d{4})\\s+((2[0-3]|[01]?[0-9]):[0-5][0-9](:[0-5][0-9])?(?:\\s?[APap][Mm])?)$|((0?[1-9]|[12][0-9]|30)/(0?[469]|11)/\\d{4})\\s+((2[0-3]|[01]?[0-9]):[0-5][0-9](:[0-5][0-9])?(?:\\s?[APap][Mm])?)$|((0?[1-9]|1[0-9]|2[0-8])/02/\\d{4})\\s+((2[0-3]|[01]?[0-9]):[0-5][0-9](:[0-5][0-9])?(?:\\s?[APap][Mm])?)$";

    public static final String DATE_REGEX = "^$|^(29/02/((\\d{2}(0[48]|[2468][048]|[13579][26]))|(0[48]|[2468][048]|[13579][26])00))$|((0?[1-9]|[12][0-9]|3[01])/(0?[13578]|1[02])/\\d{4})$|((0?[1-9]|[12][0-9]|30)/(0?[469]|11)/\\d{4})$|((0?[1-9]|1[0-9]|2[0-8])/02/\\d{4})$";

    public static final String YMD_HMS_ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public static final String YMD_HMS_PATTERN = "yyyyMMddHHmmss";

    public static final String HMS_PATTERN = "HH:mm:ss";

    public static String formatTime(Long unixTime, String pattern) {
        if (unixTime == null) {
            unixTime = genUnixTime();
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return Instant.ofEpochSecond(unixTime)
                .atZone(ZoneId.of("GMT+7"))
                .format(formatter);
    }

    public static long getUnixTime(String stringDate, String stringPattern) {
        SimpleDateFormat format = new SimpleDateFormat(stringPattern);
        try {
            Date date = format.parse(stringDate);
            return date.getTime() / 1000L;
        } catch (Exception e) {
            return genUnixTime();
        }
    }

    public static long genUnixTime() {
        return System.currentTimeMillis() / 1000L;
    }

    public static String genUnixTimeString() {
        return String.valueOf(System.currentTimeMillis() / 1000L);
    }

    public static int getMonth(long unixTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(unixTime * 1000);
        return cal.get(Calendar.MONTH) + 1;
    }

    public static int getYear(long unixTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(unixTime * 1000);
        return cal.get(Calendar.YEAR);
    }

    public static String convertBetweenPattern(String dateTime, String oldPatternString, String newPatternString) {
        if (Util.isNotNull(dateTime)) {
            DateTimeFormatter oldPattern = DateTimeFormatter.ofPattern(oldPatternString);
            LocalDate parsedDate = LocalDate.parse(dateTime, oldPattern);
            DateTimeFormatter newPattern = DateTimeFormatter.ofPattern(newPatternString);
            return parsedDate.format(newPattern);
        }
        return null;
    }

    public static String formatInstantToString(Instant instant, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of(DateUtil.DEFAULT_TIME_ZONE));
        return formatter.format(zonedDateTime);
    }

    public static Instant formatDateStringToInstant(String dateString, String pattern) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        LocalDate ldt = LocalDate.parse(dateString, dtf);
        ZonedDateTime zdt = ldt.atStartOfDay(ZoneId.of(DateUtil.DEFAULT_TIME_ZONE));
        return zdt.toInstant();
    }

    public static String formatDateTimeToString(LocalDateTime dateTime, String pattern) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
      return dateTime.format(formatter);
    }

    public static Date formatDateStringToDate(String dateString, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        try {
            return format.parse(dateString);
        } catch (Exception e) {
            return null;
        }
    }

    public static String formatDateToString(Date date, String pattern) {
        DateFormat df = new SimpleDateFormat(pattern);
        return df.format(date);
    }

    public static String getCurrentDateString( String pattern) {
        DateFormat df = new SimpleDateFormat(pattern);
        return df.format(new Date());
    }

    public static Instant formatDateTimeStringToInstant(String dateString, String pattern) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime ldt = LocalDateTime.parse(dateString, dtf);
        ZonedDateTime zdt = ldt.atZone(ZoneId.of(DateUtil.DEFAULT_TIME_ZONE));
        return zdt.toInstant();
    }

    public static LocalDateTime formatDateTimeStringToDateTime(String dateString, String pattern) {
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
      return LocalDateTime.parse(dateString, dtf);
    }

    public static Instant getStartOfDay() {
        ZoneId utcZone = ZoneId.of(DateUtil.DEFAULT_TIME_ZONE);
        LocalDate today = LocalDate.now(utcZone);
        return today.atStartOfDay(utcZone).toInstant();
    }

    public static Instant getEndOfDay() {
        ZoneId utcZone = ZoneId.of(DateUtil.DEFAULT_TIME_ZONE);
        LocalDate today = LocalDate.now(utcZone);
        return today.atTime(LocalTime.MAX).atZone(utcZone).toInstant();
    }

    public static Instant getCurrentInstant() {
        return ZonedDateTime.now().toInstant();
    }

    public static Instant addDate(Instant inputInstant, ChronoUnit field, int value) {
        return inputInstant.plus(value, field);
    }

    public static Instant addDateFromCurrentInstant(ChronoUnit field, int value) {
        Instant inputInstant = DateUtil.getCurrentInstant();
        return inputInstant.plus(value, field);
    }

    public static LocalDateTime getCurrentLocalDateTime() {
        return LocalDateTime.now();
    }
}