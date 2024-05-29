package org.anar.termitefactory.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.anar.termitefactory.service.EdgeMachineService;
import org.anar.termitefactory.service.JobService;
import org.anar.termitefactory.service.MessageDistributionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class StatusController {
    private static final Logger logger = LoggerFactory.getLogger(AlgorithmController.class);
    @Autowired
    ObjectMapper mapper;
    @Autowired
    EdgeMachineService edgeMachineService;
    @Autowired
    MessageDistributionService messageDistributionService;
    @Autowired
    JobService jobService;

    // 查询单一机器状态
    @RequestMapping(
            value = "/status/machine/v1",
            method = RequestMethod.GET)
    public String getMachineStatus(@RequestParam("number") String number) throws Exception {
        ObjectNode reply = mapper.createObjectNode();
        reply.put("success", true);
        JsonNode machines = mapper.valueToTree(edgeMachineService.getMachine(number));
        reply.set("machines", machines);
        return mapper.writeValueAsString(reply);
    }

    // 查询所有任务状态
    @RequestMapping(
            value = "/status/machine",
            method = RequestMethod.GET)
    public String getMachineStatus() throws Exception {
        ObjectNode reply = mapper.createObjectNode();
        reply.put("success", true);
        JsonNode machines = mapper.valueToTree(edgeMachineService.getEdgeMachineList());
        reply.put("number", machines.size());
        reply.set("machines", machines);
        return mapper.writeValueAsString(reply);
    }

    // 清空机器任务
    @RequestMapping(
            value = "/status/machine/clear",
            method = RequestMethod.GET)
    public String clearFinishedOperation() throws Exception {
        ObjectNode reply = mapper.createObjectNode();
        reply.put("success", true);
        messageDistributionService.clearFinishedOperation();
        return mapper.writeValueAsString(reply);
    }

    // 机器概况
    @RequestMapping(
            value = "/status/machine/overall",
            method = RequestMethod.GET)
    public String getMachineOverallStatus() throws Exception {
        ObjectNode reply = mapper.createObjectNode();
        reply.put("success", true);
        reply.set("statuses", edgeMachineService.getOverallStatus());
        return mapper.writeValueAsString(reply);
    }

    // 查询单一任务状态
    @RequestMapping(
            value = "/status/job/v1",
            method = RequestMethod.GET)
    public String getJobStatus(@RequestParam("index") long index) throws Exception {
        ObjectNode reply = mapper.createObjectNode();
        reply.put("success", true);
        JsonNode job = mapper.valueToTree(jobService.getJob(index));
        reply.set("jobs", job);
        return mapper.writeValueAsString(reply);
    }


    // 查询所有任务状态
    @RequestMapping(
            value = "/status/job",
            method = RequestMethod.GET)
    public String getJobStatus(@RequestParam("status") String status, @RequestParam("reverse") boolean reverse) throws Exception {
        ObjectNode reply = mapper.createObjectNode();
        reply.put("success", true);
        JsonNode jobs = null;
        if (reverse) {
            jobs = mapper.valueToTree(jobService.getJobListNot(status));
        } else {
            jobs = mapper.valueToTree(jobService.getJobList(status));
        }
        reply.put("number", jobs.size());
        reply.set("statuses", jobService.getJobStatuses());
        reply.set("jobs", jobs);
        return mapper.writeValueAsString(reply);
    }
}
