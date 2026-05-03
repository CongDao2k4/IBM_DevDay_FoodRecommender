package org.foodsystem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.foodsystem.client.IamClient;
import org.foodsystem.client.WatsonxClient;
import org.foodsystem.dto.WatsonxRequest;
import org.foodsystem.repository.FoodVectorRepository;

import java.net.URI;

@ApplicationScoped
public class ReflectionAgentService {

    @Inject
    @RestClient
    WatsonxClient watsonxClient;

    @Inject
    @RestClient
    IamClient iamClient;

    @Inject
    FoodVectorRepository foodVectorRepository;

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "ibm.watsonx.api-key")
    String apiKey;

    @ConfigProperty(name = "ibm.watsonx.project-id")
    String projectId;

    public String processFoodRecommendation(String userProfile, String healthCondition) {
        try {
            // Step 1: Fetch raw data from Oracle Vector Search
            String rawFoodData = foodVectorRepository.searchRelevantFoods(healthCondition, 10);

            // Step 2: Format the raw data into a draft recommendation
            String draftRecommendation = formatDraftRecommendation(rawFoodData);

            // Step 3: Get IAM Token from IBM Cloud
            URI iamUri = URI.create("https://iam.cloud.ibm.com/identity/token");
            String tokenResponse = iamClient.getIamToken("urn:ibm:params:oauth:grant-type:apikey", apiKey);
            
            // Extract access token from JSON response
            JsonNode tokenNode = objectMapper.readTree(tokenResponse);
            String accessToken = "Bearer " + tokenNode.get("access_token").asText();

            // Step 4: Create reflection prompt for the AI with advanced prompt engineering
            String reflectionPrompt = createReflectionPrompt(userProfile, healthCondition, draftRecommendation);

            // Step 5: Call Watsonx API with Granite model
            WatsonxRequest request = new WatsonxRequest("ibm/granite-3-8b-instruct", reflectionPrompt, projectId);
            String generationResponse = watsonxClient.generateText(accessToken, "2023-05-29", request);

            // Step 6: Parse the AI response and extract the generated JSON
            JsonNode responseNode = objectMapper.readTree(generationResponse);
            String aiGeneratedText = responseNode.get("results").get(0).get("generated_text").asText();

            // Step 7: Clean and extract JSON from the AI response
            String jsonResponse = extractAndValidateJson(aiGeneratedText);

            return jsonResponse;

        } catch (Exception e) {
            throw new RuntimeException("Error processing food recommendation: " + e.getMessage(), e);
        }
    }

    private String formatDraftRecommendation(String rawFoodData) {
        if (rawFoodData == null || rawFoodData.trim().isEmpty()) {
            return "No food items found in the database.";
        }
        return "Based on vector search, here are relevant food items:\n" + rawFoodData;
    }

    private String createReflectionPrompt(String userProfile, String healthCondition, String draftRecommendation) {
        return """
                You are an Expert Clinical Nutritionist with 20+ years of experience in medical nutrition therapy and dietary management for chronic diseases.
                
                TASK: Analyze the following food recommendation draft for a patient and provide a medically-sound assessment.
                
                PATIENT PROFILE:
                %s
                
                HEALTH CONDITION:
                %s
                
                DRAFT FOOD RECOMMENDATION:
                %s
                
                INSTRUCTIONS:
                1. Review each food item in the draft recommendation
                2. Identify foods that are SAFE for this specific health condition
                3. Identify and remove foods that are UNSAFE or contraindicated
                4. Provide a brief medical explanation for your assessment
                
                OUTPUT FORMAT:
                You MUST respond with ONLY a valid JSON object. Do NOT include markdown formatting, code blocks, or any text outside the JSON.
                
                Required JSON structure:
                {
                  "safe_foods": ["food1", "food2", "food3"],
                  "unsafe_foods_removed": ["food4", "food5"],
                  "medical_explanation": "Brief clinical reasoning for this recommendation based on the patient's condition."
                }
                
                CRITICAL RULES:
                - Output ONLY the JSON object, nothing else
                - Do NOT wrap the JSON in ```json or ``` markers
                - Ensure all JSON keys use double quotes
                - Keep medical_explanation concise (2-3 sentences maximum)
                - If all foods are safe, use empty array [] for unsafe_foods_removed
                - If no foods are safe, use empty array [] for safe_foods
                
                Begin your response with { and end with }
                """.formatted(userProfile, healthCondition, draftRecommendation);
    }

    private String extractAndValidateJson(String aiResponse) {
        try {
            // Remove any leading/trailing whitespace
            String cleaned = aiResponse.trim();
            
            // Remove markdown code blocks if present (```json or ```)
            cleaned = cleaned.replaceAll("^```json\\s*", "").replaceAll("^```\\s*", "");
            cleaned = cleaned.replaceAll("\\s*```$", "");
            cleaned = cleaned.trim();
            
            // Find the first { and last } to extract JSON
            int startIndex = cleaned.indexOf('{');
            int endIndex = cleaned.lastIndexOf('}');
            
            if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
                throw new IllegalArgumentException("No valid JSON object found in AI response");
            }
            
            String jsonString = cleaned.substring(startIndex, endIndex + 1);
            
            // Validate that it's proper JSON by parsing it
            JsonNode validatedJson = objectMapper.readTree(jsonString);
            
            // Verify required fields exist
            if (!validatedJson.has("safe_foods") ||
                !validatedJson.has("unsafe_foods_removed") ||
                !validatedJson.has("medical_explanation")) {
                throw new IllegalArgumentException("JSON response missing required fields");
            }
            
            // Return the validated JSON string
            return objectMapper.writeValueAsString(validatedJson);
            
        } catch (Exception e) {
            // Fallback: create a default error response
            String fallbackJson = """
                {
                  "safe_foods": [],
                  "unsafe_foods_removed": [],
                  "medical_explanation": "Unable to process AI response. Please consult with a healthcare professional for personalized dietary advice."
                }
                """;
            return fallbackJson.trim();
        }
    }
}

// Made with Bob
