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
        try (Scanner scanner = new Scanner(System.in)) {
            boolean next = true;
            Report report;
            while (next) {
                report = new Report();
                System.out.println("Please enter your query: (Please enter twice to proceed)");
                report.setQuery(this.getQuery(scanner));
                System.out.println("Please enter row limit: ");
                report.setRowLimit(Integer.parseInt(scanner.nextLine()));
                System.out.println("Please enter a desired filename: ");
                report.setFileName(scanner.nextLine());
                System.out.println("Please enter a path output: ");
                report.setPathOutput(scanner.nextLine());
                System.out.println("Proceed with this current setup? Y/N ");
                if ("Y".equalsIgnoreCase(scanner.nextLine())) {
                    System.out.println("Please wait while your query been exported. ");
                    excelExporter.processReport(report);
                    next = this.nextValidation(scanner);
                } else {
                    next = this.nextValidation(scanner);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Query Exporter Exception: ", e);
        }
    }

    private String getQuery(Scanner scanner) {
        StringBuilder query = new StringBuilder();
        String line;
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            if (line.isEmpty()) {
                break;
            }
            query.append(line).append("\n");
        }
        return query.toString();
    }

    private boolean nextValidation(Scanner scanner) {
        System.out.println("Do you want to export again? Y/N ");
        String answer = scanner.nextLine();
        if ("Y".equalsIgnoreCase(answer)) {
            return true;
        } else if ("N".equalsIgnoreCase(answer)) {
            return false;
        } else {
            System.out.println("Input either with Y or N. ");
            return this.nextValidation(scanner);
        }
    }
}
