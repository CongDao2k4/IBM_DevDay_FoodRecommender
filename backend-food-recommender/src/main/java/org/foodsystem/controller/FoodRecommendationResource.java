package org.foodsystem.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.foodsystem.agent.NutritionReflector;
import org.foodsystem.dto.RecommendationRequest;
import org.foodsystem.dto.RecommendationResponse;
import org.foodsystem.repository.FoodVectorRepository;
import org.foodsystem.service.ReflectionAgentService;

@Path("/api/chat")
public class FoodRecommendationResource {

    @Inject
    FoodVectorRepository foodVectorRepository;

    @Inject
    NutritionReflector nutritionReflector;

    @Inject
    ReflectionAgentService reflectionAgentService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RecommendationResponse processChat(RecommendationRequest request) {
        
        // 1. Tìm kiếm ngữ cảnh: Lấy thực phẩm từ DB Oracle dựa trên câu hỏi
        String retrievedFoods = foodVectorRepository.searchRelevantFoods(request.userQuery, 5);
        
        // Nếu không có dữ liệu, trả về thông báo cơ bản
        if (retrievedFoods == null || retrievedFoods.trim().isEmpty()) {
            return new RecommendationResponse("No relevant foods found in the database.");
        }

        // 2. Reflection Agent: Gọi Watsonx.ai (Granite) để đánh giá thực phẩm dựa trên bệnh lý
        String agentFinalReply = nutritionReflector.reflectAndRecommend(
                request.healthCondition,
                request.userQuery,
                retrievedFoods
        );

        // 3. Trả kết quả về cho Angular hiển thị
        return new RecommendationResponse(agentFinalReply);
    }

    @POST
    @Path("/programmatic")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public RecommendationResponse processChatProgrammatic(RecommendationRequest request) {
        // Alternative endpoint using programmatic ReflectionAgentService
        // This uses direct REST client calls to IBM Watsonx API
        String userProfile = request.fullName != null ? request.fullName : "User";
        String agentFinalReply = reflectionAgentService.processFoodRecommendation(
                userProfile,
                request.healthCondition
        );
        
        return new RecommendationResponse(agentFinalReply);
    }
}