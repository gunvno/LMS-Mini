package vn.com.atomi.charge.base.util.excel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ImportExcelColumn {
  String header();               // Tên cột trong Excel
  boolean required() default false; // Có bắt buộc nhập hay không
  int minLength() default -1;    // Độ dài tối thiểu (String)
  int maxLength() default -1;    // Độ dài tối đa (String)
  double minValue() default Double.NaN; // Giá trị min (Number)
  double maxValue() default Double.NaN; // Giá trị max (Number)
  String regex() default "";     // Regex pattern để check format
}
