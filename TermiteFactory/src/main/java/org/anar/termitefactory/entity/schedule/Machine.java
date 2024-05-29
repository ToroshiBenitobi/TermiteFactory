package org.anar.termitefactory.entity.schedule;

import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.Objects;

public class Machine {
    @Id
    String id;
    String number;
    double staticWaitingRate;
    double dynamicProcessingRate;
    double locationX;
    double locationY;
    String[] tag;
    List<ProcessTime> processTime;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getStaticWaitingRate() {
        return staticWaitingRate;
    }

    public void setStaticWaitingRate(double staticWaitingRate) {
        this.staticWaitingRate = staticWaitingRate;
    }

    public double getDynamicProcessingRate() {
        return dynamicProcessingRate;
    }

    public void setDynamicProcessingRate(double dynamicProcessingRate) {
        this.dynamicProcessingRate = dynamicProcessingRate;
    }

    public double getLocationX() {
        return locationX;
    }

    public void setLocationX(double locationX) {
        this.locationX = locationX;
    }

    public double getLocationY() {
        return locationY;
    }

    public void setLocationY(double locationY) {
        this.locationY = locationY;
    }

    public String[] getTag() {
        return tag;
    }

    public void setTag(String[] tag) {
        this.tag = tag;
    }

    public List<ProcessTime> getProcessTime() {
        return processTime;
    }

    public void setProcessTime(List<ProcessTime> processTime) {
        this.processTime = processTime;
    }

    public ProcessTime getProcessTime(String code, String procedure) {
        for (ProcessTime time : processTime) {
            if (time.getCode().equals(code) && time.getProcedure().equals(procedure))
                return time;
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Machine machine = (Machine) o;
        return Objects.equals(id, machine.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
