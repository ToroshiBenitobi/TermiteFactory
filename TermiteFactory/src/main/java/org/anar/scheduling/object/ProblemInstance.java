package org.anar.scheduling.object;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import java.io.*;
import java.util.*;

public class ProblemInstance {
    Double startTime;
    int startDay;
    ProcessTime processTimeTable;
    int[] numberOfOperations;
    int[] nextJob;
    Double[][] jobTransportationTime;
    Double[][][] processingIntervalConstraint;
    Boolean[][][] processingDayConstraint;
    Boolean[][] isPCO;
    Double[] staticWaitingRate;
    Double[] dynamicProcessingRate;
    Double[] rawMaterialCost;
    Double transportationRate;
    public List<Map<Integer, Double>>[] processTime;

    public ProblemInstance() {
    }

    public void readJsonFile(String path) throws IOException {
        File file = new File(new DefaultFileOutputContext(path).getFileName());
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuilder json = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            json.append(line.trim());
        }
        readJson(json.toString());
    }

    public void readJson(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(json);
        JsonNode numberOfOperations = jsonNode.get("numberOfOperations");
        JsonNode nextJob = jsonNode.get("nextJob");
        JsonNode processTimeTable = jsonNode.get("processTime");
        JsonNode jobTransportationTime = jsonNode.get("jobTransportationTime");
        JsonNode processingIntervalConstraint = jsonNode.get("processingIntervalConstraint");
        JsonNode processingDayConstraint = jsonNode.get("processingDayConstraint");
        JsonNode isPCO = jsonNode.get("isPCO");
        JsonNode staticWaitingRate = jsonNode.get("staticWaitingRate");
        JsonNode dynamicProcessingRate = jsonNode.get("dynamicProcessingRate");
        JsonNode rawMaterialCost = jsonNode.get("rawMaterialCost");

        this.startTime = jsonNode.get("startTime").asDouble();
        this.startDay = jsonNode.get("startDay").asInt();
        this.numberOfOperations = mapper.treeToValue(numberOfOperations, int[].class);
        this.nextJob = mapper.treeToValue(nextJob, int[].class);
        this.jobTransportationTime = mapper.treeToValue(jobTransportationTime, Double[][].class);
        this.processingIntervalConstraint = mapper.treeToValue(processingIntervalConstraint, Double[][][].class);
        this.processingDayConstraint = mapper.treeToValue(processingDayConstraint, Boolean[][][].class);
        this.isPCO = mapper.treeToValue(isPCO, Boolean[][].class);
        this.staticWaitingRate = mapper.treeToValue(staticWaitingRate, Double[].class);
        this.dynamicProcessingRate = mapper.treeToValue(dynamicProcessingRate, Double[].class);
        this.rawMaterialCost = mapper.treeToValue(rawMaterialCost, Double[].class);
        this.transportationRate = jsonNode.get("transportationRate").asDouble();
//        processTime = (List<Map<Integer, Double>>[]) mapper.treeToValue(processTimeTable,List[].class);

        processTime = new List[processTimeTable.size()];
        for (int i = 0; i < processTimeTable.size(); i++) {
            List<Map<Integer, Double>> job = new ArrayList<>();
            JsonNode jobNode = processTimeTable.get(i);
            for (int j = 0; j < jobNode.size(); j++) {
                String operationNode = jobNode.get(j).toString();
                Map<Integer, Double> operation =
                        mapper.readValue(operationNode, new TypeReference<Map<Integer, Double>>() {
                        });
                job.add(operation);
            }
            processTime[i] = job;
        }
        this.processTimeTable = new ProcessTime(processTime);

        Set<Integer> ys = this.processTimeTable.getAlternativeMachineSet(1, 1);
        Integer[] alterNumbers = ys.toArray(new Integer[ys.size()]);
    }

    public ProcessTime getProcessTimeTable() {
        return processTimeTable;
    }

    public int[] getNumberOfOperations() {
        return numberOfOperations;
    }

    public int[] getNextJob() {
        return nextJob;
    }

    public Double[][] getJobTransportationTime() {
        return jobTransportationTime;
    }

    public Double[][][] getProcessingIntervalConstraint() {
        return processingIntervalConstraint;
    }

    public Boolean[][] getIsPCO() {
        return isPCO;
    }

    public List<Map<Integer, Double>>[] getProcessTime() {
        return processTime;
    }

    public Double getStartTime() {
        return startTime;
    }

    public int getStartDay() {
        return startDay;
    }

    public Double[] getStaticWaitingRate() {
        return staticWaitingRate;
    }

    public Double[] getDynamicProcessingRate() {
        return dynamicProcessingRate;
    }

    public Boolean[][][] getProcessingDayConstraint() {
        return processingDayConstraint;
    }

    public Double[] getRawMaterialCost() {
        return rawMaterialCost;
    }

    public Double getTransportationRate() {
        return transportationRate;
    }
}
