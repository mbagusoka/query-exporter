package com.personal.queryexporter;

import com.personal.queryexporter.core.ExcelExporter;
import com.personal.queryexporter.model.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;

import java.util.Arrays;
import java.util.Scanner;

@SpringBootApplication
@PropertySource("classpath:application.properties")
public class QueryExporterApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryExporterApplication.class);
    
    private final ExcelExporter excelExporter;

    @Autowired
    public QueryExporterApplication(ExcelExporter excelExporter) {
        this.excelExporter = excelExporter;
    }

    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(QueryExporterApplication.class);
        QueryExporterApplication app = ctx.getBean(QueryExporterApplication.class);
        app.run();
    }

    private void run() {
        Scanner scanner = new Scanner(System.in);
        boolean next = true;
        Report report;
        while (next) {
            LOGGER.info("Please enter your query: ");
            report = new Report();
            report.setQuery(scanner.nextLine());
            LOGGER.info("Please enter a desired filename: ");
            report.setFileName(scanner.nextLine());
            LOGGER.info("Please enter your parameters (delimiter by comma, no space): ");
            report.setParams(Arrays.asList(scanner.nextLine().split(",")));
            LOGGER.info("Proceed with this current setup? Y/N ");
            if ("Y".equals(scanner.nextLine())) {
                LOGGER.info("Please wait while your query been exported. ");
                excelExporter.processReport(report);
                next = this.nextValidation(scanner);
            } else {
                next = this.nextValidation(scanner);
            }
        }
    }

    private boolean nextValidation(Scanner scanner) {
        LOGGER.info("Do you want to export again? Y/N ");
        if ("Y".equals(scanner.nextLine())) {
            return true;
        } else if ("N".equals(scanner.nextLine())) {
            return false;
        } else {
            LOGGER.info("Input either with Y or N. ");
            return this.nextValidation(scanner);
        }
    }
}
