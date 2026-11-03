package com.fitness.activityservice.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fitness.activityservice.model.ActivityType;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivityRequest {
    private String userId;
    private ActivityType type;
    private Integer duration;
    private Integer caloriesBurned;
    private LocalDateTime startTime;
    private Map<String, Object> additionalMatrics;
    
    public void setAdditionalMetrics(Map<String, Object> metrics) {
        this.additionalMatrics = metrics;
    }

    public Map<String, Object> getAdditionalMetrics() {
        return this.additionalMatrics;
    }
}
