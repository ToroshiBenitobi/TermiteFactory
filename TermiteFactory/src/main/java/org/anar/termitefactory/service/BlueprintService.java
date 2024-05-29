package org.anar.termitefactory.service;

import org.anar.termitefactory.entity.schedule.Blueprint;
import org.anar.termitefactory.entity.schedule.Procedure;
import org.anar.termitefactory.repository.BluePrintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BlueprintService {
    @Autowired
    BluePrintRepository bluePrintRepository;

    public Blueprint getBlueprint(String code) {
        return bluePrintRepository.findByCode(code);
    }

    public Procedure getProcedure(String code, int number) {
        Blueprint blueprint = bluePrintRepository.findByCode(code);
        return blueprint.getProcedure().get(number);
    }

}
