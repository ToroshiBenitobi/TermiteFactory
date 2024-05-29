package org.anar.termitefactory.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.anar.termitefactory.entity.EdgeMachine;
import org.anar.termitefactory.entity.Job;
import org.anar.termitefactory.entity.Procedure;
import org.anar.termitefactory.entity.schedule.Blueprint;
import org.anar.termitefactory.repository.BluePrintRepository;
import org.anar.termitefactory.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class JobService {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    JobRepository jobRepository;
    @Autowired
    BluePrintRepository bluePrintRepository;

    public JsonNode solidifyAllocation(JsonNode node) throws JsonProcessingException {
        ObjectNode root = objectMapper.createObjectNode();

        // secure the uniqueness.
        long size = Calendar.getInstance().getTimeInMillis();
        ArrayNode jobs = node.get("jobList").deepCopy();
        List<Job> jobList = new ArrayList<>();
        for (int i = 0; i < jobs.size(); i++) {
            ObjectNode newJob = jobs.get(i).deepCopy();
            long index = newJob.get("index").asLong();
            long nextJob = newJob.get("nextJob").asLong();
            newJob.put("index", index + size);
            if (nextJob != -1)
                newJob.put("nextJob", nextJob + size);
            ArrayNode preJobs = newJob.get("preJobs").deepCopy();
            if (preJobs.size() > 0) {
                for (int j = 0; j < preJobs.size(); j++) {
                    preJobs.set(j, preJobs.get(j).asLong() + size);
                }
            }
            newJob.set("preJobs", preJobs);
            jobs.set(i, newJob);

            Job job = new Job();
            job.setIndex(newJob.get("index").asLong());
            job.setNextJob(newJob.get("nextJob").asLong());
            job.setJobCode(newJob.get("jobCode").asText());
            job.setStatus("scheduled");
            job.setPreJobs(objectMapper.treeToValue(newJob.get("preJobs"), List.class));

            Blueprint blueprint = bluePrintRepository.findByCode(job.getJobCode());
            List<Procedure> procedureList = new ArrayList<>();
            for (int j = 0; j < blueprint.getProcedure().size(); j++) {
                Procedure procedure = new Procedure();
                procedure.setStatus("scheduled");
                procedure.setJob(index + size);
                procedure.setNumber(j);
                procedureList.add(procedure);
            }
            job.setProcedures(procedureList);

            jobList.add(job);
        }


        ArrayNode machines = node.get("allocation").get("machines").deepCopy();
        for (int i = 0; i < machines.size(); i++) {
            ObjectNode machine = machines.get(i).deepCopy();
            ArrayNode operations = machine.get("operations").deepCopy();
            for (int j = 0; j < operations.size(); j++) {
                ObjectNode operation = operations.get(j).deepCopy();

                // solidification
                Procedure procedure = jobList.get(operation.get("job").asInt()).getProcedures().get(operation.get("number").asInt());
                procedure.setPreMachine(operation.get("preMachine").asText());
                procedure.setNextMachine(operation.get("nextMachine").asText());
                procedure.setMachine(machine.get("name").asText());

                operation.put("job", operation.get("job").asLong() + size);
                operations.set(j, operation);


            }
            machine.set("operations", operations);
            machines.set(i, machine);
        }

        root.putObject("allocation").set("machines", machines);
        root.set("jobList", jobs);

        jobRepository.saveAll(jobList);
        return root;
    }

    public JsonNode getJobStatuses() {
        int sumOfScheduledJob = 0;
        int sumOfRunningJob = 0;
        int sumOfFinished = 0;

        for (Job job : jobRepository.findAll()) {
            String status = job.getStatus();
            if (status.equals("scheduled")) {
                ++sumOfScheduledJob;
            } else if (status.equals("running")) {
                ++sumOfRunningJob;
            } else if (status.equals("finished")) {
                ++sumOfFinished;
            }
        }

        ObjectNode jobStatuses = objectMapper.createObjectNode();
        jobStatuses.put("scheduled", sumOfScheduledJob);
        jobStatuses.put("running", sumOfRunningJob);
        jobStatuses.put("finished", sumOfFinished);

        return jobStatuses;
    }

    public List<Job> getJobList() {
        return jobRepository.findAll();
    }

    public Job getJob(long index) {
        return jobRepository.findByIndex(index);
    }

    public List<Job> getJobList(String status) {
        return jobRepository.findAllByStatus(status);
    }

    public List<Job> getJobListNot(String status) {
        return jobRepository.findAllByStatusNot(status);
    }
}
