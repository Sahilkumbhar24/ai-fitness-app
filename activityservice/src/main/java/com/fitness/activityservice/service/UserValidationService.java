package com.fitness.activityservice.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserValidationService {

    public boolean validateUser(String userId){
        if (userId == null || userId.trim().isEmpty()) {
            log.warn("User validation skipped: userId is null or empty");
            return false;
        }
        return true;
    }

}
