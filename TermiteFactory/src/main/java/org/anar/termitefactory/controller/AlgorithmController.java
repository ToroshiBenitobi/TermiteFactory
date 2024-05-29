package org.anar.termitefactory.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.anar.scheduling.HMOGWORunner;
import org.anar.termitefactory.service.GenerateProblemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AlgorithmController {
    private static final Logger logger = LoggerFactory.getLogger(AlgorithmController.class);
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    GenerateProblemService generateProblemService;

    // 算法分配
    @RequestMapping(
            value = "/algorithm/project",
            method = RequestMethod.POST)
    public String process(@RequestBody String payload) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(payload);
        ObjectNode description = node.get("description").deepCopy();
        description.put("transportationRate", 0.5);
        String problem = generateProblemService.generateProblem(description);
        logger.info("Start to run algorithm.");
        String results = HMOGWORunner.run(problem);
        return results;
    }
}
