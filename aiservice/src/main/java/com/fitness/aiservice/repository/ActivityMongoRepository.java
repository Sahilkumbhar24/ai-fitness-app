package com.fitness.aiservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.fitness.aiservice.model.Activity;

@Repository
public interface ActivityMongoRepository extends MongoRepository<Activity, String> {
}
