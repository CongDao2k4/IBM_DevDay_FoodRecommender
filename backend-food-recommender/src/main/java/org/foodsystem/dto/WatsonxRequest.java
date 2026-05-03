package org.foodsystem.dto;

import java.util.HashMap;
import java.util.Map;

public class WatsonxRequest {
    private String model_id;
    private String input;
    private String project_id;
    private Map<String, Object> parameters;

    public WatsonxRequest() {
        this.parameters = new HashMap<>();
        this.parameters.put("max_new_tokens", 900);
    }

    public WatsonxRequest(String model_id, String input, String project_id) {
        this();
        this.model_id = model_id;
        this.input = input;
        this.project_id = project_id;
    }

    // Getters and Setters
    public String getModel_id() {
        return model_id;
    }

    public void setModel_id(String model_id) {
        this.model_id = model_id;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getProject_id() {
        return project_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}

// Made with Bob
