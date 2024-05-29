package org.anar.termitefactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MQTTClientTests {
    @Test
    void contextLoads() {
        String topic = "news";
        String content = "Visit www.hascode.com! :D";
        int qos = 2;
        String broker = "tcp://0.0.0.0:5702";
        String clientId = "paho-java-client";

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            System.out.println(connOpts.getMqttVersion());
//            connOpts.setCleanSession(true);
            System.out.println("paho-client connecting to broker: " + broker);
            sampleClient.connect(connOpts);
            System.out.println("paho-client connected to broker");
            System.out.println("paho-client publishing message: " + content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);

            sampleClient.setCallback(new MqttCallback() {
                public void connectionLost(Throwable cause) {}

                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("Message: " + message.toString());
                }

                public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            sampleClient.publish(topic, message);
            System.out.println("paho-client message published");

            sampleClient.subscribe("machine:ChangSha:F1:1001");

//            sampleClient.disconnect();
//            System.out.println("paho-client disconnected");
        } catch (MqttException me) {
            me.printStackTrace();
        }
    }
}