package org.anar.scheduling.object;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProcessTime {
    private final List<Map<Integer, Double>>[] processTime;

    public ProcessTime(List<Map<Integer, Double>>[] processTime) {
        this.processTime = processTime;
    }

    public List<Map<Integer, Double>>[] processTime() {
        return processTime;
    }

    public Set<Integer> getAlternativeMachineSet(int job, int number) {
        return processTime[job].get(number).keySet();
    }

    public double getProcessTime(int job, int number, int machine) {
        return processTime[job].get(number).get(machine);
    }

    public int[] getNumberOfOperations() {
        int[] numberOfOperations = new int[processTime.length];
        for (int i = 0; i < processTime.length; i++) {
            numberOfOperations[i] = processTime[i].size();
        }
        return numberOfOperations;
    }

    public int getVariables() {
        int[] numberOfOperations = getNumberOfOperations();
        int n = 0;
        for (int numberOfOperation : numberOfOperations) {
            n += numberOfOperation;
        }
        return n * 2;
    }

    public int getNumberOfJobs() {
        return processTime.length;
    }
}
