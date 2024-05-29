package org.anar.termitefactory.service.moquette.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.netty.buffer.ByteBufUtil;
import org.anar.termitefactory.config.MqttType;
import org.anar.termitefactory.controller.AlgorithmController;
import org.anar.termitefactory.entity.EdgeMachine;
import org.anar.termitefactory.service.EdgeMachineService;
import org.anar.termitefactory.service.moquette.MqttCloudClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

@Component
public class SafetyInterceptHandler extends AbstractInterceptHandler {
    private static final Logger logger = LoggerFactory.getLogger(SafetyInterceptHandler.class);
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    EdgeMachineService edgeMachineService;
    @Autowired
    MqttCloudClient mqttCloudClient;

    @Override
    public String getID() {
        return SafetyInterceptHandler.class.getName();
    }

    @Override
    public void onPublish(InterceptPublishMessage message) {
        logger.info("Received: topic: " + message.getTopicName()
                + ", content: " + new String(ByteBufUtil.getBytes(message.getPayload()), Charset.forName("UTF-8")));
        try {
            JsonNode node = objectMapper.readTree(new String(ByteBufUtil.getBytes(message.getPayload()), Charset.forName("UTF-8")));
            // restore request.
            if (node.get("type").asText().equals(MqttType.RESTORE_QUEST)) {
                String number = node.get("number").asText();
                EdgeMachine edgeMachine = edgeMachineService.getMachine(number);
                ObjectNode root = objectMapper.createObjectNode();
                root.put("type", MqttType.RESTORE);
                JsonNode status = objectMapper.valueToTree(edgeMachine);
                root.set("status", status);
                mqttCloudClient.publish("machine/"+number,objectMapper.writeValueAsString(root));
            }

        } catch (JsonProcessingException | MqttException e) {
            e.printStackTrace();
        }
    }
}
