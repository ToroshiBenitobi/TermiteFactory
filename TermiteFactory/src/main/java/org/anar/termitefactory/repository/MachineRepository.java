package org.anar.termitefactory.repository;

import org.anar.termitefactory.entity.schedule.Machine;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface MachineRepository extends MongoRepository<Machine, String> {
    @Query("{ 'tag': { $all: [?0] } } ")
    List<Machine> findAllByTag(String tag0);
    @Query("{ 'tag': { $all: [?0, ?1] } } ")
    List<Machine> findAllByTag(String tag0, String tag1);
    @Query("{ 'tag': { $all: [?0, ?1, ?2] } } ")
    List<Machine> findAllByTag(String tag0, String tag1, String tag2);
    @Query("{ 'tag': { $all: [?0, ?1, ?2, ?3] } } ")
    List<Machine> findAllByTag(String tag0, String tag1, String tag2, String tag3);
    @Query("{ 'tag': { $all: [?0, ?1, ?2, ?3, ?4] } } ")
    List<Machine> findAllByTag(String tag0, String tag1, String tag2, String tag3, String tag4);
    @Query("{ 'number': ?0 } ")
    Machine findByNumber(String number);
}
