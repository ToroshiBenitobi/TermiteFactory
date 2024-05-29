package org.anar.termitefactory.entity;

import org.springframework.data.annotation.Id;

import java.util.List;

public class EdgeMachine {
    @Id
    String id;
    String number;
    String status;
    double consumption;
    double hourlyConsumption;
    long runningTime;
    long idlingTime;
    long timeStamp;

    List<Procedure> procedureList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getConsumption() {
        return consumption;
    }

    public void setConsumption(double consumption) {
        this.consumption = consumption;
    }

    public double getHourlyConsumption() {
        return hourlyConsumption;
    }

    public void setHourlyConsumption(double hourlyConsumption) {
        this.hourlyConsumption = hourlyConsumption;
    }

    public long getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(long runningTime) {
        this.runningTime = runningTime;
    }

    public long getIdlingTime() {
        return idlingTime;
    }

    public void setIdlingTime(long idlingTime) {
        this.idlingTime = idlingTime;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public List<Procedure> getProcedureList() {
        return procedureList;
    }

    public void setProcedureList(List<Procedure> procedureList) {
        this.procedureList = procedureList;
    }
}
