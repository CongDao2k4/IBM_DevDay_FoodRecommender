-- =====================================================
-- Flyway Migration: V2__Init_Schema.sql
-- Description: Initialize schema for LangChain4j RAG architecture
-- Database: Oracle 23ai with Vector support
-- =====================================================

-- Drop existing tables if they exist (Oracle 23ai syntax)
BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE FOOD_NUTRITION CASCADE CONSTRAINTS';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE FOOD_ALLERGEN CASCADE CONSTRAINTS';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE HEALTH_RULE CASCADE CONSTRAINTS';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN
            RAISE;
        END IF;
END;
/

-- Table 1: FOOD_NUTRITION
-- Stores nutritional information for various foods
CREATE TABLE FOOD_NUTRITION (
    id              RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    food_name       VARCHAR2(255) NOT NULL,
    measure         VARCHAR2(100),
    weight_grams    FLOAT,
    calories        FLOAT,
    protein         FLOAT,
    fat             FLOAT,
    sat_fat         FLOAT,
    fiber           FLOAT,
    carbs           FLOAT,
    category        VARCHAR2(100),
    text_content    CLOB,
    embedding       VECTOR(384, FLOAT32)
);

-- Table 2: FOOD_ALLERGEN
-- Stores allergen information for food products
CREATE TABLE FOOD_ALLERGEN (
    id              RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    food_product    VARCHAR2(255),
    main_ingredient VARCHAR2(255),
    sweetener       VARCHAR2(255),
    fat_oil         VARCHAR2(255),
    seasoning       VARCHAR2(255),
    allergens       VARCHAR2(1000),
    prediction      VARCHAR2(100),
    text_content    CLOB,
    embedding       VECTOR(384, FLOAT32)
);

-- Table 3: HEALTH_RULE
-- Stores health rules and medical warnings
CREATE TABLE HEALTH_RULE (
    id              RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    food_category   VARCHAR2(255),
    allergen_risk   VARCHAR2(500),
    medical_warning CLOB,
    text_content    CLOB,
    embedding       VECTOR(384, FLOAT32)
);

-- Create vector indexes on embedding columns for vector similarity search
-- Using NEIGHBOR PARTITIONS organization (memory-efficient alternative to INMEMORY)
-- with HNSW algorithm and cosine distance
CREATE VECTOR INDEX idx_food_nutrition_embedding
ON FOOD_NUTRITION (embedding)
ORGANIZATION NEIGHBOR PARTITIONS
DISTANCE COSINE
WITH TARGET ACCURACY 90;

CREATE VECTOR INDEX idx_food_allergen_embedding
ON FOOD_ALLERGEN (embedding)
ORGANIZATION NEIGHBOR PARTITIONS
DISTANCE COSINE
WITH TARGET ACCURACY 90;

CREATE VECTOR INDEX idx_health_rule_embedding
ON HEALTH_RULE (embedding)
ORGANIZATION NEIGHBOR PARTITIONS
DISTANCE COSINE
WITH TARGET ACCURACY 90;

-- Create indexes on commonly queried columns
CREATE INDEX idx_food_nutrition_name ON FOOD_NUTRITION(food_name);
CREATE INDEX idx_food_nutrition_category ON FOOD_NUTRITION(category);
CREATE INDEX idx_food_allergen_product ON FOOD_ALLERGEN(food_product);
CREATE INDEX idx_health_rule_category ON HEALTH_RULE(food_category);

-- Made with Bob
