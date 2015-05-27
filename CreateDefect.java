import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.CreateRequest;

import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.CreateResponse;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.QueryFilter;
import com.rallydev.rest.util.Ref;

public class CreateDefect{
    public static void main(String[] args) throws URISyntaxException, IOException {


        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123"; //use your ApiKey
        
        String projectRef = "/project/12345"; 
        String applicationName = "RestExample_createDefect";


        RallyRestApi restApi = new RallyRestApi(new URI(host),apiKey);
        restApi.setApplicationName(applicationName);

        //Read User
        QueryRequest userRequest = new QueryRequest("User");
        userRequest.setQueryFilter(new QueryFilter("UserName", "=", "user@company.com"));
        QueryResponse userQueryResponse = restApi.query(userRequest);
        JsonArray userQueryResults = userQueryResponse.getResults();
        JsonElement userQueryElement = userQueryResults.get(0);
        JsonObject userQueryObject = userQueryElement.getAsJsonObject();
        String userRef = userQueryObject.get("_ref").getAsString();



        try {
            for (int i=0; i<1; i++) {
                System.out.println("Creating a defect...");
                JsonObject newDefect = new JsonObject();
                newDefect.addProperty("Name", "bug12345again");
                newDefect.addProperty("Project", projectRef);
                
                CreateRequest createRequest = new CreateRequest("defect", newDefect);
                CreateResponse createResponse = restApi.create(createRequest);
                if (createResponse.wasSuccessful()) {

                    System.out.println(String.format("Created %s %s", createResponse.getObject().get("FormattedID"), createResponse.getObject().get("_ref").getAsString()));

                    //Read defect
                    String ref = Ref.getRelativeRef(createResponse.getObject().get("_ref").getAsString());
                    System.out.println(String.format("\nReading Defect %s...", ref));

                } else {
                    String[] createErrors;
                    createErrors = createResponse.getErrors();
                    System.out.println("Error occurred creating a defect: ");
                    for (int j=0; i<createErrors.length;j++) {
                        System.out.println(createErrors[j]);
                    }
                }
            }
        } finally {
            //Release all resources
            restApi.close();
        }

    }
}