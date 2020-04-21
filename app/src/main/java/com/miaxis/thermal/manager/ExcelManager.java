package com.miaxis.thermal.manager;

import androidx.annotation.NonNull;

import com.miaxis.thermal.app.App;
import com.miaxis.thermal.data.entity.Record;
import com.miaxis.thermal.data.entity.RecordSearch;
import com.miaxis.thermal.data.repository.RecordRepository;
import com.miaxis.thermal.util.DateUtil;
import com.miaxis.thermal.util.ValueUtil;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ExcelManager {

    private ExcelManager() {
    }

    public static ExcelManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final ExcelManager instance = new ExcelManager();
    }

    /**
     * ================================ 静态内部类单例写法 ================================
     **/

    private static final String SHEET_NAME = "record";

    public interface OnExportListener {
        void onProgress(int now, int total);
        void onResult(boolean result, String message);
    }

    public void exportRecordExcel(@NonNull RecordSearch recordSearch, @NonNull String filePath, @NonNull OnExportListener listener) {
        App.getInstance().getThreadExecutor().execute(() -> {
            int recordCount = RecordRepository.getInstance().searchRecordCount(recordSearch);
            if (recordCount == 0) {
                listener.onResult(false, "待导出内容为空");
                return;
            }
            try {
                XSSFWorkbook workbook = createWorkbook();
                XSSFSheet sheet = workbook.getSheet(SHEET_NAME);
                recordSearch.setPageSize(100);
                List<Record> recordList;
                int allPage = recordCount / recordSearch.getPageSize() + 1;
                for (int page = 1; page <= allPage; page++) {
                    recordSearch.setPageNum(page);
                    recordList = RecordRepository.getInstance().searchRecord(recordSearch);
                    if (recordList != null && !recordList.isEmpty()) {
                        for (int i = 0; i < recordList.size(); i++) {
                            Record record = recordList.get(i);
                            XSSFRow row = sheet.createRow((page - 1) * recordSearch.getPageSize() + i + 1);
                            writeRecordToRow(row, record);
                        }
                    }
                    listener.onProgress(page, allPage);
                }
                File file = new File(filePath);
                saveWorkbook(workbook, new FileOutputStream(file));
                listener.onResult(true, "导出成功");
            } catch (Exception e) {
                e.printStackTrace();
                listener.onResult(false, "导出过程出现错误：\n" + e.getMessage());
            }
        });
    }

    private XSSFWorkbook createWorkbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(SHEET_NAME);
        XSSFRow row = sheet.createRow(0);
        writeRecordRowName(sheet, row);
        return workbook;
    }

    private void saveWorkbook(XSSFWorkbook workbook, FileOutputStream fileOutputStream) {
        try {
            workbook.write(fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeRecordToRow(XSSFRow row, Record record) {
        XSSFCell cellId = row.createCell(0);
        cellId.setCellValue(record.getId());
        XSSFCell cellName = row.createCell(1);
        cellName.setCellValue(record.getName());
        XSSFCell cellIdentifyNumber = row.createCell(2);
        cellIdentifyNumber.setCellValue(record.getIdentifyNumber());
        XSSFCell cellPhone = row.createCell(3);
        cellPhone.setCellValue(record.getPhone());
        XSSFCell cellType = row.createCell(4);
        cellType.setCellValue(ValueUtil.getPersonTypeName(record.getType()));
        XSSFCell cellVerifyTime = row.createCell(5);
        cellVerifyTime.setCellValue(DateUtil.DATE_FORMAT.format(record.getVerifyTime()));
        XSSFCell cellScore = row.createCell(6);
        cellScore.setCellValue(String.format(Locale.CHINA, "%.2f", record.getScore()));
        XSSFCell cellTemperature = row.createCell(7);
        if (record.getTemperature() == -1f) {
            cellTemperature.setCellValue("无");
        } else if (record.getTemperature() == -2f) {
            cellTemperature.setCellValue("错误");
        } else {
            cellTemperature.setCellValue(String.format(Locale.CHINA, "%.2f", record.getTemperature()));
        }
        XSSFCell cellVerifyPicturePath = row.createCell(8);
        cellVerifyPicturePath.setCellValue(record.getVerifyPicturePath());
    }

    private void writeRecordRowName(XSSFSheet sheet, XSSFRow row) {
        sheet.setColumnWidth(0, 256 * 10 + 184);
        sheet.setColumnWidth(1, 256 * 15 + 184);
        sheet.setColumnWidth(2, 256 * 20 + 184);
        sheet.setColumnWidth(3, 256 * 15 + 184);
        sheet.setColumnWidth(4, 256 * 10 + 184);
        sheet.setColumnWidth(5, 256 * 20 + 184);
        sheet.setColumnWidth(6, 256 * 10 + 184);
        sheet.setColumnWidth(7, 256 * 10 + 184);
        sheet.setColumnWidth(8, 256 * 90 + 184);
        XSSFCell cellId = row.createCell(0);
        cellId.setCellValue("ID");
        XSSFCell cellName = row.createCell(1);
        cellName.setCellValue("姓名");
        XSSFCell cellIdentifyNumber = row.createCell(2);
        cellIdentifyNumber.setCellValue("证件号码");
        XSSFCell cellPhone = row.createCell(3);
        cellPhone.setCellValue("手机号码");
        XSSFCell cellType = row.createCell(4);
        cellType.setCellValue("人员类型");
        XSSFCell cellVerifyTime = row.createCell(5);
        cellVerifyTime.setCellValue("日志时间");
        XSSFCell cellScore = row.createCell(6);
        cellScore.setCellValue("人脸分数");
        XSSFCell cellTemperature = row.createCell(7);
        cellTemperature.setCellValue("体温");
        XSSFCell cellVerifyPicturePath = row.createCell(8);
        cellVerifyPicturePath.setCellValue("图片路径");
    }

}
