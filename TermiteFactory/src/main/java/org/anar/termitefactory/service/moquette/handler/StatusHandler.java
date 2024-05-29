package org.anar.termitefactory.service.moquette.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.netty.buffer.ByteBufUtil;
import org.anar.termitefactory.entity.EdgeMachine;
import org.anar.termitefactory.entity.Job;
import org.anar.termitefactory.entity.Procedure;
import org.anar.termitefactory.repository.EdgeMachineRepository;
import org.anar.termitefactory.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Component
public class StatusHandler extends AbstractInterceptHandler {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    JobRepository jobRepository;
    @Autowired
    EdgeMachineRepository edgeMachineRepository;

    @Override
    public String getID() {
        return SafetyInterceptHandler.class.getName();
    }

    @Override
    public void onPublish(InterceptPublishMessage message) {
        if (message.getTopicName().equals("status")) {
            try {
                JsonNode node = objectMapper.readTree(new String(ByteBufUtil.getBytes(message.getPayload()), Charset.forName("UTF-8")));
                String number = node.get("number").asText();

                // update the machine status.
                EdgeMachine edgeMachine = edgeMachineRepository.findByNumber(number);
                if (edgeMachine == null) {
                    edgeMachine = new EdgeMachine();
                    edgeMachine.setNumber(number);
                }

                edgeMachine.setStatus(node.get("status").asText());
                edgeMachine.setConsumption(node.get("consumption").asDouble());
                edgeMachine.setHourlyConsumption(node.get("hourlyConsumption").asDouble());
                edgeMachine.setRunningTime(node.get("runningTime").asLong());
                edgeMachine.setIdlingTime(node.get("idlingTime").asLong());
                edgeMachine.setTimeStamp(node.get("timeStamp").asLong());
                List<Procedure> machineProcedures = new ArrayList<>();
                for (JsonNode operation : node.get("operations")) {
                    Procedure procedure = objectMapper.treeToValue(operation, Procedure.class);
                    machineProcedures.add(procedure);
                }
                edgeMachine.setProcedureList(machineProcedures);
                edgeMachineRepository.save(edgeMachine);

                // update operations.
                JsonNode operations = node.get("operations");
                for (JsonNode operation : operations) {
                    Procedure procedure = objectMapper.treeToValue(operation, Procedure.class);
                    procedure.setMachine(edgeMachine.getNumber());
                    Job job = jobRepository.findByIndex(procedure.getJob());
                    if (null == job) continue;
                    job.getProcedures().set(procedure.getNumber(), procedure);

                    boolean isFinished = true;
                    String jobStatus = "";
                    if (job.getProcedures().get(job.getProcedures().size() - 1).getStatus().equals("finished")) {
                        job.setStatus("finished");
                    } else if (job.getProcedures().get(0).getStatus().equals("prepared")
                            || job.getProcedures().get(0).getStatus().equals("scheduled")) {
                        job.setStatus("scheduled");
                    } else {
                        job.setStatus("running");
                    }
                    jobRepository.save(job);
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
}
