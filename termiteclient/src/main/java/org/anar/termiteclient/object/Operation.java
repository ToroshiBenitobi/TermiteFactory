package org.anar.termiteclient.object;

import java.time.LocalDateTime;
import java.util.Objects;

public class Operation {
    private long job;
    private int number;
    private LocalDateTime operationStartTime;
    private LocalDateTime processCompletedTime;
    private LocalDateTime transportCompletedTime;
    private String nextMachine;
    private String preMachine;

    private String jobName;
    private String jobCode;
    private String operationName;
    private String operationCode;
    private String status;

    public Operation(long job, int number) {
        this.job = job;
        this.number = number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobCode() {
        return jobCode;
    }

    public void setJobCode(String jobCode) {
        this.jobCode = jobCode;
    }

    public String getNextMachine() {
        return nextMachine;
    }

    public void setNextMachine(String nextMachine) {
        this.nextMachine = nextMachine;
    }

    public long getJob() {
        return job;
    }

    public void setJob(long job) {
        this.job = job;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public LocalDateTime getOperationStartTime() {
        return operationStartTime;
    }

    public void setOperationStartTime(LocalDateTime operationStartTime) {
        this.operationStartTime = operationStartTime;
    }

    public LocalDateTime getProcessCompletedTime() {
        return processCompletedTime;
    }

    public void setProcessCompletedTime(LocalDateTime processCompletedTime) {
        this.processCompletedTime = processCompletedTime;
    }

    public LocalDateTime getTransportCompletedTime() {
        return transportCompletedTime;
    }

    public void setTransportCompletedTime(LocalDateTime transportCompletedTime) {
        this.transportCompletedTime = transportCompletedTime;
    }

    public String getPreMachine() {
        return preMachine;
    }

    public void setPreMachine(String preMachine) {
        this.preMachine = preMachine;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getOperationCode() {
        return operationCode;
    }

    public void setOperationCode(String operationCode) {
        this.operationCode = operationCode;
    }

    @Override
    public String toString() {
        return "(" + job + "," + number + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Operation operation = (Operation) o;
        return job == operation.job && number == operation.number;
    }

    @Override
    public int hashCode() {
        return Objects.hash(job, number);
    }
}
