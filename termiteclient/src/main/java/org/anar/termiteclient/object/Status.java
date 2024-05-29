package org.anar.termiteclient.object;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.anar.termiteclient.config.MqttType;
import org.anar.termiteclient.controller.TaskController;
import org.anar.termiteclient.service.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Component
public class Status {
    private static final Logger logger = LoggerFactory.getLogger(Status.class);
    @Autowired
    OperationStatus operationStatus;
    @Autowired
    MqttClient client;
    @Autowired
    ObjectMapper objectMapper;

    String status = "idling";
    double consumption = 0;
    double hourConsumption = 0;
    double hourlyConsumption = 0;
    long runningTime = 0;
    long idlingTime = 0;
    boolean isLocked = false;
    boolean isRestored = false;

    public Status() {
    }

    public void restoreStatus() {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", MqttType.RESTORE_QUEST);
        root.put("number", client.getId());
        try {
            client.publish("machine/" + client.getId(), objectMapper.writeValueAsString(root));
            logger.info("Restoring the status");
        } catch (MqttException | JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedDelay = 20000, initialDelay = 10000)
    public void publishStatus() throws JsonProcessingException, MqttException {
        if (!isRestored) {
            restoreStatus();
            return;
        }
        JsonNode root = getStatusNode();
        client.publish("status", objectMapper.writeValueAsString(root));
        logger.info("Complete the routine status publishing.");
    }

    public JsonNode getStatusNode() {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("number", client.getId());
        root.put("status", status);
        root.put("consumption", consumption);
        root.put("hourlyConsumption", hourlyConsumption);
        root.put("runningTime", runningTime);
        root.put("idlingTime", idlingTime);
        root.put("type", MqttType.STATUS);
        ArrayNode operations = root.putArray("operations");

        for (Operation operation : operationStatus.getOperationList()) {
            operations.add(objectMapper.valueToTree(operation));
        }

        // put in the time stamp.
        root.put("timeStamp", Calendar.getInstance().getTimeInMillis());

        return root;
    }

    @Scheduled(fixedRate = 30000, initialDelay = 10000)
    public void accumulateTime() {
        if (status.equals("running")) {
            runningTime += 30;
            hourConsumption += 0.1;
            consumption += 0.1;
        } else if (status.equals("idling")) {
            idlingTime += 30;
            hourConsumption += 0.01;
            consumption += 0.01;
        }
    }

    @Scheduled(fixedRate = 36000000, initialDelay = 10000)
    public void computeHourlyConsumption() {
        hourlyConsumption = hourConsumption;
        hourConsumption = 0;
    }


    // todo: test
    @Scheduled(fixedDelay = 2000, initialDelay = 10000)
    public void testRunning() throws JsonProcessingException, MqttException {
        if (isLocked) return;
        boolean isFinished = true;
        List<Operation> operationList = operationStatus.getOperationList();
        int index = 0;
        while (index < operationList.size()) {
            Operation operation = operationList.get(index);
            String status = operation.getStatus();
            if (status.equals("finished")) {
                ++index;
            } else {
                isFinished = false;
                if (status.equals("scheduled")) {
                    List<Job> jobList = operationStatus.getJobList();
                    if (jobList.get(jobList.indexOf(new Job(operation.getJob()))).getPreJobs().size() == 0
                            && operation.getNumber() == 0) {
                        operation.setStatus("prepared");
                    } else {
                        operation.setStatus("waiting");
                    }
                } else if (status.equals("prepared")) {
                    operation.setOperationStartTime(LocalDateTime.now());
                    operation.setStatus("processing");
                } else if (status.equals("processing")) {
                    operation.setProcessCompletedTime(LocalDateTime.now());
                    operation.setStatus("transporting");
                } else if (status.equals("transporting")) {
                    operation.setTransportCompletedTime(LocalDateTime.now());
                    operation.setStatus("finished");
                    ObjectNode root = objectMapper.createObjectNode();
                    root.put("type", MqttType.OPERATION_STATUS_CHANGED);
                    root.put("job", operation.getJob());
                    root.put("number", operation.getNumber());
                    root.put("status", "finished");
                    if (!operation.getNextMachine().isEmpty())
                        client.publish("machine/" + operation.getNextMachine(), getObjectMapper().writeValueAsString(root));
                }

                // Set the machine status.
                if (operation.getStatus().equals("waiting")) {
                    setStatus("idling");
                } else {
                    setStatus("running");
                    publishStatus();
                }

                break;
            }
        }

        if (isFinished) {
            setStatus("idling");
        } else {
            setStatus("running");
        }
    }


    public OperationStatus getOperationStatus() {
        return operationStatus;
    }

    public void setOperationStatus(OperationStatus operationStatus) {
        this.operationStatus = operationStatus;
    }

    public MqttClient getClient() {
        return client;
    }

    public void setClient(MqttClient client) {
        this.client = client;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getConsumption() {
        return consumption;
    }

    public void setConsumption(double consumption) {
        this.consumption = consumption;
    }

    public double getHourlyConsumption() {
        return hourlyConsumption;
    }

    public void setHourlyConsumption(double hourlyConsumption) {
        this.hourlyConsumption = hourlyConsumption;
    }

    public long getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(long runningTime) {
        this.runningTime = runningTime;
    }

    public long getIdlingTime() {
        return idlingTime;
    }

    public void setIdlingTime(long idlingTime) {
        this.idlingTime = idlingTime;
    }

    public double getHourConsumption() {
        return hourConsumption;
    }

    public void setHourConsumption(double hourConsumption) {
        this.hourConsumption = hourConsumption;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public boolean isRestored() {
        return isRestored;
    }

    public void setRestored(boolean restored) {
        isRestored = restored;
    }
}
