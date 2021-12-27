package com.ccwlab.worker.message;

public class Report {
    Result result;
    long workId;
    String message;

    public Report(Result result, long workId, String message) {
        this.result = result;
        this.workId = workId;
        this.message = message;
    }

    public long getWorkId() {
        return workId;
    }

    public void setWorkId(long workId) {
        this.workId = workId;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}