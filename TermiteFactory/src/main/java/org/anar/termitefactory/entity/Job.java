package org.anar.termitefactory.entity;

import org.springframework.data.annotation.Id;

import java.util.List;

public class Job {
    @Id
    String id;
    long index;
    long nextJob;
    List<Long> preJobs;
    String status;
    String jobCode;
    List<Procedure> procedures;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Procedure> getProcedures() {
        return procedures;
    }

    public void setProcedures(List<Procedure> procedures) {
        this.procedures = procedures;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJobCode() {
        return jobCode;
    }

    public void setJobCode(String jobCode) {
        this.jobCode = jobCode;
    }
}
