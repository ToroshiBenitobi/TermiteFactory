package org.anar.termitefactory.service.moquette;

import io.moquette.broker.Server;
import io.moquette.broker.config.ClasspathResourceLoader;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.IResourceLoader;
import io.moquette.broker.config.ResourceLoaderConfig;
import io.moquette.interception.InterceptHandler;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class MqttCloudClient {
    @Autowired
    MoquetteServer moquetteServer;

    MqttClient client;

    private Server mqttServer;

    public void connect() throws MqttException {
        String url = "tcp://0.0.0.0:" + Integer.toString(moquetteServer.getMqttServer().getPort());
        String id = "cloud";
        client = new MqttClient(url, id);
        client.connect();
        client.setCallback(new MqttCloudClientCallBack());
    }

    public void publish(String topic, String payload, int qos) throws MqttException {
        MqttMessage message = new MqttMessage();
        message.setPayload(payload.getBytes());
        message.setQos(qos);
        client.publish(topic, message);
    }

    public void publish(String topic, String payload) throws MqttException {
        if (!client.isConnected()) {
            connect();
        }
        MqttMessage message = new MqttMessage();
        message.setPayload(payload.getBytes());
        message.setQos(2);
        client.publish(topic, message);
    }

    public void disConnect() throws MqttException {
        client.disconnect();
    }
}
