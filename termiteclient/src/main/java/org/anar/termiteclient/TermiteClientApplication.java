package org.anar.termiteclient;

import org.anar.termiteclient.object.Status;
import org.anar.termiteclient.service.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@SpringBootApplication
@EnableScheduling
public class TermiteClientApplication {

    public static void main(String[] args) throws IOException, MqttException {
        SpringApplication application = new SpringApplication(TermiteClientApplication.class);
        final ApplicationContext context = application.run(args);
        MqttClient client = context.getBean(MqttClient.class);
        Status status = context.getBean(Status.class);
        client.connect(args[0]);
        status.restoreStatus();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                client.disConnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }));
    }
}
