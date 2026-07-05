package vn.com.atomi.charge.base.util.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import vn.com.atomi.charge.base.util.DateUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public abstract class BaseTemplateExcelExporter<T> {

  protected abstract Class<T> getDtoClass();
  protected abstract String getTemplatePath();
  protected int getStartRow() {
    return 1; // mặc định ghi từ row 1
  }
  protected String getSheetName() {
    return "Data";
  }

  public Workbook export(List<T> data) throws IOException {
    InputStream is = new ClassPathResource(getTemplatePath()).getInputStream();
    Workbook workbook = new XSSFWorkbook(is);
    Sheet sheet = workbook.getSheet(getSheetName());

    List<Field> fields = Arrays.stream(getDtoClass().getDeclaredFields())
      .filter(f -> f.isAnnotationPresent(ExportExcelColumn.class))
      .toList();

    writeData(sheet, fields, data);

    return workbook;
  }

  private void writeData(Sheet sheet, List<Field> fields, List<T> data) {
    int rowIndex = getStartRow();
    Row styleRow = sheet.getRow(rowIndex);
    int stt = 1;

    for (T item : data) {
      Row row = sheet.getRow(rowIndex);
      if (row == null) {
        row = sheet.createRow(rowIndex);
      }

      int sttColIndex = 0; // STT luôn ở cột đầu
      Cell sttCell = row.getCell(sttColIndex);
      if (sttCell == null) {
        sttCell = row.createCell(sttColIndex);

        Cell styleCell = styleRow.getCell(sttColIndex);
        if (styleCell != null) {
          sttCell.setCellStyle(styleCell.getCellStyle());
        }
      }
      sttCell.setCellValue(stt++);

      for (Field field : fields) {
        field.setAccessible(true);
        ExportExcelColumn col = field.getAnnotation(ExportExcelColumn.class);

        Cell cell = row.getCell(col.col());
        if (cell == null) {
          cell = row.createCell(col.col());
          Cell styleCell = styleRow.getCell(col.col());
          if (styleCell != null) {
            cell.setCellStyle(styleCell.getCellStyle());
          }
        }

        try {
          Object value = field.get(item);
          writeCell(cell, value, col);
        } catch (IllegalAccessException ignored) {
        }
      }
      rowIndex++;
    }
  }

  private void writeCell(Cell cell, Object value, ExportExcelColumn col) {
    if (value == null) return;

    if (value instanceof Number n) {
      cell.setCellValue(n.doubleValue());
    } else if (value instanceof LocalDateTime dt) {
      String pattern = col.format().isEmpty()
        ? DateUtil.DMY_HMS_SLASH_PATTERN
        : col.format();
      cell.setCellValue(dt.format(DateTimeFormatter.ofPattern(pattern)));
    } else {
      cell.setCellValue(value.toString());
    }
  }
}

