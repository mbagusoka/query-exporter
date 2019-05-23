package com.personal.queryexporter;

import com.personal.queryexporter.core.ExcelExporter;
import com.personal.queryexporter.model.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@PropertySource("classpath:application.properties")
public class QueryExporterApplication {

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
        Report report1 = new Report();
        report1.setQuery(
                "select userbo.cd as \"User Code\", userbo.name as \"User Name\", userbo.create_dt as \"Created Date\",\n" +
                "case when\n" +
                "  (select max(loginhist.login_dt) from gcm_asecurity.im_login_hist loginhist where loginhist.user_cd = userbo.cd) < userbo.create_dt\n" +
                "    then\n" +
                "      null\n" +
                "    else\n" +
                "      (select max(loginhist.login_dt) from gcm_asecurity.im_login_hist loginhist where loginhist.user_cd = userbo.cd)\n" +
                "end as \"Last Login\"\n" +
                "from gcm_asecurity.im_user userbo\n" +
                "where userbo.is_delete = 'N'\n" +
                "and userbo.is_inactive = 'N'\n" +
                "order by userbo.cd"
        );
        report1.setFileName("test");
        Report report2 = new Report();
        report2.setQuery(
                "select A.ACTN_BY_CUST_ID as group_id, A.ACTN_BY_CUST_NM as group_nm, A.SRVC_NM as service_nm,\n" +
                "B.DEBIT_ACCT_NO as acct_no, trunc(C.CREATED_DT) as reg_acct_dt, D.BUS_UNIT_CD as segment, count(A.SRVC_NM) as total_trx,\n" +
                "sum(NVL(b.CH_TYP_1_EQ_AMT,0) + NVL(b.CH_TYP_2_EQ_AMT,0) + NVL(b.CH_TYP_3_EQ_AMT,0) + NVL(b.CH_TYP_4_EQ_AMT,0) + NVL(b.CH_TYP_5_EQ_AMT,0)) as total_charge,\n" +
                "sum(b.trx_amt) as tot_amt\n" +
                "from  gcm_agcm.ACTV_LOG a\n" +
                "inner join GCM_AGCM.EXECUTION_LOG b on A.ID = B.ACTV_LOG_ID\n" +
                "inner join gcm_agcm.corp_acct c on B.DEBIT_ACCT_NO = C.ACCT_NO and C.CORP_ID = A.ACTN_BY_CUST_ID\n" +
                "inner join gcm_agcm.corp d on C.CORP_ID = d.id\n" +
                "where A.ACTN_DT between to_date(?, 'dd-MM-yyyy') and to_date(?, 'dd-MM-yyyy')\n" +
                "and b.DEBIT_ACCT_NO is not null\n" +
                "group by A.ACTN_BY_CUST_ID, A.ACTN_BY_CUST_NM, A.SRVC_NM, B.DEBIT_ACCT_NO, trunc(C.CREATED_DT), D.BUS_UNIT_CD\n" +
                "order by A.ACTN_BY_CUST_ID, A.SRVC_NM asc"
        );
        report2.setParams(Arrays.asList("01-04-2019", "01-05-2019"));
        report2.setFileName("test 2 params");
        Report report3 = new Report();
        report3.setQuery(
                "select ACTN_DT as \"Action Date\", ACTN_BY_CUST_ID as \"Corporate ID\", ACTN_BY_CUST_NM as \"Corporate Name\",\n" +
                "ACTN_TYP_NM as \"Action\", ACTN_BY_NM as \"Action By\", REF_NO as \"Reference No.\", SRVC_NM as \"Service\", IS_ERR as \"Is Error\", ERR_MAP_NM as \"Error Name\", \n" +
                "TRX_STS_NM as \"Status\"\n" +
                "from GCM_AGCM.ACTV_LOG\n" +
                "where to_char(ACTN_DT, 'MM-yyyy') = ?\n" +
                "and ACTN_BY <> 'System'\n" +
                "and ECHANNEL_CD = 'CM'\n" +
                "order by ACTN_BY_CUST_ID, REF_NO, ACTN_DT"
        );
        report3.setParams(Arrays.asList("04-2019"));
        report3.setFileName("test 1 param");
        List<Report> reports = Arrays.asList(report1, report2, report3);
        for (Report report : reports) {
            excelExporter.processReport(report);
        }
    }
}
