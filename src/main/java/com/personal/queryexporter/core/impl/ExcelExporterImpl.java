package com.personal.queryexporter.core.impl;

import com.personal.queryexporter.config.DatabaseConfig;
import com.personal.queryexporter.core.ExcelExporter;
import com.personal.queryexporter.model.Report;
import com.personal.queryexporter.util.Utility;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;

@Service
public class ExcelExporterImpl implements ExcelExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelExporterImpl.class);
    private static final String SHEET = "Sheet";
    private static final String XLS = ".xls";
    private static final String TOTAL = "TOTAL";
    private static final String UNDERSCORE = "_";

    private final DatabaseConfig databaseConfig;

    @Value("${path.output}")
    private String pathOutput;

    @Value("${row.limit}")
    private int rowLimit;

    @Autowired
    public ExcelExporterImpl(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    @Override
    @Async
    public void processReport(Report report) {
        LOGGER.info("------START EXPORTING QUERY------");
        int rowCount = 0;
        String countQuery = Utility.countQueryBuilder(report.getQuery());
        try (Connection conn = databaseConfig.dataSource().getConnection(); PreparedStatement ps = conn.prepareStatement(countQuery)) {
            this.setQueryParameters(report, ps);
            try (ResultSet rs = ps.executeQuery()) {
                rowCount = rs.getInt(TOTAL);
            }
        } catch (SQLException e) {
            LOGGER.error("Process Report Exception: ", e);
        }
        this.generateReport(report, rowCount);
        LOGGER.info("------START EXPORTING QUERY------");
    }

    private void generateReport(Report report, int rowCount) {
        int row = 1;
        int loop;
        if (0 != rowCount) {
            loop = (int) Math.ceil((float) rowCount / (float) rowLimit);
            for (int i = 0; i < loop; i++) {
                report.setQuery(Utility.rowQueryBuilder(report.getQuery(), String.valueOf(row), String.valueOf(row + rowLimit)));
                row += rowLimit + 1;
                report.setFileName(report.getFileName().concat(UNDERSCORE).concat(String.valueOf(i + 1)));
                this.export(report);
            }
        }
    }

    private void export(Report report) {
        LOGGER.info("------START EXPORTING REPORT {}------", report.getFileName());
        try (Connection conn = databaseConfig.dataSource().getConnection(); PreparedStatement ps = conn.prepareStatement(report.getQuery())) {
            this.setQueryParameters(report, ps);
            try (ResultSet rs = ps.executeQuery(); Workbook workbook = new HSSFWorkbook()) {
                Sheet sheet = workbook.createSheet(SHEET);
                Row titleRow = sheet.createRow(0);
                this.setHeaderColumn(rs, workbook, titleRow);
                this.fillData(rs, sheet);
                try (FileOutputStream fos = new FileOutputStream(pathOutput.concat(report.getFileName()).concat(XLS))) {
                    workbook.write(fos);
                }
            }
        } catch (IOException | SQLException e) {
            LOGGER.error("Export Exception: ", e);
        }
        LOGGER.info("------END EXPORTING REPORT {}------", report.getFileName());
    }

    private void setQueryParameters(Report report, PreparedStatement ps) throws SQLException {
        if (null != report.getParams()) {
            for (int i = 0; i < report.getParams().size(); i++) {
                ps.setString(i + 1, report.getParams().get(i));
            }
        }
    }

    private void setHeaderColumn(ResultSet rs, Workbook workbook, Row titleRow) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        for (int colIndex = 0; colIndex < metaData.getColumnCount(); colIndex++) {
            Cell cell = titleRow.createCell(colIndex);
            cell.setCellValue(metaData.getColumnLabel(colIndex + 1));
            cell.setCellStyle(this.getBoldFont(workbook));

        }
    }

    private void fillData(ResultSet rs, Sheet sheet) throws SQLException {
        int rowCount = 1;
        Row row;
        while (rs.next()) {
            row = sheet.createRow(rowCount);
            ResultSetMetaData metaData = rs.getMetaData();
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                row.createCell(i).setCellValue(rs.getString(metaData.getColumnLabel(i + 1)));
                sheet.autoSizeColumn(i);
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
}
