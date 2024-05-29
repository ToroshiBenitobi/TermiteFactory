package org.anar.termitefactory.entity;

import java.time.LocalDateTime;

public class Procedure {
    long job;
    int number;

    LocalDateTime operationStartTime;
    LocalDateTime processCompletedTime;
    LocalDateTime transportCompletedTime;
    String preMachine;
    String nextMachine;
    String status;
    String machine;
    private String jobName;
    private String jobCode;
    private String operationName;
    private String operationCode;

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

    public String getMachine() {
        return machine;
    }

    public void setMachine(String machine) {
        this.machine = machine;
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

    public String getNextMachine() {
        return nextMachine;
    }

    public void setNextMachine(String nextMachine) {
        this.nextMachine = nextMachine;
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
}
