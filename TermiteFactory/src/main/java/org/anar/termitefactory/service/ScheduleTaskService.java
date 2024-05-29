package org.anar.termitefactory.service;

import org.anar.termitefactory.config.MqttType;
import org.anar.termitefactory.entity.EdgeMachine;
import org.anar.termitefactory.repository.EdgeMachineRepository;
import org.anar.termitefactory.service.moquette.MqttCloudClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;

@Service
public class ScheduleTaskService {
    @Autowired
    EdgeMachineRepository edgeMachineRepository;
    @Autowired
    MqttCloudClient client;

    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    public void checkMachineAlive() throws MqttException {
        List<EdgeMachine> machineList = edgeMachineRepository.findAll();
        Long currentTime = Calendar.getInstance().getTimeInMillis();
        for (EdgeMachine machine : machineList) {
            if (currentTime - machine.getTimeStamp() > 120000) {
                String content = "{\"type\":\"" + MqttType.CHECK + "\"}";
                client.publish("machine/" + machine.getNumber(), content);
                machine.setStatus("disconnected");
                edgeMachineRepository.save(machine);
            }
        }
    }
}
