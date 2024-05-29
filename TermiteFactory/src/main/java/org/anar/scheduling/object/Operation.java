package org.anar.scheduling.object;

import java.util.Objects;

public class Operation {
    private int job;
    private int number;
    private double startTime;
    private double processTime;
    private double transportTime;
    private String nextMachine;
    private String preMachine;

    private String operationName;
    private String operationCode;

    public Operation(int job, int number) {
        this.job = job;
        this.number = number;
    }

    public Operation(int job, int number, double startTime, double processTime, double transportTime, String nextMachine, String preMachine) {
        this.job = job;
        this.number = number;
        this.startTime = startTime;
        this.processTime = processTime;
        this.transportTime = transportTime;
        this.nextMachine = nextMachine;
        this.preMachine = preMachine;
    }

    public String getNextMachine() {
        return nextMachine;
    }

    public void setNextMachine(String nextMachine) {
        this.nextMachine = nextMachine;
    }

    public int getJob() {
        return job;
    }

    public void setJob(int job) {
        this.job = job;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public double getProcessTime() {
        return processTime;
    }

    public void setProcessTime(double processTime) {
        this.processTime = processTime;
    }

    public double getTransportTime() {
        return transportTime;
    }

    public void setTransportTime(double transportTime) {
        this.transportTime = transportTime;
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
