package org.anar.termiteclient.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBufUtil;
import org.anar.termiteclient.config.MqttType;
import org.anar.termiteclient.object.Job;
import org.anar.termiteclient.object.Operation;
import org.anar.termiteclient.object.OperationStatus;
import org.anar.termiteclient.object.Status;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

@Service
public class MqttClient implements MqttCallback {
    private static final Logger logger = LoggerFactory.getLogger(MqttClient.class);
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    OperationStatus operationStatus;
    @Autowired
    Status status;

    @Value("${mqtt-server.address}")
    String address;
    @Value("${mqtt-server.port}")
    int port;
    String id;

    org.eclipse.paho.client.mqttv3.MqttClient client;


    public void connect(String id) throws MqttException {
        this.id = id;
        connect();
    }

    public void connect() throws MqttException {
        String url = "tcp://" + address + ":" + Integer.toString(port);

        client = new org.eclipse.paho.client.mqttv3.MqttClient(url, id);
        client.connect();
        client.setCallback(this);
        client.subscribe("machine/" + id);
        client.subscribe("machine");
    }

    public void subscribe(String topic) throws MqttException {
        client.subscribe(topic);
    }

    public void publish(String topic, String payload) throws MqttException {
        MqttMessage message = new MqttMessage();
        message.setPayload(payload
                .getBytes());
        message.setQos(2);
        try {
            if (!client.isConnected()) {
                connect();
                logger.info("reconnected.");
            }
            client.publish(topic, message);
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        }
    }

    public void disConnect() throws MqttException {
        client.disconnect();
    }

    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        logger.info(new String(mqttMessage.getPayload()));
        JsonNode payload = objectMapper.readTree(new String(mqttMessage.getPayload()));
        String type = payload.get("type").asText();
        if (type.equals(MqttType.ALLOCATION)) {
            Long timeStamp = payload.get("timeStamp").asLong();
            JsonNode operations = payload.get("operations");
            JsonNode jobs = payload.get("jobs");
            status.setLocked(true);
            logger.info("Stop to process.");
            operationStatus.readNewSchedule(timeStamp, operations, jobs);
        } else if (type.equals(MqttType.OPERATION_STATUS_CHANGED)) {
            String status = payload.get("status").asText();
            List<Job> jobList = operationStatus.getJobList();
            long job = payload.get("job").asLong();
            int number = payload.get("number").asInt();
            if (status.equals("finished")) {
                int index = jobList.indexOf(new Job(job));
                if (index == -1) return;
                if (jobList.get(index).getNumberOfOperations() == number + 1) {
                    boolean flag = false;
                    for (int i = 0; i < jobList.size(); i++) {
                        Job job1 = jobList.get(i);
                        for (int j = 0; j < job1.getPreJobs().size(); j++) {
                            if (job1.getPreJobs().get(j) == job) {
                                job1.getPreJobs().remove(j);
                                flag = true;
                                break;
                            }
                        }
                        // if all previous jobs finished.
                        if (job1.getPreJobs().size() == 0 && flag) {
                            for (Operation operation : operationStatus.getOperationList()) {
                                if (operation.getJob() == job1.getIndex())
                                    operation.setStatus("prepared");
                            }
                        }
                    }
                } else {
                    for (Operation operation : operationStatus.getOperationList()) {
                        if (operation.getJob() == job && operation.getNumber() == number + 1) {
                            operation.setStatus("prepared");
                            break;
                        }
                    }
                }
            }
        } else if (type.equals(MqttType.RESTORE)) {
            status.setConsumption(payload.get("status").get("consumption").asDouble());
            status.setHourlyConsumption(payload.get("status").get("hourlyConsumption").asDouble());
            status.setRunningTime(payload.get("status").get("runningTime").asLong());
            status.setIdlingTime(payload.get("status").get("idlingTime").asLong());
            status.setRestored(true);
//            for (JsonNode operation : payload.get("procedureList")) {
//                Operation o = objectMapper.treeToValue(operation, Operation.class);
//                operationStatus.getOperationList().add(o);
//            }
            // todo: restore jobs.
        } else if (type.equals(MqttType.COMMENCE)) {
            logger.info("Begin to process.");
            status.setLocked(false);
        } else if (type.equals(MqttType.CLEAR_FINISHED_OPERATION)) {
            operationStatus.clearFinishedOperations();
            status.publishStatus();
        } else if (type.equals(MqttType.DELETE)) {
            List<Operation> operationList = operationStatus.getOperationList();
            operationList.remove(new Operation(payload.get("index").asLong(), payload.get("number").asInt()));
        } else if (type.equals(MqttType.CHECK)) {
            status.publishStatus();
        } else {
            logger.info("Unknown Mqtt Message Type.");
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }

    public String getId() {
        return id;
    }
}
