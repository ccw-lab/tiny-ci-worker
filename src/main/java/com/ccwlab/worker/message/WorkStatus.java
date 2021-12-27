package com.ccwlab.worker.message;

public enum WorkStatus{
    WAIT_FOR_WORKER,
    PROGRESS_BY_WORKER,
    STOPPED,
    COMPLETED,
    ERROR
}
