package org.anar.termitefactory;

import org.anar.termitefactory.service.moquette.MoquetteServer;
import org.anar.termitefactory.controller.AlgorithmController;
import org.anar.termitefactory.service.moquette.MqttCloudClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@SpringBootApplication
@EnableMongoRepositories
@EnableScheduling
public class TermiteFactoryApplication {
    public static void main(String[] args) throws IOException, MqttException {
        SpringApplication application = new SpringApplication(TermiteFactoryApplication.class);
        final ApplicationContext context = application.run(args);
        MoquetteServer server = context.getBean(MoquetteServer.class);
        server.startServer();
        MqttCloudClient client = context.getBean(MqttCloudClient.class);
        client.connect();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.stop();
                try {
                    client.disConnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
//                logger.info("Moquette Server stopped");
            }
        });
    }

}
