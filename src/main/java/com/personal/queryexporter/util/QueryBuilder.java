package com.personal.queryexporter.util;

public final class QueryBuilder {

    private static final String PRE_COUNT = "select count(*) total from ( ";
    private static final String POST_COUNT = " )";
    private static final String PRE_ROW = "select * from ( select rownum no, a.* from ( ";
    private static final String MIDDLE_ROW = " ) a ) where no between ";
    private static final String POST_ROW = " and ";

    private QueryBuilder() {
        throw new IllegalAccessError("QueryBuilder Class");
    }

    public static String countQueryBuilder(String query) {
        return PRE_COUNT.concat(query).concat(POST_COUNT);
    }

    public static String rowQueryBuilder(String query, String from, String to) {
        return PRE_ROW.concat(query).concat(MIDDLE_ROW).concat(from).concat(POST_ROW).concat(to);
    }
}
