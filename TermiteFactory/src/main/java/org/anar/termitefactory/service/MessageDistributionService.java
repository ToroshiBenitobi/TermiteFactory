package org.anar.termitefactory.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.anar.termitefactory.config.MqttType;
import org.anar.termitefactory.entity.EdgeMachine;
import org.anar.termitefactory.entity.Job;
import org.anar.termitefactory.entity.Procedure;
import org.anar.termitefactory.entity.schedule.Machine;
import org.anar.termitefactory.repository.JobRepository;
import org.anar.termitefactory.repository.MachineRepository;
import org.anar.termitefactory.service.moquette.MqttCloudClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
public class MessageDistributionService {
    private static final Logger logger = LoggerFactory.getLogger(MessageDistributionService.class);
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MqttCloudClient client;
    @Autowired
    EdgeMachineService edgeMachineService;
    @Autowired
    MachineRepository machineRepository;
    @Autowired
    JobRepository jobRepository;


    public void sendAllocation(JsonNode machines, JsonNode jobList) throws JsonProcessingException, MqttException {
        long timeStamp = Calendar.getInstance().getTimeInMillis();
        for (JsonNode machine : machines) {
            if (machine.get("operations").size() == 0) continue;

            String number = machine.get("name").asText();
            Machine edge = machineRepository.findByNumber(number);

            ObjectNode root = objectMapper.createObjectNode();
            root.put("type", MqttType.ALLOCATION);
            root.put("timeStamp", timeStamp);
            root.set("operations", machine.get("operations"));
            root.set("jobs", jobList);
            // choose the topic and the content.
            String topic = "machine/" + number;
            String content = objectMapper.writeValueAsString(root);

            client.publish(topic, content);
        }
        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", MqttType.COMMENCE);
        root.put("timeStamp", timeStamp);
        client.publish("machine", objectMapper.writeValueAsString(root));
        logger.info("Start to process operations.");
    }

    public void sendAllocation(String machineNumber, long index, int number) throws JsonProcessingException, MqttException {
        long timeStamp = Calendar.getInstance().getTimeInMillis();

        Machine edge = machineRepository.findByNumber(machineNumber);

        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", MqttType.ALLOCATION);
        root.put("timeStamp", timeStamp);

        Job job = jobRepository.findByIndex(index);
        Procedure procedure = job.getProcedures().get(number);
        procedure.setMachine(machineNumber);

        ArrayNode operations = objectMapper.createArrayNode();
        operations.add(objectMapper.valueToTree(procedure));

        root.set("operations", operations);
        List<Job> jobList = new ArrayList<>();
        jobList.add(jobRepository.findByIndex(index));
        for (Long preJob : jobList.get(0).getPreJobs()) {
            jobList.add(jobRepository.findByIndex(index));
        }
        root.set("jobs", objectMapper.valueToTree(jobList));
        String topic = "machine/" + machineNumber;
        String content = objectMapper.writeValueAsString(root);
        client.publish(topic, content);

        // Start to process operations.
        ObjectNode commence = objectMapper.createObjectNode();
        commence.put("type", MqttType.COMMENCE);
        commence.put("timeStamp", timeStamp);
        client.publish(topic, objectMapper.writeValueAsString(commence));
    }

    public void clearFinishedOperation() throws JsonProcessingException, MqttException {
        ObjectNode root = objectMapper.createObjectNode();
        long timeStamp = Calendar.getInstance().getTimeInMillis();
        root.put("type", MqttType.CLEAR_FINISHED_OPERATION);
        root.put("timeStamp", timeStamp);
        String topic = "machine";
        String content = objectMapper.writeValueAsString(root);

        client.publish(topic, content);
    }

}
