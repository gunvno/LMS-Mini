package vn.com.atomi.charge.base.util;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class StringUtil {

  public static String beautyError(Exception e) {
    return ExceptionUtils.getStackTrace(e);
  }

  public static long getUnixTime() {
    return System.currentTimeMillis() / 1000;
  }

  public static String formatTime(Long unixTime, String pattern) {
    if (unixTime == null) {
      unixTime = getUnixTime();
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
    return Instant.ofEpochMilli(unixTime)
        .atZone(ZoneId.of("GMT+7"))
        .format(formatter);
  }
}
