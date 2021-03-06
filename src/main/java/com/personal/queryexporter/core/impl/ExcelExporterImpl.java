package com.personal.queryexporter.core.impl;

import com.personal.queryexporter.common.constant.ExporterConstants;
import com.personal.queryexporter.common.service.ZipService;
import com.personal.queryexporter.config.DatabaseConfig;
import com.personal.queryexporter.core.ExcelExporter;
import com.personal.queryexporter.model.Report;
import com.personal.queryexporter.util.QueryBuilder;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@Service
public class ExcelExporterImpl implements ExcelExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelExporterImpl.class);
    private static final String SHEET = "Sheet";
    private static final String XLS = ".xls";
    private static final String TOTAL = "TOTAL";
    private static final String UNDERSCORE = "_";

    private final DatabaseConfig databaseConfig;
    private final ZipService zipService;

    @Autowired
    public ExcelExporterImpl(DatabaseConfig databaseConfig, ZipService zipService) {
        this.databaseConfig = databaseConfig;
        this.zipService = zipService;
    }

    @Override
    public void processReport(Report report) {
        LOGGER.info("------START EXPORTING QUERY------ {}", Calendar.getInstance().getTime());
        int rowCount = 0;
        String countQuery = QueryBuilder.countQueryBuilder(report.getQuery());
        try (Connection conn = databaseConfig.dataSource().getConnection(); PreparedStatement ps = conn.prepareStatement(countQuery)) {
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                rowCount = rs.getInt(TOTAL);
            }
        } catch (SQLException e) {
            LOGGER.error("Process Report Exception: ", e);
        }
        if (rowCount > 0) {
            this.generateReport(report, rowCount);
        } else {
            LOGGER.info("There is no record with this query.");
        }
        LOGGER.info("------END EXPORTING QUERY------ {}", Calendar.getInstance().getTime());
    }

    private void generateReport(Report report, int rowCount) {
        int row = 0;
        int loop;
        String fileName = report.getFileName();
        String query = report.getQuery();
        int rowLimit = report.getRowLimit();
        List<File> files = new ArrayList<>();
        if (0 != rowCount) {
            loop = (int) Math.ceil((double) rowCount / rowLimit);
            for (int i = 0; i < loop; i++) {
                report.setQuery(QueryBuilder.rowQueryBuilder(query, String.valueOf(row + 1), String.valueOf(row + rowLimit)));
                row += rowLimit;
                report.setFileName(fileName.concat(UNDERSCORE).concat(String.valueOf(i + 1)));
                this.export(report);
                files.add(new File(report.getPathOutput().concat(report.getFileName()).concat(XLS)));
            }
            this.zipFiles(report, files, fileName);
        }
    }

    private void export(Report report) {
        LOGGER.info("------START EXPORTING REPORT {}------", report.getFileName());
        try (Connection conn = databaseConfig.dataSource().getConnection(); PreparedStatement ps = conn.prepareStatement(report.getQuery())) {
            try (ResultSet rs = ps.executeQuery(); Workbook workbook = new HSSFWorkbook()) {
                Sheet sheet = workbook.createSheet(SHEET);
                Row titleRow = sheet.createRow(0);
                this.setHeaderColumn(rs, workbook, titleRow);
                this.fillData(rs, sheet);
                try (FileOutputStream fos = new FileOutputStream(report.getPathOutput().concat(report.getFileName()).concat(XLS))) {
                    workbook.write(fos);
                }
            }
        } catch (IOException | SQLException e) {
            LOGGER.error("Export Exception: ", e);
        }
        LOGGER.info("------END EXPORTING REPORT {}------", report.getFileName());
    }

    private void setHeaderColumn(ResultSet rs, Workbook workbook, Row titleRow) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        for (int colIndex = 0; colIndex < metaData.getColumnCount() - 1; colIndex++) {
            Cell cell = titleRow.createCell(colIndex);
            cell.setCellValue(metaData.getColumnLabel(colIndex + 2));
            cell.setCellStyle(this.getBoldFont(workbook));
        }
    }

    private void fillData(ResultSet rs, Sheet sheet) throws SQLException {
        int rowCount = 1;
        Row row;
        while (rs.next()) {
            row = sheet.createRow(rowCount);
            ResultSetMetaData metaData = rs.getMetaData();
            for (int i = 0; i < metaData.getColumnCount() - 1; i++) {
                row.createCell(i).setCellValue(rs.getString(metaData.getColumnLabel(i + 2)));
            }
            rowCount++;
        }
    }

    private CellStyle getBoldFont(Workbook workbook) {
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(boldFont);
        return style;
    }

    private void zipFiles(Report report, List<File> files, String fileName) {
        if (report.isZipFlag()) {
            Map<String, Object> params = new HashMap<>();
            params.put(ExporterConstants.FILES, files);
            params.put(ExporterConstants.FILE_PATH, report.getPathOutput().concat(fileName));
            if (report.isPasswordFlag()) {
                params.put(ExporterConstants.PASS_KEY, report.getPassword());
            }
            zipService.zip(params);
        }
    }
}
