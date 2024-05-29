package org.anar.termitefactory.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.anar.termitefactory.entity.EdgeMachine;
import org.anar.termitefactory.entity.Procedure;
import org.anar.termitefactory.entity.schedule.Machine;
import org.anar.termitefactory.repository.EdgeMachineRepository;
import org.anar.termitefactory.repository.MachineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
public class EdgeMachineService {
    @Autowired
    ObjectMapper mapper;
    @Autowired
    EdgeMachineRepository edgeMachineRepository;
    @Autowired
    MachineService machineService;


    public List<EdgeMachine> getEdgeMachineList() {
        return edgeMachineRepository.findAll();
    }

    public EdgeMachine getMachine(String number) {
        return edgeMachineRepository.findByNumber(number);
    }

    public JsonNode getOverallStatus() {
        ObjectNode root = mapper.createObjectNode();
        List<EdgeMachine> edgeMachines = edgeMachineRepository.findAll();
        double sumOfConsumptions = 0;
        double sumOfHourlyConsumptions = 0;
        long timeStamp = Calendar.getInstance().getTimeInMillis();
        long sumOfTransportationTimes = 0;
        int sumOfRunningMachines = 0;
        int sumOfIdlingMachines = 0;
        int sumOfDisconnectedMachines = 0;
        for (EdgeMachine edgeMachine : edgeMachines) {
            sumOfConsumptions += edgeMachine.getConsumption();
            sumOfHourlyConsumptions += edgeMachine.getHourlyConsumption();
            String status = edgeMachine.getStatus();
            if (status.equals("running")) {
                ++sumOfRunningMachines;
            } else if (status.equals("idling")) {
                ++sumOfIdlingMachines;
            } else if (status.equals("disconnected")) {
                ++sumOfDisconnectedMachines;
            }
            for (Procedure procedure : edgeMachine.getProcedureList()) {
                if (procedure.getStatus().equals("finished")) {
                    sumOfTransportationTimes +=
                            Duration.between(procedure.getProcessCompletedTime(), procedure.getTransportCompletedTime()).getSeconds();
                }
            }
        }
        root.put("timeStamp", timeStamp);
        root.put("sumOfConsumptions", sumOfConsumptions);
        root.put("sumOfHourlyConsumptions", sumOfHourlyConsumptions);
        root.put("sumOfTransportationTimes", sumOfTransportationTimes);
        root.put("number", sumOfRunningMachines + sumOfIdlingMachines + sumOfDisconnectedMachines);
        ObjectNode machineStatuses = root.putObject("machineStatuses");
        machineStatuses.put("running", sumOfRunningMachines);
        machineStatuses.put("idling", sumOfIdlingMachines);
        machineStatuses.put("disconnected", sumOfDisconnectedMachines);
        return root;
    }

    public List<EdgeMachine> getConnectedMatchedMachineList(String[] args) {
        List<Machine> machineList = machineService.getMatchedMachine(args);
        List<EdgeMachine> edgeMachineList = new ArrayList<>();
        for (Machine machine : machineList) {
            String number = machine.getNumber();
            EdgeMachine edgeMachine = edgeMachineRepository.findByNumber(number);
            if (!edgeMachine.getStatus().equals("disconnected")) {
                edgeMachineList.add(edgeMachine);
            }
        }
        return edgeMachineList;
    }
}
