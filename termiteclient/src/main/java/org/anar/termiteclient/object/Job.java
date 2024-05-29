package org.anar.termiteclient.object;

import java.util.List;
import java.util.Objects;

public class Job {
    private long index;
    private long nextJob;
    private List<Long> preJobs;
    private int numberOfOperations;
    private String jobCode;
    private String jobName;

    public Job(long index) {
        this.index = index;
    }

    public Job() {
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getNextJob() {
        return nextJob;
    }

    public void setNextJob(long nextJob) {
        this.nextJob = nextJob;
    }

    public List<Long> getPreJobs() {
        return preJobs;
    }

    public void setPreJobs(List<Long> preJobs) {
        this.preJobs = preJobs;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return index == job.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index);
    }
}
