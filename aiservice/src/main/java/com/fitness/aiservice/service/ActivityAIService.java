package com.fitness.aiservice.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Data
public class ActivityAIService {
    
    private final GeminiService geminiService;

    public Recommendation generateRecommendation(Activity activity){
        String prompt = createPromptForActivity(activity);
        String aiResponse = geminiService.getRecommendation(prompt);
        log.info("Response from AI: {}",aiResponse);
        return processAIresponse(activity,aiResponse);
    }

    private Recommendation processAIresponse(Activity activity, String aiResponse) {
        try{
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(aiResponse);
            JsonNode textNode = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text");
            
            String rawText = textNode.asText().trim();
            int start = rawText.indexOf('{');
            int end = rawText.lastIndexOf('}');
            if (start != -1 && end > start) {
                rawText = rawText.substring(start, end + 1);
            }
            
            JsonNode analysisJson = mapper.readTree(rawText);
            JsonNode analysisNode = analysisJson.path("analysis");
            StringBuilder fullAnalysis = new StringBuilder();
            addAnalysisSection(fullAnalysis, analysisNode, "overall", "Overall: ");
            addAnalysisSection(fullAnalysis, analysisNode, "pace", "Pace: ");
            addAnalysisSection(fullAnalysis, analysisNode, "heartRate", "Heart Rate: ");
            addAnalysisSection(fullAnalysis, analysisNode, "caloriesBurned", "Calories Burned: ");
            
            JsonNode suggestionsNode = analysisJson.path("suggestions");
            if (suggestionsNode.isMissingNode()) {
                suggestionsNode = analysisJson.path("suggestion");
            }
            if (suggestionsNode.isMissingNode()) {
                suggestionsNode = analysisJson.path("actionableSuggestions");
            }
            List<String> suggestions = extractSuggestion(suggestionsNode);
            List<String> improvements = extractImprovements(analysisJson.path("improvements"));
            List<String> safety = extractSafety(analysisJson.path("safety"));
            
            return Recommendation.builder()
                .activityId(activity.getId())
                .userId(activity.getUserId())
                .type(activity.getType() != null ? activity.getType().toString() : "UNKNOWN")
                .recommendation(fullAnalysis.toString().trim())
                .improvements(improvements)
                .suggestions(suggestions)
                .safety(safety)
                .createdAt(LocalDateTime.now())
                .build();

        } catch(Exception e){
            log.error("Error parsing AI response: {}", e.getMessage(), e);
            return createDefaultRecommendation(activity);
        }
    }

    private Recommendation createDefaultRecommendation(Activity activity) {
        return Recommendation.builder()
            .activityId(activity.getId())
            .userId(activity.getUserId())
            .type(activity.getType() != null ? activity.getType().toString() : "UNKNOWN")
            .recommendation("Unable to generate detailed analysis")
            .improvements(Collections.singletonList("Unable to generate improvements"))
            .suggestions(Collections.singletonList("Unable to generate suggestions"))
            .safety(Collections.singletonList("Unable to generate safety"))
            .createdAt(LocalDateTime.now())
            .build();
    }

    private List<String> extractSuggestion(JsonNode suggestionNode) {
        List<String> suggestions = new ArrayList<>();
        if (suggestionNode != null && suggestionNode.isArray()) {
            suggestionNode.forEach(item -> {
                if (item.isObject()) {
                    String workout = item.path("workout").asText("");
                    String description = item.path("description").asText("");
                    if (description.isEmpty()) {
                        description = item.path("recommendation").asText("");
                    }
                    suggestions.add((workout.isEmpty() ? "" : workout + ": ") + description);
                } else {
                    suggestions.add(item.asText());
                }
            });
        }
        return suggestions.isEmpty() ? Collections.singletonList("No specific suggestions provided") : suggestions;
    }

    private List<String> extractImprovements(JsonNode improvementNode) {
        List<String> improvements = new ArrayList<>();
        if (improvementNode != null && improvementNode.isArray()) {
            improvementNode.forEach(item -> {
                if (item.isObject()) {
                    String area = item.path("area").asText();
                    String recommendation = item.path("recommendation").asText();
                    improvements.add((area.isEmpty() ? "" : area + ": ") + recommendation);
                } else {
                    improvements.add(item.asText());
                }
            });
        }
        return improvements.isEmpty() ? Collections.singletonList("No specific improvements provided") : improvements;
    }

    private List<String> extractSafety(JsonNode safetyNode) {
        List<String> safety = new ArrayList<>();
        if (safetyNode != null && safetyNode.isArray()) {
            safetyNode.forEach(item -> safety.add(item.asText()));
        }
        return safety.isEmpty() ? Collections.singletonList("No specific safety guidelines provided") : safety;
    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
        if (!analysisNode.path(key).isMissingNode() && !analysisNode.path(key).asText().isEmpty()) {
            fullAnalysis.append(prefix).append(analysisNode.path(key).asText()).append("\n\n");
        }
    }

    private String createPromptForActivity(Activity activity){
        return String.format("""
            Analyze the fitness activity and provide a detailed recommendation in the following exact JSON format:
            {
              "analysis": {
                "overall": "Overall performance and execution analysis",
                "pace": "Pacing, cadence, or speed analysis",
                "heartRate": "Cardiovascular intensity and estimated exertion analysis",
                "caloriesBurned": "Calorie burn efficiency and metabolic output analysis"
              },
              "improvements": [
                {
                  "area": "Area name (e.g. Endurance, Form, Pacing, Recovery)",
                  "recommendation": "Specific advice on how to improve this area"
                }
              ],
              "suggestions": [
                {
                  "workout": "Suggested Next Workout Title",
                  "description": "Specific actionable training plan, target duration, or intervals"
                },
                {
                  "workout": "Active Recovery or Complementary Training",
                  "description": "Recommended cross-training, mobility, or stretching session"
                }
              ],
              "safety": [
                "Specific safety, hydration, or injury prevention guideline 1",
                "Recovery or warm-up/cool-down guideline 2"
              ]
            }

            Analyze this activity:
            Activity Type: %s
            Duration: %d minutes
            Calories Burned: %d
            Additional Metrics: %s

            Provide high quality, actionable fitness coaching insights focusing on performance, improvements, concrete next workout suggestions, and safety guidelines.
            Ensure the response follows the exact JSON format shown above with valid JSON only.
            """, 
            activity.getType(),
            activity.getDuration(),
            activity.getCaloriesBurned(),
            activity.getAdditionalMatrics());
    }

}
