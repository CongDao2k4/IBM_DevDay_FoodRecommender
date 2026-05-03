package org.foodsystem.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.foodsystem.dto.WatsonxRequest;

@RegisterRestClient(configKey = "watsonx-api")
public interface WatsonxClient {

    // Call Watsonx Granite model for text generation
    @POST
    @Path("/ml/v1/text/generation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    String generateText(@HeaderParam("Authorization") String token,
                        @QueryParam("version") String version,
                        WatsonxRequest request);
}