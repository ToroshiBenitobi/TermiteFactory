package org.anar.termiteclient.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.anar.termiteclient.object.Status;
import org.anar.termiteclient.service.MqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class StatusController {
    private static final Logger logger = LoggerFactory.getLogger(StatusController.class);
    @Autowired
    ObjectMapper mapper;
    @Autowired
    Status status;

    @RequestMapping(
            value = "/status/v1",
            method = RequestMethod.GET)
    public String getStatus() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode reply = mapper.createObjectNode();
        reply.put("success", true);
        reply.set("status", status.getStatusNode());
        return mapper.writeValueAsString(reply);
    }

    @RequestMapping(
            value = "/status/set",
            method = RequestMethod.GET)
    public String setStatus(@RequestParam("status") String statusString,
                            @RequestParam("consumption") double consumption,
                            @RequestParam("hourlyConsumption") double hourlyConsumption,
                            @RequestParam("runningTime") long runningTime,
                            @RequestParam("idlingTime") long idlingTime) throws Exception {
        status.setStatus(statusString);
        status.setConsumption(consumption);
        status.setHourlyConsumption(hourlyConsumption);
        status.setRunningTime(runningTime);
        status.setIdlingTime(idlingTime);

        ObjectNode reply = mapper.createObjectNode();
        reply.put("success", true);
        return mapper.writeValueAsString(reply);
    }
}

