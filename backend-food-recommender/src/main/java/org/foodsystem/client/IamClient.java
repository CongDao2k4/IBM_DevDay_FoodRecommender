package org.foodsystem.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

// Trỏ trực tiếp lên domain của IAM
@RegisterRestClient(baseUri = "https://iam.cloud.ibm.com")
public interface IamClient {

    @POST
    @Path("/identity/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    String getIamToken(@FormParam("grant_type") String grantType, 
                       @FormParam("apikey") String apiKey);
}