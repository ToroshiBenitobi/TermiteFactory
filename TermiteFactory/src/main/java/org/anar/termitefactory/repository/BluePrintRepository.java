package org.anar.termitefactory.repository;

import org.anar.termitefactory.entity.schedule.Blueprint;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BluePrintRepository extends MongoRepository<Blueprint, String> {
    Blueprint findByCode(String code);
    List<Blueprint> findAllByName(String name);
}
