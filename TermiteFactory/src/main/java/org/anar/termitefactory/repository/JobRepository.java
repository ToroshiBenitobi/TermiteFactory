package org.anar.termitefactory.repository;

import org.anar.termitefactory.entity.Job;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface JobRepository extends MongoRepository<Job, String> {
    @Query(value = "{}", count = true)
    public long count();
    Job findByIndex(long index);
    List<Job> findAllByStatus(String status);
    List<Job> findAllByStatusNot(String status);
}
