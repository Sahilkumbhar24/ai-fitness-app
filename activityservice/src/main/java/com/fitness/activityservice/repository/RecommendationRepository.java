package com.fitness.activityservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.fitness.activityservice.model.Recommendation;

@Repository
public interface RecommendationRepository extends MongoRepository<Recommendation, String> {
    List<Recommendation> findByUserId(String userId);
    Optional<Recommendation> findByActivityId(String activityId);
}

