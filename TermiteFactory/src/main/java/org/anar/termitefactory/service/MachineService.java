package org.anar.termitefactory.service;

import org.anar.termitefactory.entity.schedule.Machine;
import org.anar.termitefactory.repository.MachineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MachineService {
    @Autowired
    MachineRepository machineRepository;

    public List<Machine> getMatchedMachine(String[] tags) {
        List<Machine> machines = null;

        switch (tags.length) {
            case 1:
                machines = machineRepository.findAllByTag(tags[0]);
                break;
            case 2:
                machines = machineRepository.findAllByTag(tags[0], tags[1]);
                break;
            case 3:
                machines = machineRepository.findAllByTag(tags[0], tags[1], tags[2]);
                break;
            case 4:
                machines = machineRepository.findAllByTag(tags[0], tags[1], tags[2], tags[3]);
                break;
            case 5:
                machines = machineRepository.findAllByTag(tags[0], tags[1], tags[2], tags[3], tags[4]);
                break;
        }
        return machines;
    }
}
