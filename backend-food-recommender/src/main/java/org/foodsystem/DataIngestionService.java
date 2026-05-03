// package org.foodsystem;

// import com.opencsv.CSVReader;
// import com.opencsv.exceptions.CsvException;
// import dev.langchain4j.data.embedding.Embedding;
// import dev.langchain4j.model.embedding.EmbeddingModel;
// //import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
// import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
// import io.agroal.api.AgroalDataSource;
// import io.quarkus.logging.Log;
// import io.quarkus.runtime.Startup;
// import io.quarkus.runtime.StartupEvent;
// import jakarta.enterprise.context.ApplicationScoped;
// import jakarta.enterprise.event.Observes;
// import jakarta.inject.Inject;
// import jakarta.transaction.Transactional;
// import oracle.sql.VECTOR;

// import java.io.IOException;
// import java.io.InputStreamReader;
// import java.nio.charset.StandardCharsets;
// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.SQLException;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.UUID;

// /**
//  * DataIngestionService - Startup component for ingesting CSV data into Oracle 23ai
//  * Reads three CSV files, generates embeddings using LangChain4j, and inserts into database
//  */
// // @Startup
// // @ApplicationScoped
// public class DataIngestionService {

//     @Inject
//     AgroalDataSource dataSource;

//     private final EmbeddingModel embeddingModel;

//     public DataIngestionService() {
//         // Initialize the embedding model (runs in-process)
//         this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();
//         Log.info("AllMiniLmL6V2EmbeddingModel initialized successfully");
//     }

//     /**
//      * This method is called automatically when the application starts
//      */
//     void onStart(@Observes StartupEvent event) {
//         Log.info("Application started - triggering data ingestion");
//         ingestData();
//     }

//     @Transactional
//     public void ingestData() {
//         System.out.println("=".repeat(80));
//         System.out.println("--- STARTING DATA INGESTION INTO ORACLE DB ---");
//         System.out.println("=".repeat(80));
//         Log.info("Starting data ingestion process...");
        
//         try {
//             ingestNutritionData();
//             ingestAllergenData();
//             ingestHealthRules();
            
//             System.out.println("=".repeat(80));
//             System.out.println("--- DATA INGESTION COMPLETED SUCCESSFULLY ---");
//             System.out.println("=".repeat(80));
//             Log.info("Data ingestion completed successfully!");
//         } catch (Exception e) {
//             System.out.println("=".repeat(80));
//             System.out.println("--- DATA INGESTION FAILED ---");
//             System.out.println("=".repeat(80));
//             Log.error("Error during data ingestion", e);
//             throw new RuntimeException("Data ingestion failed", e);
//         }
//     }

//     /**
//      * Ingest nutrition data from nutrients_csvfile.csv
//      */
//     private void ingestNutritionData() throws IOException, CsvException, SQLException {
//         Log.info("Ingesting nutrition data...");
        
//         String csvFile = "/nutrients_csvfile.csv";
//         List<String[]> records = readCSV(csvFile);
        
//         if (records.isEmpty()) {
//             Log.warn("No nutrition data found in CSV file");
//             return;
//         }
        
//         // Skip header row
//         records.remove(0);
        
//         String sql = """
//             INSERT INTO FOOD_NUTRITION 
//             (id, food_name, measure, weight_grams, calories, protein, fat, sat_fat, fiber, carbs, category, text_content, embedding)
//             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
//             """;
        
//         try (Connection conn = dataSource.getConnection();
//              PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
//             int count = 0;
//             for (String[] record : records) {
//                 try {
//                     // Parse CSV columns (adjust indices based on actual CSV structure)
//                     String foodName = getStringValue(record, 0);
//                     String measure = getStringValue(record, 1);
//                     float weightGrams = parseFloatWithTrace(record, 2);
//                     float calories = parseFloatWithTrace(record, 3);
//                     float protein = parseFloatWithTrace(record, 4);
//                     float fat = parseFloatWithTrace(record, 5);
//                     float satFat = parseFloatWithTrace(record, 6);
//                     float fiber = parseFloatWithTrace(record, 7);
//                     float carbs = parseFloatWithTrace(record, 8);
//                     String category = getStringValue(record, 9);
                    
//                     // Generate text content for embedding
//                     String textContent = String.format(
//                         "%s in %s per %s contains %.1f kcal, %.1fg protein, %.1fg fat, %.1fg saturated fat, %.1fg fiber, %.1fg carbs",
//                         foodName, category, measure, calories, protein, fat, satFat, fiber, carbs
//                     );
                    
//                     // Generate embedding
//                     float[] embedding = generateEmbedding(textContent);
                    
//                     // Set parameters
//                     pstmt.setBytes(1, uuidToBytes(UUID.randomUUID()));
//                     pstmt.setString(2, foodName);
//                     pstmt.setString(3, measure);
//                     pstmt.setFloat(4, weightGrams);
//                     pstmt.setFloat(5, calories);
//                     pstmt.setFloat(6, protein);
//                     pstmt.setFloat(7, fat);
//                     pstmt.setFloat(8, satFat);
//                     pstmt.setFloat(9, fiber);
//                     pstmt.setFloat(10, carbs);
//                     pstmt.setString(11, category);
//                     pstmt.setString(12, textContent);
                    
//                     // Set vector using Oracle VECTOR format
//                     pstmt.setObject(13, convertToOracleVector(embedding));
                    
//                     pstmt.addBatch();
//                     count++;
                    
//                     if (count % 100 == 0) {
//                         pstmt.executeBatch();
//                         Log.infof("Inserted %d nutrition records", count);
//                     }
//                 } catch (Exception e) {
//                     Log.warnf("Skipping invalid nutrition record: %s", e.getMessage());
//                 }
//             }
            
//             // Execute remaining batch
//             pstmt.executeBatch();
//             Log.infof("Successfully inserted %d nutrition records", count);
//         }
//     }

//     /**
//      * Ingest allergen data from food_ingredients_and_allergens.csv
//      */
//     private void ingestAllergenData() throws IOException, CsvException, SQLException {
//         Log.info("Ingesting allergen data...");
        
//         String csvFile = "/food_ingredients_and_allergens.csv";
//         List<String[]> records = readCSV(csvFile);
        
//         if (records.isEmpty()) {
//             Log.warn("No allergen data found in CSV file");
//             return;
//         }
        
//         // Skip header row
//         records.remove(0);
        
//         String sql = """
//             INSERT INTO FOOD_ALLERGEN 
//             (id, food_product, main_ingredient, sweetener, fat_oil, seasoning, allergens, prediction, text_content, embedding)
//             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
//             """;
        
//         try (Connection conn = dataSource.getConnection();
//              PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
//             int count = 0;
//             for (String[] record : records) {
//                 try {
//                     // Parse CSV columns
//                     String foodProduct = getStringValue(record, 0);
//                     String mainIngredient = getStringValue(record, 1);
//                     String sweetener = getStringValue(record, 2);
//                     String fatOil = getStringValue(record, 3);
//                     String seasoning = getStringValue(record, 4);
//                     String allergens = getStringValue(record, 5);
//                     String prediction = getStringValue(record, 6);
                    
//                     // Generate text content for embedding
//                     String textContent = String.format(
//                         "%s contains ingredients: %s, %s, %s, %s. Allergens: %s. Prediction: %s",
//                         foodProduct, mainIngredient, sweetener, fatOil, seasoning, allergens, prediction
//                     );
                    
//                     // Generate embedding
//                     float[] embedding = generateEmbedding(textContent);
                    
//                     // Set parameters
//                     pstmt.setBytes(1, uuidToBytes(UUID.randomUUID()));
//                     pstmt.setString(2, foodProduct);
//                     pstmt.setString(3, mainIngredient);
//                     pstmt.setString(4, sweetener);
//                     pstmt.setString(5, fatOil);
//                     pstmt.setString(6, seasoning);
//                     pstmt.setString(7, allergens);
//                     pstmt.setString(8, prediction);
//                     pstmt.setString(9, textContent);
//                     pstmt.setObject(10, convertToOracleVector(embedding));
                    
//                     pstmt.addBatch();
//                     count++;
                    
//                     if (count % 100 == 0) {
//                         pstmt.executeBatch();
//                         Log.infof("Inserted %d allergen records", count);
//                     }
//                 } catch (Exception e) {
//                     Log.warnf("Skipping invalid allergen record: %s", e.getMessage());
//                 }
//             }
            
//             // Execute remaining batch
//             pstmt.executeBatch();
//             Log.infof("Successfully inserted %d allergen records", count);
//         }
//     }

//     /**
//      * Ingest health rules from health_rules.csv
//      */
//     private void ingestHealthRules() throws IOException, CsvException, SQLException {
//         Log.info("Ingesting health rules...");
        
//         String csvFile = "/health_rules.csv";
//         List<String[]> records = readCSV(csvFile);
        
//         if (records.isEmpty()) {
//             Log.warn("No health rules found in CSV file");
//             return;
//         }
        
//         // Skip header row
//         records.remove(0);
        
//         String sql = """
//             INSERT INTO HEALTH_RULE 
//             (id, food_category, allergen_risk, medical_warning, text_content, embedding)
//             VALUES (?, ?, ?, ?, ?, ?)
//             """;
        
//         try (Connection conn = dataSource.getConnection();
//              PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
//             int count = 0;
//             for (String[] record : records) {
//                 try {
//                     // Parse CSV columns
//                     String foodCategory = getStringValue(record, 0);
//                     String allergenRisk = getStringValue(record, 1);
//                     String medicalWarning = getStringValue(record, 2);
                    
//                     // Generate text content for embedding
//                     String textContent = String.format(
//                         "Category %s allergen risk: %s. Warning: %s",
//                         foodCategory, allergenRisk, medicalWarning
//                     );
                    
//                     // Generate embedding
//                     float[] embedding = generateEmbedding(textContent);
                    
//                     // Set parameters
//                     pstmt.setBytes(1, uuidToBytes(UUID.randomUUID()));
//                     pstmt.setString(2, foodCategory);
//                     pstmt.setString(3, allergenRisk);
//                     pstmt.setString(4, medicalWarning);
//                     pstmt.setString(5, textContent);
//                     pstmt.setObject(6, convertToOracleVector(embedding));
                    
//                     pstmt.addBatch();
//                     count++;
                    
//                     if (count % 100 == 0) {
//                         pstmt.executeBatch();
//                         Log.infof("Inserted %d health rule records", count);
//                     }
//                 } catch (Exception e) {
//                     Log.warnf("Skipping invalid health rule record: %s", e.getMessage());
//                 }
//             }
            
//             // Execute remaining batch
//             pstmt.executeBatch();
//             Log.infof("Successfully inserted %d health rule records", count);
//         }
//     }

//     /**
//      * Read CSV file from resources
//      */
//     private List<String[]> readCSV(String resourcePath) throws IOException, CsvException {
//         try (var inputStream = getClass().getResourceAsStream(resourcePath)) {
//             if (inputStream == null) {
//                 throw new IOException("CSV file not found: " + resourcePath);
//             }
            
//             try (var reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
//                  var csvReader = new CSVReader(reader)) {
//                 return csvReader.readAll();
//             }
//         }
//     }

//     /**
//      * Generate embedding vector from text using LangChain4j
//      */
//     private float[] generateEmbedding(String text) {
//         Embedding embedding = embeddingModel.embed(text).content();
//         return embedding.vector();
//     }

//     /**
//      * Parse float value, treating 't' (trace) as 0
//      */
//     private float parseFloatWithTrace(String[] record, int index) {
//         String value = getStringValue(record, index);
        
//         if (value == null || value.trim().isEmpty()) {
//             return 0.0f;
//         }
        
//         // Handle 't' for trace amounts
//         if (value.trim().equalsIgnoreCase("t")) {
//             return 0.0f;
//         }
        
//         try {
//             return Float.parseFloat(value.trim());
//         } catch (NumberFormatException e) {
//             Log.warnf("Invalid float value '%s' at index %d, defaulting to 0", value, index);
//             return 0.0f;
//         }
//     }

//     /**
//      * Safely get string value from CSV record
//      */
//     private String getStringValue(String[] record, int index) {
//         if (index >= 0 && index < record.length) {
//             String value = record[index];
//             return value != null ? value.trim() : "";
//         }
//         return "";
//     }

//     /**
//      * Convert UUID to byte array for Oracle RAW(16)
//      */
//     private byte[] uuidToBytes(UUID uuid) {
//         byte[] bytes = new byte[16];
//         long msb = uuid.getMostSignificantBits();
//         long lsb = uuid.getLeastSignificantBits();
        
//         for (int i = 0; i < 8; i++) {
//             bytes[i] = (byte) (msb >>> (8 * (7 - i)));
//         }
//         for (int i = 8; i < 16; i++) {
//             bytes[i] = (byte) (lsb >>> (8 * (15 - i)));
//         }
        
//         return bytes;
//     }

//     /**
//      * Convert float array to Oracle VECTOR type
//      */
//     private VECTOR convertToOracleVector(float[] embedding) throws SQLException {
//         // Convert float array to double array (Oracle VECTOR expects double[])
//         double[] doubleArray = new double[embedding.length];
//         for (int i = 0; i < embedding.length; i++) {
//             doubleArray[i] = embedding[i];
//         }
//         return VECTOR.ofFloat32Values(doubleArray);
//     }
// }

// // Made with Bob
