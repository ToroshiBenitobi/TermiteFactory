package org.anar.termitefactory.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.anar.termitefactory.entity.schedule.Blueprint;
import org.anar.termitefactory.entity.schedule.Component;
import org.anar.termitefactory.entity.schedule.Machine;
import org.anar.termitefactory.entity.schedule.Procedure;
import org.anar.termitefactory.repository.BluePrintRepository;
import org.anar.termitefactory.repository.MachineRepository;
import org.anar.termitefactory.util.Job;
import org.anar.termitefactory.util.JobArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import java.util.*;

@Service
public class GenerateProblemService {
    private static final double[] NaNPIC = {Double.NaN, Double.NaN};
    private static final boolean[] NaNPDC = {true, true, true, true, true, true, true};

    @Autowired
    BluePrintRepository bluePrintRepository;
    @Autowired
    MachineRepository machineRepository;
    @Autowired
    MachineService machineService;

    public String generateProblem(JsonNode jsonNode) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode projects = jsonNode.get("projects");

        List<Blueprint> firstList = new ArrayList<>();
        for (JsonNode project : projects) {
            String code = project.get("code").asText();
            int number = project.get("number").asInt();
            Blueprint blueprint = bluePrintRepository.findByCode(code);
            for (int i = 0; i < number; i++) {
                firstList.add(blueprint);
            }
        }

        Date currentDate = new Date(jsonNode.get("startTime").asLong());
        double startTime = currentDate.getHours() * 3600 + currentDate.getMinutes() * 60 + currentDate.getSeconds();
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        int startDay = c.get(Calendar.DAY_OF_WEEK) - 1; // 0 for Sun.
        double transportationRate = jsonNode.get("transportationRate").asDouble();

        JobArray jobArray = getJobArray(firstList);

        // ground config
        ObjectNode root = mapper.createObjectNode();
        ObjectNode nameMaps = root.putObject("nameMaps");
        ArrayNode machineNameMap = nameMaps.putArray("machineNameMap");
        ArrayNode taskNameMap = nameMaps.putArray("taskNameMap");

        root.put("startTime", startTime);
        root.put("startDay", startDay);
        root.put("transportationRate", transportationRate);

        // jobs
        ArrayNode numberOfOperations = mapper.createArrayNode();
        ArrayNode isPCO = mapper.createArrayNode();
        ArrayNode processingIntervalConstraint = mapper.createArrayNode();
        ArrayNode processingDayConstraint = mapper.createArrayNode();
        ArrayNode rawMaterialCost = mapper.createArrayNode();
        ArrayNode processTime = mapper.createArrayNode();
        for (Job job : jobArray.getJobList()) {
            numberOfOperations.add(job.getNumberOfOperation());
            rawMaterialCost.add(job.getRawMaterialCost());
            isPCO.add(mapper.valueToTree(job.getIsPCO()));
            ArrayNode subPIC = mapper.createArrayNode();
            ArrayNode subPDC = mapper.createArrayNode();
            ArrayNode subPT = mapper.createArrayNode();
            ObjectNode nameMap = taskNameMap.addObject();

            for (double[] doubles : job.getProcessingIntervalConstraint()) {
                ArrayNode bin = mapper.valueToTree(NaNPIC);
                if (doubles != null) {
                    bin = mapper.valueToTree(doubles);
                }
                subPIC.add(bin);
            }

            for (boolean[] booleans : job.getProcessingDayConstraint()) {
                ArrayNode bin = mapper.valueToTree(NaNPDC);
                if (booleans != null) {
                    bin = mapper.valueToTree(booleans);
                }
                subPDC.add(bin);
            }

            for (Map<Integer, Double> map : job.getProcessTime()) {
                ObjectNode bin = mapper.createObjectNode();
                for (Integer integer : map.keySet()) {
                    bin.put(String.valueOf(integer), map.get(integer));
                }
                subPT.add(bin);
            }

            nameMap.put("jobCode", job.getBlueprint().getCode());
            nameMap.put("jobName", job.getBlueprint().getName());
            ArrayNode operations = nameMap.putArray("operations");

            // todo: add name map
            for (Procedure procedure : job.getBlueprint().getProcedure()) {
                ObjectNode operation = operations.addObject();
                operation.put("procedureName", procedure.getName());
                operation.put("procedureCode", procedure.getCode());
            }

            processingIntervalConstraint.add(subPIC);
            processingDayConstraint.add(subPDC);
            processTime.add(subPT);
        }
        root.putArray("numberOfOperations").addAll(numberOfOperations);
        root.putArray("isPCO").addAll(isPCO);
        root.putArray("processingIntervalConstraint").addAll(processingIntervalConstraint);
        root.putArray("processingDayConstraint").addAll(processingDayConstraint);
        root.putArray("rawMaterialCost").addAll(rawMaterialCost);
        root.putArray("processTime").addAll(processTime);


        // machine
        ArrayNode staticWaitingRate = mapper.createArrayNode();
        ArrayNode dynamicProcessingRate = mapper.createArrayNode();
        for (Machine machine : jobArray.getMachineList()) {
            staticWaitingRate.add(machine.getStaticWaitingRate());
            dynamicProcessingRate.add(machine.getDynamicProcessingRate());
            machineNameMap.add(machine.getNumber());
        }
        root.putArray("staticWaitingRate").addAll(staticWaitingRate);
        root.putArray("dynamicProcessingRate").addAll(dynamicProcessingRate);

        // next job
        ArrayNode nextJob = mapper.valueToTree(jobArray.getNextJob());
        root.putArray("nextJob").addAll(nextJob);

        // job transportation time
        ArrayNode jobTransportationTime = mapper.createArrayNode();
        for (double[] doubles : jobArray.getJobTransportationTime()) {
            jobTransportationTime.add(mapper.valueToTree(doubles));
        }
        root.putArray("jobTransportationTime").addAll(jobTransportationTime);

        return mapper.writeValueAsString(root);
    }

    public List<Blueprint> getSubBlueprint(Blueprint blueprint) {
        List<Blueprint> subBlueprintList = new ArrayList<>();
        if (!blueprint.isRaw()) {
            List<Component> componentList = blueprint.getComponents();
            for (Component component : componentList) {
                Blueprint subBlueprint = bluePrintRepository.findByCode(component.getCode());
                for (int i = 0; i < component.getNumber(); i++) {
                    subBlueprintList.add(subBlueprint);
                }
            }
        }
        return subBlueprintList;
    }

    public List<Blueprint> getAllBlueprints(List<Blueprint> firstList) {
        boolean flag = true;
        List<Blueprint> blueprintList = firstList;
        int size = 0;
        while (flag) {
            List<Blueprint> subList = new ArrayList<>();
            for (int i = size; i < blueprintList.size(); i++) {
                subList.addAll(getSubBlueprint(blueprintList.get(i)));
            }
            flag = false;
            for (Blueprint blueprint : subList) {
                flag |= blueprint.isRaw();
            }
            size = blueprintList.size();
            blueprintList.addAll(subList);
        }
        return blueprintList;
    }

    public List<Blueprint> getNonRawAllBlueprints(List<Blueprint> firstList) {
        List<Blueprint> blueprintList = getAllBlueprints(firstList);
        List<Blueprint> newList = new ArrayList<>();
        for (Blueprint blueprint : blueprintList) {
            if (!blueprint.isRaw()) newList.add(blueprint);
        }
        return newList;
    }

    public void setAllBlueprints(JobArray jobArray) {
        List<Integer> nextJob = new ArrayList<>();
        List<Blueprint> firstList = jobArray.getBlueprintList();
        boolean flag = true;
        List<Blueprint> blueprintList = firstList;

        int size = 0;
        for (int i = 0; i < blueprintList.size(); i++) {
            nextJob.add(-1);
        }

        while (flag) {
            List<Blueprint> subList = new ArrayList<>();
            for (int i = size; i < blueprintList.size(); i++) {
                List<Blueprint> subSubList = getSubBlueprint(blueprintList.get(i));
                subList.addAll(subSubList);
                for (int j = 0; j < subSubList.size(); j++) {
                    nextJob.add(i);
                }
            }
            flag = false;
            for (Blueprint blueprint : subList) {
                flag |= blueprint.isRaw();
            }
            size = blueprintList.size();
            blueprintList.addAll(subList);
        }

        List<Blueprint> newList = new ArrayList<>();
        List<Integer> newNextJob = new ArrayList<>();
        int[] drift = new int[blueprintList.size()];
        for (int i = 0; i < blueprintList.size(); i++) {
            if (!blueprintList.get(i).isRaw()) {
                newList.add(blueprintList.get(i));
                if (nextJob.get(i) != -1) {
                    newNextJob.add(nextJob.get(i) - drift[nextJob.get(i)]);
                } else {
                    newNextJob.add(-1);
                }

            } else {
                for (int j = i; j < size; j++) {
                    ++drift[j];
                }
            }
        }

        Collections.reverse(newList);
        Collections.reverse(newNextJob);

        // change next job to the right location.
        int sum = newNextJob.size() - 1;
        for (int i = 0; i < newNextJob.size(); i++) {
            if (newNextJob.get(i) != -1) {
                newNextJob.set(i, sum - newNextJob.get(i));
            }
        }

        jobArray.setBlueprintList(newList);
        jobArray.setNextJob(newNextJob.stream().mapToInt(i -> i).toArray());
    }

    public double getRawMaterialPrice(Blueprint blueprint) {
        double sum = 0;
        List<Blueprint> blueprintList = getSubBlueprint(blueprint);
        for (Blueprint blueprint1 : blueprintList) {
            if (blueprint1.isRaw())
                sum += blueprint1.getPrice();
        }
        return sum;
    }

    public JobArray getJobArray(List<Blueprint> firstList) {
        JobArray jobArray = new JobArray(firstList);
        setAllBlueprints(jobArray);
        List<Blueprint> blueprintList = jobArray.getBlueprintList();
        List<Job> jobList = new ArrayList<>(blueprintList.size());
        List<Machine> machineList = new ArrayList<>();
        for (int i = 0; i < blueprintList.size(); i++) {
            Job job = new Job();
            Blueprint blueprint = blueprintList.get(i);
            List<Procedure> procedures = blueprint.getProcedure();

            int numberOfOperation = procedures.size();
            double[][] processingIntervalConstraint = new double[numberOfOperation][];
            boolean[][] processingDayConstraint = new boolean[numberOfOperation][];
            boolean[] isPCO = new boolean[numberOfOperation];
            List<Map<Integer, Double>> processTime = new ArrayList<>();
            double rawMaterialCost = 0;

            for (int j = 0; j < procedures.size(); j++) {
                isPCO[j] = procedures.get(j).isPCD();
                if (isPCO[j]) {
                    processingIntervalConstraint[j] = procedures.get(j).getInterval();
                    processingDayConstraint[j] = procedures.get(j).getDay();
                }
                Map<Integer, Double> map = new HashMap<>();
                List<Machine> matchedMachineList = machineService.getMatchedMachine(procedures.get(j).getTag());
                for (Machine machine : matchedMachineList) {
                    int index;
                    index = machineList.indexOf(machine);
                    // add machine to machineList.
                    if (index == -1) {
                        index = machineList.size();
                        machineList.add(machine);
                    }
                    double time = machine.getProcessTime(blueprint.getCode(), procedures.get(j).getCode()).getSpan();
                    map.put(index, time);
                }
                processTime.add(map);
            }

            // add raw material cost.
            rawMaterialCost = getRawMaterialPrice(blueprint);

            job.setBlueprint(blueprint);
            job.setNumberOfOperation(numberOfOperation);
            job.setProcessTime(processTime);
            job.setIsPCO(isPCO);
            job.setProcessingIntervalConstraint(processingIntervalConstraint);
            job.setProcessingDayConstraint(processingDayConstraint);
            job.setRawMaterialCost(rawMaterialCost);

            jobList.add(job);
        }

        jobArray.setJobList(jobList);
        jobArray.setMachineList(machineList);

        // Job Transportation Time
        double[][] jobTransportationTime = new double[machineList.size()][machineList.size()];
        for (int i = 0; i < machineList.size(); i++) {
            for (int j = 0; j < machineList.size(); j++) {
                Machine m1 = machineList.get(i);
                Machine m2 = machineList.get(j);
                double time =
                        Math.sqrt(Math.pow(m1.getLocationX() - m2.getLocationX(), 2)
                                + Math.pow(m1.getLocationY() - m2.getLocationY(), 2)) * 360;

                // min time
                if (time < 120) {
                    time = 120;
                }
                jobTransportationTime[i][j] = time;
            }
        }
        jobArray.setJobTransportationTime(jobTransportationTime);

        return jobArray;
    }
}
