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
        try {
            String apiKey = geminiService.getGeminiApiKey();
            if (apiKey == null || apiKey.trim().isEmpty() || "YOUR_GEMINI_API_KEY".equalsIgnoreCase(apiKey.trim())) {
                log.warn("Gemini API key is not configured. Generating intelligent baseline workout recommendation.");
                return createDefaultRecommendation(activity);
            }
            String prompt = createPromptForActivity(activity);
            String aiResponse = geminiService.getRecommendation(prompt);
            log.info("Response from AI: {}", aiResponse);
            return processAIresponse(activity, aiResponse);
        } catch (Exception e) {
            log.warn("Error calling Gemini API ({}), falling back to intelligent baseline workout recommendation.", e.getMessage());
            return createDefaultRecommendation(activity);
        }
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
        String sport = activity.getType() != null ? activity.getType().toString().toUpperCase() : "WORKOUT";
        int duration = activity.getDuration() != null && activity.getDuration() > 0 ? activity.getDuration() : 30;
        int calories = activity.getCaloriesBurned() != null && activity.getCaloriesBurned() >= 0 ? activity.getCaloriesBurned() : 200;

        double burnRate = (double) calories / duration;
        String formattedBurnRate = String.format("%.1f", burnRate);

        // Dynamic intensity & metabolic profiling
        String intensityCategory;
        String exertionDescription;
        String heartRateZone;
        String pacingInsight;

        if (burnRate < 2.0 && duration >= 30) {
            intensityCategory = "Low Intensity / Metabolic Tracking Discrepancy";
            exertionDescription = String.format("A recorded burn rate of %s kcal/min for %d minutes indicates very light exertion or possible under-reporting by fitness sensors.", formattedBurnRate, duration);
            heartRateZone = "Zone 1 (Active Recovery / Resting, <50% Max HR)";
            pacingInsight = "Steady low-stress cadence. If this was an active workout, ensure heart rate monitor or GPS tracker is calibrated.";
        } else if (burnRate < 5.0) {
            intensityCategory = "Light Aerobic / Fat Oxidation (Zone 1-2)";
            exertionDescription = String.format("Burn rate of %s kcal/min indicates sustained aerobic energy with high reliance on lipid metabolism.", formattedBurnRate);
            heartRateZone = "Zone 2 (Aerobic Base Building, 60-70% Max HR)";
            pacingInsight = "Conversational pace maintained comfortably. Excellent foundation for cardiovascular capillary density.";
        } else if (burnRate < 9.5) {
            intensityCategory = "Moderate Aerobic / Glycogen-Sparing (Zone 3)";
            exertionDescription = String.format("Burn rate of %s kcal/min represents a solid aerobic effort with substantial caloric expenditure.", formattedBurnRate);
            heartRateZone = "Zone 3 (Tempo / Aerobic Endurance, 70-80% Max HR)";
            pacingInsight = "Rhythmic, controlled pacing with steady metabolic delivery across all " + duration + " minutes.";
        } else if (burnRate < 14.0) {
            intensityCategory = "High Intensity / Lactate Threshold (Zone 4)";
            exertionDescription = String.format("Vigorous burn rate of %s kcal/min demonstrates heavy anaerobic glycolysis and peak cardiac output.", formattedBurnRate);
            heartRateZone = "Zone 4 (Threshold Training, 80-90% Max HR)";
            pacingInsight = "High metabolic velocity. Fast glycogen depletion; requires targeted carbohydrate replenishment.";
        } else {
            intensityCategory = "Peak Anaerobic / Neuromuscular Sprint (Zone 5)";
            exertionDescription = String.format("Exceptional burn rate of %s kcal/min marks peak cardiovascular effort near maximum VO2 capacity.", formattedBurnRate);
            heartRateZone = "Zone 5 (Peak Neuromuscular, >90% Max HR)";
            pacingInsight = "High-velocity power output requiring long rest intervals between sets or intervals.";
        }

        // Sport-specific biomechanics and coaching
        StringBuilder analysis = new StringBuilder();
        analysis.append("Overall: ").append(sport).append(" session completed for ").append(duration)
                .append(" minutes with ").append(calories).append(" total calories burned. ")
                .append("Classified as ").append(intensityCategory).append(".\n\n");
        analysis.append("Pace: ").append(pacingInsight).append("\n\n");
        analysis.append("Heart Rate: ").append(heartRateZone).append(". ").append(exertionDescription).append("\n\n");
        analysis.append("Calories Burned: ").append(calories).append(" kcal (Average metabolic burn rate: ").append(formattedBurnRate).append(" kcal/min).");

        List<String> improvements = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        List<String> safety = new ArrayList<>();

        switch (sport) {
            case "RUNNING":
                improvements.add("Cadence & Stride: Aim for 165–175 strides per minute to minimize ground contact time and joint impact forces.");
                improvements.add("Pacing Strategy: " + (burnRate > 10.0 ? "Practice negative splitting—start 15s/km slower for the first 10 minutes to preserve leg turnover." : "Maintain relaxed shoulders and engage core to prevent pelvis drop as fatigue sets in."));
                suggestions.add("Next Workout: " + (duration < 30 ? "Progression Run: 35-min session with the final 10 minutes at threshold tempo." : "Interval Fartlek: 30 mins with 6x1-minute high-cadence surges followed by 90s easy jog."));
                suggestions.add("Active Recovery: 20-min foam rolling focusing on calves, IT bands, and dynamic quad stretches.");
                safety.add("Hydration: Replenish approximately " + Math.max(350, duration * 10) + "ml of fluid with electrolytes post-session.");
                safety.add("Footwear Check: Replace running shoes every 500–700 km to protect knee cartilage and Achilles tendons.");
                break;

            case "CYCLING":
                improvements.add("Pedaling Mechanics: Maintain a smooth 85–95 RPM cadence rather than grinding heavy gear ratios to protect patellar tendons.");
                improvements.add("Aerodynamics & Posture: Relax upper body grip on handlebars to avoid neck tension and lower back stiffness.");
                suggestions.add("Next Workout: Sweet Spot Training (2x10 min intervals at 88-93% threshold with 5 min spin recovery).");
                suggestions.add("Active Recovery: 25-min low-resistance Zone 1 recovery spin followed by hip flexor and hamstring mobility.");
                safety.add("Post-Ride Nutrition: Consume 20-30g of protein and complex carbs within 45 minutes to accelerate glycogen resynthesis.");
                safety.add("Check tire pressure and maintain clean drivetrain before extended road sessions.");
                break;

            case "SWIMMING":
                improvements.add("Stroke Mechanics: Focus on high elbow catch during the pull phase to maximize propulsion per stroke.");
                improvements.add("Breathing Rhythm: Practice bilateral breathing (every 3 strokes) to balance shoulder rotation and spinal alignment.");
                suggestions.add("Next Workout: Technique pyramid: 4x50m kick drills, 4x100m moderate crawl, 2x50m sprint finish.");
                suggestions.add("Active Recovery: Scapular retraction exercises and door-frame pectoral stretches to prevent swimmer's shoulder.");
                safety.add("Rehydration: Even in the water, significant sweating occurs; drink at least 500ml water after swimming.");
                safety.add("Dry ears thoroughly and avoid sudden transition to cold environments.");
                break;

            case "WALKING":
                improvements.add("Brisk Cadence: Increase arm swing and push off the balls of the feet to boost caloric output by 15-20%.");
                improvements.add("Posture & Incline: Maintain upright spine and incorporate 3-5% incline intervals to stimulate glute activation.");
                suggestions.add("Next Workout: Power Walk: 45 minutes incorporating 5x2-minute fast-paced intervals.");
                suggestions.add("Active Recovery: 15-min lower back decompression and ankle mobility stretching.");
                safety.add("Wear supportive footwear with cushioned arches to avoid plantar fasciitis.");
                safety.add("Stay visible with reflective gear when walking during dawn or dusk.");
                break;

            case "YOGA":
                improvements.add("Breath Synchronization: Coordinate Ujjayi breathing with each vinyasa transition to regulate parasympathetic tone.");
                improvements.add("Alignment & Core: Prioritize spinal length over forward depth in hamstring and hip-opening poses.");
                suggestions.add("Next Workout: 45-min Dynamic Ashtanga or Power Vinyasa session targeting core stability.");
                suggestions.add("Active Recovery: 20-min Yin Yoga focusing on restorative hip and lower back releases.");
                safety.add("Never force joint extension; respect natural range of motion to protect ligaments.");
                safety.add("Allow 5-10 minutes of complete stillness in Savasana to integrate neural adaptations.");
                break;

            case "HIKING":
                improvements.add("Pacing on Gradients: Shorten stride length on uphill sections to keep heart rate in steady aerobic zones.");
                improvements.add("Trekking Poles: Utilize trekking poles to offload up to 20% of knee and ankle load during steep descents.");
                suggestions.add("Next Workout: 60-min weighted ruck or hilly trail hike with a light daypack.");
                suggestions.add("Active Recovery: Quad foam rolling and calf eccentric drops to relieve downhill eccentric stress.");
                safety.add("Carry at least 1 liter of water per 2 hours of trail activity, plus electrolyte tablets.");
                safety.add("Check trail terrain and weather forecast before venturing into isolated routes.");
                break;

            default:
                improvements.add("Progressive Overload: Gradually increase volume, tempo, or resistance by 5-10% weekly.");
                improvements.add("Movement Quality: Prioritize full range of motion and controlled eccentric tempo over raw speed.");
                suggestions.add("Next Workout: Balanced cross-training session with 25 minutes of aerobic conditioning and 15 minutes of core work.");
                suggestions.add("Active Recovery: Full-body mobility routine focusing on thoracic spine and hip flexor mobilization.");
                safety.add("Warm up dynamically for at least 5 minutes before elevating heart rate to high-intensity thresholds.");
                safety.add("Hydrate consistently throughout the day to support muscular recovery and cellular repair.");
                break;
        }

        if (duration > 60) {
            safety.add(0, "Extended Duration Alert: For sessions exceeding 60 minutes, ingest 30-60g of simple carbohydrates during or immediately after the session.");
        }

        return Recommendation.builder()
            .activityId(activity.getId())
            .userId(activity.getUserId())
            .type(sport)
            .recommendation(analysis.toString().trim())
            .improvements(improvements)
            .suggestions(suggestions)
            .safety(safety)
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
