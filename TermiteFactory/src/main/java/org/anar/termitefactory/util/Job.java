package org.anar.termitefactory.util;

import org.anar.termitefactory.entity.schedule.Blueprint;
import org.anar.termitefactory.repository.BluePrintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

public class Job {

    List<Map<Integer, Double>> processTime;
    int nextJob;
    int numberOfOperation;
    double[][] processingIntervalConstraint;
    boolean[][] processingDayConstraint;
    boolean[] isPCO;
    double rawMaterialCost;
    Blueprint blueprint;

    public Job() {
    }

    public Blueprint getBlueprint() {
        return blueprint;
    }

    public void setBlueprint(Blueprint blueprint) {
        this.blueprint = blueprint;
    }

    public List<Map<Integer, Double>> getProcessTime() {
        return processTime;
    }

    public void setProcessTime(List<Map<Integer, Double>> processTime) {
        this.processTime = processTime;
    }

    public int getNextJob() {
        return nextJob;
    }

    public void setNextJob(int nextJob) {
        this.nextJob = nextJob;
    }

    public int getNumberOfOperation() {
        return numberOfOperation;
    }

    public void setNumberOfOperation(int numberOfOperation) {
        this.numberOfOperation = numberOfOperation;
    }

    public double[][] getProcessingIntervalConstraint() {
        return processingIntervalConstraint;
    }

    public void setProcessingIntervalConstraint(double[][] processingIntervalConstraint) {
        this.processingIntervalConstraint = processingIntervalConstraint;
    }

    public boolean[][] getProcessingDayConstraint() {
        return processingDayConstraint;
    }

    public void setProcessingDayConstraint(boolean[][] processingDayConstraint) {
        this.processingDayConstraint = processingDayConstraint;
    }

    public boolean[] getIsPCO() {
        return isPCO;
    }

    public void setIsPCO(boolean[] isPCO) {
        this.isPCO = isPCO;
    }

    public double getRawMaterialCost() {
        return rawMaterialCost;
    }

    public void setRawMaterialCost(double rawMaterialCost) {
        this.rawMaterialCost = rawMaterialCost;
    }
}
