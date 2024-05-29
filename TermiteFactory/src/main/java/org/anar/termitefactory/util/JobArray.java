package org.anar.termitefactory.util;

import org.anar.termitefactory.entity.schedule.Blueprint;
import org.anar.termitefactory.entity.schedule.Machine;

import java.util.List;
import java.util.Map;

public class JobArray {
    List<Job> jobList;
    List<Blueprint> blueprintList;
    List<Machine> machineList;
    double[][] jobTransportationTime;
    int[] nextJob;

    public JobArray(List<Blueprint> blueprintList) {
        this.blueprintList = blueprintList;
    }

    public List<Job> getJobList() {
        return jobList;
    }

    public void setJobList(List<Job> jobList) {
        this.jobList = jobList;
    }

    public List<Blueprint> getBlueprintList() {
        return blueprintList;
    }

    public void setBlueprintList(List<Blueprint> blueprintList) {
        this.blueprintList = blueprintList;
    }

    public List<Machine> getMachineList() {
        return machineList;
    }

    public void setMachineList(List<Machine> machineList) {
        this.machineList = machineList;
    }

    public double[][] getJobTransportationTime() {
        return jobTransportationTime;
    }

    public void setJobTransportationTime(double[][] jobTransportationTime) {
        this.jobTransportationTime = jobTransportationTime;
    }

    public int[] getNextJob() {
        return nextJob;
    }

    public void setNextJob(int[] nextJob) {
        this.nextJob = nextJob;
    }
}
