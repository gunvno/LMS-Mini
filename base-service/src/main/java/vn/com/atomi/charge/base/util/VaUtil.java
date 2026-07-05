package vn.com.atomi.charge.base.util;

public class VaUtil {

  public static String nextVaCodeFromLast(String lastCode, int suffixLength) {
    if (lastCode == null || lastCode.length() < suffixLength) {
      throw new IllegalArgumentException("Invalid last code format");
    }

    // Extract suffix from lastCode
    String oldSuffix = lastCode.substring(lastCode.length() - suffixLength);

    // Get next suffix (may be longer than suffixLength if overflow happens)
    String nextSuffix = nextVaCodeSuffix(oldSuffix, suffixLength);

    // Replace old suffix with new suffix in lastCode
    // Note: if nextSuffix is longer, append it instead of replacing
    String prefix = lastCode.substring(0, lastCode.length() - suffixLength);

    return prefix + nextSuffix;
  }

  private static String nextVaCodeSuffix(String lastSuffix, int suffixLength) {
    if (lastSuffix.matches("\\d{" + suffixLength + "}")) {
      // Pure numeric suffix
      int number = Integer.parseInt(lastSuffix);
      int max = (int) Math.pow(10, suffixLength) - 1;

      if (number < max) {
        return String.format("%0" + suffixLength + "d", number + 1);
      } else {
        // Switch to letter + numeric suffix, e.g. A001
        return "A" + String.format("%0" + (suffixLength - 1) + "d", 1);
      }
    }

    // Check if suffix is all letters (e.g., "ZZZZ")
    if (lastSuffix.matches("[A-Z]{" + suffixLength + "}")) {
      // If all letters are 'Z', reset to numeric suffix with length suffixLength+1, e.g. "00001"
      if (lastSuffix.chars().allMatch(c -> c == 'Z')) {
        return String.format("%0" + (suffixLength + 1) + "d", 1);
      } else {
        // Otherwise increment letters
        return incrementLetters(lastSuffix);
      }
    }

    // Mixed letter + number suffix, e.g., "A001", "AA999"
    String letterPart = "";
    String numberPart = "";

    for (int i = 0; i < lastSuffix.length(); i++) {
      char c = lastSuffix.charAt(i);
      if (Character.isDigit(c)) {
        letterPart = lastSuffix.substring(0, i);
        numberPart = lastSuffix.substring(i);
        break;
      }
    }

    int number = Integer.parseInt(numberPart);
    int max = (int) Math.pow(10, numberPart.length()) - 1;

    if (number < max) {
      return letterPart + String.format("%0" + numberPart.length() + "d", number + 1);
    } else {
      String nextLetters = incrementLetters(letterPart);

      // Check if nextLetters is all 'Z' and length increased? If so, reset to numeric with length+1
      if (nextLetters.chars().allMatch(c -> c == 'Z') && nextLetters.length() > letterPart.length()) {
        return String.format("%0" + (numberPart.length() + 1) + "d", 1);
      }

      return nextLetters + String.format("%0" + numberPart.length() + "d", 1);
    }
  }

  private static String incrementLetters(String letters) {
    if (letters.isEmpty()) {
      return "A";
    }

    int lastIndex = letters.length() - 1;
    char lastChar = letters.charAt(lastIndex);

    if (lastChar < 'Z') {
      return letters.substring(0, lastIndex) + (char) (lastChar + 1);
    } else {
      // If last char is 'Z', recurse to increment previous letters
      String prefix = incrementLetters(letters.substring(0, lastIndex));
      return prefix + "A";
    }
  }
}
