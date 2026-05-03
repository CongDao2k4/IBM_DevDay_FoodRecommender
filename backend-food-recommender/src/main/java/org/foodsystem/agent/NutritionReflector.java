package org.foodsystem.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface NutritionReflector {

    @SystemMessage("""
        You are an expert Medical AI & Reflective Nutrition Agent. 
        Your task is to review a list of foods retrieved from the database and evaluate them against the user's health condition.
        You must strictly forbid foods that are harmful to their condition.
        Format your response clearly with two sections: 'Satisfied (Safe)' and 'Not Satisfied (Avoid)'.
        Always reply in English.
        """)
    @UserMessage("""
        User's Health Condition: {healthProfile}
        User's Request: {query}
        Retrieved Foods from Database: {foodData}
        
        Reflect on this data and provide your final recommendation.
        """)
    String reflectAndRecommend(@V("healthProfile") String healthProfile, 
                               @V("query") String query, 
                               @V("foodData") String foodData);
}