package org.anar.scheduling.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.anar.scheduling.object.*;
import org.anar.scheduling.problem.SchedulingProblem;
import org.anar.scheduling.solution.SchedulingSolution;

import java.util.*;

public class SchedulingSolutionConverter {
    static public List<Operation>[] decodeToMachineAllocation(SchedulingSolution solution, ProcessTime processTime, int numberOfMachines) {
        List<Operation>[] allocation = new List[numberOfMachines];
        for (int i = 0; i < numberOfMachines; i++) {
            allocation[i] = new ArrayList<>();
        }
        int half = solution.variables().size() / 2;
        int[] counter = new int[solution.jobs().length];
        for (int i = 0; i < half; i++) {
            int job = solution.variables().get(i);
            int allocate = solution.variables().get(i + half);
            Set<Integer> ys = processTime.getAlternativeMachineSet(job, counter[job]);
            Integer[] alterNumbers = ys.toArray(new Integer[ys.size()]);
            Arrays.sort(alterNumbers);
            int allocatedMachine = alterNumbers[allocate];

            allocation[allocatedMachine].add(new Operation(job, counter[job]));

            ++counter[job];
        }
        return allocation;
    }

    static public List<Integer>[] decode(SchedulingSolution solution, ProcessTime processTime, int numberOfMachines) {
        List<Integer>[] allocation = new List[solution.jobs().length];
        for (int i = 0; i < solution.jobs().length; i++) {
            allocation[i] = new ArrayList<>();
        }
        int half = solution.variables().size() / 2;
        int[] counter = new int[solution.jobs().length];
        for (int i = 0; i < half; i++) {
            int job = solution.variables().get(i);
            int allocate = solution.variables().get(i + half);
            Set<Integer> ys = processTime.getAlternativeMachineSet(job, counter[job]);
            Integer[] alterNumbers = ys.toArray(new Integer[ys.size()]);
            Arrays.sort(alterNumbers);
            int allocatedMachine = alterNumbers[allocate];

            allocation[job].add(allocatedMachine);

            ++counter[job];
        }
        return allocation;
    }

    static public String convertToJson(
            SchedulingSolution solution,
            SchedulingProblem problem,
            JsonNode nameMaps) {
        return convertToJson(solution, problem.getProcessTime(), problem.getJobTransportationTime(),
                problem.getProcessingIntervalConstraint(), problem.getIsPCO(), problem.getStartTime(), problem, nameMaps);
    }

    static public String convertToJson(
            SchedulingSolution solution,
            ProcessTime processTime,
            Double[][] jobTransportationTime,
            Double[][][] processingIntervalConstraint,
            Boolean[][] isPCO,
            Double startTime,
            SchedulingProblem problem,
            JsonNode nameMaps) {
        JsonNode machineNameMap = nameMaps.get("machineNameMap");
        JsonNode taskNameMap = nameMaps.get("taskNameMap");
        int[] nextJob = problem.getNextJob();

        List<Double>[] complicationTime = new List[solution.jobs().length];

        int numberOfMachines = jobTransportationTime.length;
        List<Integer>[] allocation = decode(solution, processTime, numberOfMachines);
        List<Operation>[] machineAllocation = decodeToMachineAllocation(solution, processTime, numberOfMachines);
        for (int i = 0; i < complicationTime.length; i++) {
            complicationTime[i] = new ArrayList<>();
        }
        double[] jobStartTime = new double[solution.jobs().length];
        List<Double>[] delays = new List[solution.jobs().length];
        for (int i = 0; i < delays.length; i++) {
            delays[i] = new ArrayList<>();
            delays[i].add(0.0);
        }
        problem.computeComplicationTime(complicationTime, jobStartTime, delays, solution, allocation, machineAllocation);

// todo: add nameMap
        Allocation allocationChart = new Allocation();
        for (int i = 0; i < machineAllocation.length; i++) {
            Machine machine = new Machine(i, machineNameMap.get(i).asText());
            for (Operation operation : machineAllocation[i]) {
                operation.setStartTime(complicationTime[operation.getJob()].get(operation.getNumber()) - processTime.getProcessTime(operation.getJob(), operation.getNumber(), i));
                operation.setProcessTime(processTime.getProcessTime(operation.getJob(), operation.getNumber(), i));

                // only one operation.
                if (allocation[operation.getJob()].size() == 1) {
                    if (nextJob[operation.getJob()] != -1) {
                        int next = nextJob[operation.getJob()];
                        int nextMachine = allocation[next].get(0);
                        operation.setNextMachine(machineNameMap.get(nextMachine).asText());
                    } else {
                        operation.setNextMachine("");
                    }
                    operation.setPreMachine("");
                    operation.setTransportTime(0);
                } else {
                    if (allocation[operation.getJob()].size() > operation.getNumber() + 1 && operation.getNumber() != 0) {

                        operation.setNextMachine(machineNameMap.get(allocation[operation.getJob()].get(operation.getNumber() + 1)).asText());
                        operation.setPreMachine(machineNameMap.get(allocation[operation.getJob()].get(operation.getNumber() - 1)).asText());
                        operation.setTransportTime(jobTransportationTime[i][allocation[operation.getJob()].get(operation.getNumber() + 1)]);
                    } else if (operation.getNumber() == 0) {
                        operation.setNextMachine(machineNameMap.get(allocation[operation.getJob()].get(operation.getNumber() + 1)).asText());
                        operation.setPreMachine("");
                        operation.setTransportTime(jobTransportationTime[i][allocation[operation.getJob()].get(operation.getNumber() + 1)]);
                    } else {
                        if (nextJob[operation.getJob()] != -1) {
                            int next = nextJob[operation.getJob()];
                            int nextMachine = allocation[next].get(0);
                            operation.setNextMachine(machineNameMap.get(nextMachine).asText());
                        } else {
                            operation.setNextMachine("");
                        }
                        operation.setPreMachine(machineNameMap.get(allocation[operation.getJob()].get(operation.getNumber() - 1)).asText());
                        operation.setTransportTime(0);
                    }
                }
                operation.setOperationCode(taskNameMap.get(operation.getJob()).get("operations").get(operation.getNumber()).get("procedureCode").asText());
                operation.setOperationName(taskNameMap.get(operation.getJob()).get("operations").get(operation.getNumber()).get("procedureName").asText());
                machine.addOperation(operation);
            }
            allocationChart.addMachine(machine);
        }

        List<Job> jobList = new ArrayList<>();
        for (int i = 0; i < problem.getNumberOfJobs(); i++) {
            Job job = new Job();
            job.setIndex(i);
            job.setJobCode(taskNameMap.get(i).get("jobCode").asText());
            job.setJobName(taskNameMap.get(i).get("jobName").asText());
            job.setNumberOfOperations(problem.getNumberOfOperations()[i]);
            job.setNextJob(problem.getNextJob()[i]);
            List<Integer> preJobs = new ArrayList<>();
            for (int j = 0; j < problem.getNumberOfJobs(); j++) {
                if (problem.getNextJob()[j] == i)
                    preJobs.add(j);
            }
            job.setPreJobs(preJobs);
            jobList.add(job);
        }


        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        ObjectNode allocationNode = mapper.valueToTree(allocationChart);
        ArrayNode jobListNode = mapper.valueToTree(jobList);

        root.put("allocation", allocationNode);
        root.put("jobList", jobListNode);

        String json = null;
        try {
            json = mapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }

}
