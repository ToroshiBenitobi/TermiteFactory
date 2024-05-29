package org.anar.termiteclient.object;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class OperationStatus {
    private static final Logger logger = LoggerFactory.getLogger(OperationStatus.class);
    @Autowired
    ObjectMapper objectMapper;
    Long timeStamp;
    private final List<Operation> operationList;
    private final List<Job> jobList;

    public OperationStatus() {
        this.operationList = new ArrayList<>();
        this.jobList = new ArrayList<>();
        timeStamp = Calendar.getInstance().getTimeInMillis();
    }

    public void clear() {
        operationList.clear();
    }

    public void readNewSchedule(Long timeStamp, JsonNode operations, JsonNode jobs) throws JsonProcessingException {


        for (JsonNode job : jobs) {
            Job newJob = objectMapper.treeToValue(job, Job.class);
            jobList.add(newJob);
        }

        this.timeStamp = timeStamp;
        for (JsonNode operation : operations) {
            Operation newOperation = objectMapper.treeToValue(operation, Operation.class);

            String jobCode = null;
            String jobName = null;
            for (Job job : jobList) {
                if (job.getIndex() == newOperation.getJob()) {
                    jobCode = job.getJobCode();
                    jobName = job.getJobName();
                }
            }

            newOperation.setJobCode(jobCode);
            newOperation.setJobName(jobName);
            newOperation.setStatus("scheduled");

            operationList.add(newOperation);
        }

        Operation firstOperation = operationList.get(0);
        if (jobList.get(jobList.indexOf(new Job(firstOperation.getJob()))).getPreJobs().size() == 0
                && firstOperation.getNumber() == 0) {
            firstOperation.setStatus("prepared");
        }


    }

    public List<Operation> getOperationList() {
        return operationList;
    }

    public List<Job> getJobList() {
        return jobList;
    }

    @Scheduled(fixedDelay = 20000, initialDelay = 15000)
    public void clearUnnecessaryJobs() {
        Set<Job> usefulJobs = new HashSet<>();
        for (Operation operation : operationList) {
            Job job = new Job(operation.getJob());
            usefulJobs.add(job);
            for (Long preJob : jobList.get(jobList.indexOf(job)).getPreJobs()) {
                usefulJobs.add(new Job(preJob));
            }
        }
        jobList.removeIf(job -> !usefulJobs.contains(job));
    }

    public void clearFinishedOperations() {
        int index = 0;
        while (index < operationList.size() && operationList.get(index).getStatus().equals("finished")) {
            ++index;
        }
        // remove finished operations.
        if (operationList.size() > 0) {
            List<Operation> subList = operationList.subList(index, operationList.size());
            operationList.clear();
            operationList.addAll(subList);
        }
    }
}
