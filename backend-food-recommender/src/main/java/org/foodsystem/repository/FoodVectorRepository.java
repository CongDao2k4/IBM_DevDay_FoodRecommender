package org.foodsystem.repository;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class FoodVectorRepository {

    @Inject
    EntityManager entityManager;

    @Inject
    EmbeddingModel embeddingModel; // Model local sẽ tự động được inject

    public String searchRelevantFoods(String userQuery, int limit) {
        // Step 1: Convert user query to vector using local embedding model
        Embedding queryEmbedding = embeddingModel.embed(userQuery).content();
        String vectorString = queryEmbedding.vectorAsList().toString();

        // Step 2: Use Oracle 23ai VECTOR_DISTANCE to search across all food-related tables
        // Query FOOD_NUTRITION table for nutritional information
        String sql = "SELECT food_name, text_content FROM FOOD_NUTRITION " +
                     "ORDER BY VECTOR_DISTANCE(embedding, :vector) FETCH FIRST :limit ROWS ONLY";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("vector", vectorString);
        query.setParameter("limit", limit);

        List<Object[]> results = query.getResultList();

        // Step 3: Combine results into text context
        StringBuilder context = new StringBuilder();
        
        if (!results.isEmpty()) {
            context.append("=== Nutritional Information ===\n");
            for (Object[] row : results) {
                context.append("- ").append(row[0])
                       .append(": ").append(row[1] != null ? row[1] : "No details available")
                       .append("\n");
            }
        }
        
        // Also search FOOD_ALLERGEN table for allergen information
        String allergenSql = "SELECT food_product, allergens, text_content FROM FOOD_ALLERGEN " +
                            "ORDER BY VECTOR_DISTANCE(embedding, :vector) FETCH FIRST :limit ROWS ONLY";
        
        Query allergenQuery = entityManager.createNativeQuery(allergenSql);
        allergenQuery.setParameter("vector", vectorString);
        allergenQuery.setParameter("limit", limit);
        
        List<Object[]> allergenResults = allergenQuery.getResultList();
        
        if (!allergenResults.isEmpty()) {
            context.append("\n=== Allergen Information ===\n");
            for (Object[] row : allergenResults) {
                context.append("- ").append(row[0])
                       .append(" (Allergens: ").append(row[1] != null ? row[1] : "None")
                       .append("): ").append(row[2] != null ? row[2] : "No details")
                       .append("\n");
            }
        }
        
        // Search HEALTH_RULE table for health warnings
        String healthSql = "SELECT food_category, medical_warning, text_content FROM HEALTH_RULE " +
                          "ORDER BY VECTOR_DISTANCE(embedding, :vector) FETCH FIRST :limit ROWS ONLY";
        
        Query healthQuery = entityManager.createNativeQuery(healthSql);
        healthQuery.setParameter("vector", vectorString);
        healthQuery.setParameter("limit", limit);
        
        List<Object[]> healthResults = healthQuery.getResultList();
        
        if (!healthResults.isEmpty()) {
            context.append("\n=== Health Rules & Warnings ===\n");
            for (Object[] row : healthResults) {
                context.append("- ").append(row[0])
                       .append(": ").append(row[1] != null ? row[1] : "No warnings")
                       .append(" | ").append(row[2] != null ? row[2] : "")
                       .append("\n");
            }
        }
        
        return context.toString();
    }
}