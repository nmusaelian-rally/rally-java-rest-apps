//This code toggles Custom Text field value on a User from empty to current milliseconds, and from a non-empty value to empty

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.util.Fetch;

import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.request.UpdateRequest;
import com.rallydev.rest.response.UpdateResponse;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.QueryFilter;
import java.util.Date;


public class ClearCustomUserTextField{
    public static void main(String[] args) throws URISyntaxException, IOException {


        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123"; //use your ApiKey
        String applicationName = "ClearCustomUserTextField";


        RallyRestApi restApi = new RallyRestApi(new URI(host),apiKey);
        restApi.setApplicationName(applicationName);

        try {
            QueryRequest userRequest = new QueryRequest("User");
            userRequest.setFetch(new Fetch("UserName", "DisplayName", "c_Comments"));
            userRequest.setQueryFilter(new QueryFilter("UserName", "=", "nick@denver.com"));
            QueryResponse userQueryResponse = restApi.query(userRequest);
            JsonArray userQueryResults = userQueryResponse.getResults();
            JsonElement userQueryElement = userQueryResults.get(0);
            JsonObject userObject = userQueryElement.getAsJsonObject();
            String userRef = userObject.get("_ref").getAsString();
            System.out.println(userObject.get("UserName") + " Comments field value: " + userObject.get("c_Comments"));
            if(userObject.get("c_Comments").isJsonNull()){
                String newComment = new Date().getTime()+""; //converting long to string
                System.out.println("Comments field is empty. Updating to: " + newComment);
                update(restApi, userRef, newComment);
            }
            else{
                System.out.println("not empty");
                System.out.println("Comments field is not empty. Clearing the value...");
                update(restApi, userRef, "");
            }
        } finally {
            //Release all resources
            restApi.close();
        }

    }
    private static void update(RallyRestApi restApi, String userRef, String comment ) throws URISyntaxException,IOException {
        JsonObject userUpdate = new JsonObject();
        userUpdate.addProperty("c_Comments", comment);
        UpdateRequest updateRequest = new UpdateRequest(userRef,userUpdate);
        UpdateResponse updateResponse = restApi.update(updateRequest);

        if (updateResponse.wasSuccessful()) {
            System.out.println("Successfully updated user");
        } else {
            System.out.println("Error occurred attempting to update user");
            String[] errorList;
            errorList = updateResponse.getErrors();
            for (int e=0;e<errorList.length;e++) {
                System.out.println(errorList[e]);
            }
        }
    }

}