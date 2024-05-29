package org.anar.termitefactory.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.anar.termitefactory.config.MqttType;
import org.anar.termitefactory.entity.EdgeMachine;
import org.anar.termitefactory.entity.Job;
import org.anar.termitefactory.entity.schedule.Procedure;
import org.anar.termitefactory.entity.schedule.Blueprint;
import org.anar.termitefactory.service.BlueprintService;
import org.anar.termitefactory.service.EdgeMachineService;
import org.anar.termitefactory.service.JobService;
import org.anar.termitefactory.service.MessageDistributionService;
import org.anar.termitefactory.service.moquette.MqttCloudClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RedistributionController {
    private static final Logger logger = LoggerFactory.getLogger(RedistributionController.class);
    @Autowired
    ObjectMapper mapper;
    @Autowired
    JobService jobService;
    @Autowired
    BlueprintService blueprintService;
    @Autowired
    EdgeMachineService edgeMachineService;
    @Autowired
    MqttCloudClient mqttCloudClient;
    @Autowired
    MessageDistributionService messageDistributionService;


    @RequestMapping(
            value = "/redistribute/alternative_machine",
            method = RequestMethod.GET)
    public String getAlternativeMachine(@RequestParam("index") long index, @RequestParam("number") int number) throws Exception {

        String code = jobService.getJob(index).getJobCode();
        Procedure procedure = blueprintService.getProcedure(code, number);
        List<EdgeMachine> edgeMachineList = edgeMachineService.getConnectedMatchedMachineList(procedure.getTag());

        ObjectNode reply = mapper.createObjectNode();
        reply.put("success", true);
        reply.set("machines", mapper.valueToTree(edgeMachineList));

        return mapper.writeValueAsString(reply);
    }

    @RequestMapping(
            value = "/redistribute/v1",
            method = RequestMethod.GET)
    public String redistributeJob(@RequestParam("index") long index, @RequestParam("number") int number,
                                  @RequestParam("machine") String machineNumber, @RequestParam("slot") int slot) throws JsonProcessingException, MqttException {
        ObjectNode node1 = mapper.createObjectNode();
        node1.put("index", index);
        node1.put("number", number);
        node1.put("type", MqttType.DELETE);

        mqttCloudClient.publish("machine/" + jobService.getJob(index).getProcedures().get(number).getMachine(), mapper.writeValueAsString(node1));
        messageDistributionService.sendAllocation(machineNumber, index, number);

        ObjectNode reply = mapper.createObjectNode();
        reply.put("success", true);
        return mapper.writeValueAsString(reply);
    }
}
