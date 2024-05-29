package org.anar.scheduling.object;

import java.util.List;

public class Job {
    private int index;
    private int nextJob;
    private String jobCode;
    private String jobName;
    private int numberOfOperations;
    private List<Integer> preJobs;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<Integer> getPreJobs() {
        return preJobs;
    }

    public void setPreJobs(List<Integer> preJobs) {
        this.preJobs = preJobs;
    }

    public int getNextJob() {
        return nextJob;
    }

    public void setNextJob(int nextJob) {
        this.nextJob = nextJob;
    }

    public String getJobCode() {
        return jobCode;
    }

    public void setJobCode(String jobCode) {
        this.jobCode = jobCode;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public int getNumberOfOperations() {
        return numberOfOperations;
    }

    public void setNumberOfOperations(int numberOfOperations) {
        this.numberOfOperations = numberOfOperations;
    }
}
