package org.anar.termitefactory.repository;

import org.anar.termitefactory.entity.EdgeMachine;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EdgeMachineRepository extends MongoRepository<EdgeMachine, String> {
    EdgeMachine findByNumber(String number);
}
