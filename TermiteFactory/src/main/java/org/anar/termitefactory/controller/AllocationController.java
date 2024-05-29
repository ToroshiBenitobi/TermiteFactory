package org.anar.termitefactory.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.anar.scheduling.HMOGWORunner;
import org.anar.termitefactory.service.GenerateProblemService;
import org.anar.termitefactory.service.JobService;
import org.anar.termitefactory.service.MessageDistributionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AllocationController {
    private static final Logger logger = LoggerFactory.getLogger(AlgorithmController.class);

    @Autowired
    MessageDistributionService messageDistributionService;
    @Autowired
    JobService jobService;

    // 分配任务
    @RequestMapping(
            value = "/allocation/distribute/v1",
            method = RequestMethod.POST)

    public String distributeAllocation(@RequestBody String payload) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(payload);

        node = jobService.solidifyAllocation(node);

        JsonNode machines = node.get("allocation").get("machines");
        JsonNode jobList = node.get("jobList");
        messageDistributionService.sendAllocation(machines, jobList);

        ObjectNode reply = mapper.createObjectNode();
        reply.put("success", true);
        return mapper.writeValueAsString(reply);
    }
}
