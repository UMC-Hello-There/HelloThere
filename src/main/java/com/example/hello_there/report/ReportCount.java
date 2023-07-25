package com.example.hello_there.report;

public enum ReportCount {
    ADD_REPORT_FOR_BOARD(10000),
    ADD_REPORT_FOR_COMMENT(100),
    ADD_REPORT_FOR_MESSAGE(1);

    private final int counts;

    ReportCount(int counts){
        this.counts=counts;
    }

    public int getCount(){
        return counts;
    }
}
