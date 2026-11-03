package com.fitness.activityservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitness.activityservice.model.Recommendation;
import com.fitness.activityservice.service.RecommendationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    
    @GetMapping
    public ResponseEntity<List<Recommendation>> getAllRecommendations() {
        return ResponseEntity.ok(recommendationService.getAllRecommendations());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Recommendation>> getUserRecommendation(@PathVariable String userId) {
        return ResponseEntity.ok(recommendationService.getUserRecommendation(userId));
    }

    @GetMapping("/activity/{activityId}")
    public ResponseEntity<Recommendation> getActivityRecommendation(@PathVariable String activityId) {
        return ResponseEntity.ok(recommendationService.getRecommendationForActivity(activityId));
    }
}
