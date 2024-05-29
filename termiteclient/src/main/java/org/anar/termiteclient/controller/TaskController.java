package org.anar.termiteclient.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.anar.termiteclient.object.Status;
import org.anar.termiteclient.service.MqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskController {
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MqttClient mqttClient;
    @Autowired
    Status status;

    @RequestMapping(
            value = "/test/message",
            method = RequestMethod.POST)
    public String message(@RequestBody String payload) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(payload);
        String message = mapper.writeValueAsString(node.get("payload"));
        String topic = node.get("topic").asText();

        mqttClient.publish(topic,message);

        ObjectNode reply = mapper.createObjectNode();
        reply.put("success", true);
        return mapper.writeValueAsString(reply);
    }
}
