/*
This app creates an epic story, two child stories and three tasks per child story
*/


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.CreateRequest;
import com.rallydev.rest.response.CreateResponse;
import com.rallydev.rest.util.Ref;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class CreateEpicChildStoriesTasks {

    public static void main(String[] args) throws URISyntaxException, IOException {
        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123"; 
        String applicationName = "Nick:createParentChildStoriesTasks";
        RallyRestApi restApi = null;
        DateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");

        try {
            restApi = new RallyRestApi(new URI(host),apiKey);
            restApi.setApplicationName(applicationName);
            String projectRef = "/project/1791266714"; 
            QueryRequest userRequest = new QueryRequest("User");
            userRequest.setFetch(new Fetch("UserName"));
            userRequest.setQueryFilter(new QueryFilter("UserName", "=", "user@company.com"));
            QueryResponse userQueryResponse = restApi.query(userRequest);
            JsonObject userJsonObject = userQueryResponse.getResults().get(0).getAsJsonObject();
            String userRef = userJsonObject.get("_ref").getAsString();
            for (int i=0; i<1; i++) {
                System.out.println("Creating a story...");
                JsonObject newEpic = new JsonObject();
                newEpic.addProperty("Name", "epic: " + iso.format(new Date()));
                newEpic.addProperty("Project", projectRef);
                newEpic.addProperty("Owner", userRef);
                CreateRequest createRequest = new CreateRequest("hierarchicalrequirement", newEpic);
                CreateResponse createResponse = restApi.create(createRequest);
                if (createResponse.wasSuccessful()) {
                    System.out.println("Created " + createResponse.getObject().get("_ref").getAsString());
                    String epicRef = Ref.getRelativeRef(createResponse.getObject().get("_ref").getAsString());
                    System.out.println("\nReading epic story " + epicRef);
                    for (int j=0; j<2; j++){
                        System.out.println("Creating a child story " + j);
                        JsonObject newChildStory = new JsonObject();
                        newChildStory.addProperty("Name", "story: " + iso.format(new Date()));
                        newChildStory.addProperty("Project", projectRef);
                        newChildStory.addProperty("Parent", epicRef);
                        newChildStory.addProperty("Owner", userRef);
                        CreateRequest createRequest2 = new CreateRequest("hierarchicalrequirement", newChildStory);
                        CreateResponse createResponse2 = restApi.create(createRequest2);
                        if (createResponse2.wasSuccessful()) {
                            System.out.println("Created " + createResponse.getObject().get("_ref").getAsString());
                            String childStoryRef = Ref.getRelativeRef(createResponse2.getObject().get("_ref").getAsString());
                            System.out.println("\nReading Child Story..." + childStoryRef);
                            for (int k=0; k<3; k++){
                                System.out.println("Creating a task " + k + " for " + childStoryRef);
                                JsonObject newTask = new JsonObject();
                                newTask.addProperty("Name", "task: " + iso.format(new Date()));
                                newTask.addProperty("Project", projectRef);
                                newTask.addProperty("WorkProduct", childStoryRef);
                                newTask.addProperty("Owner", userRef);
                                CreateRequest createRequest3 = new CreateRequest("task", newTask);
                                CreateResponse createResponse3 = restApi.create(createRequest3);
                                if (createResponse3.wasSuccessful()) {
                                    System.out.println("Created " + createResponse3.getObject().get("_ref").getAsString());
                                    String taskRef = Ref.getRelativeRef(createResponse3.getObject().get("_ref").getAsString());
                                    System.out.println("\nReading task ..." + taskRef);
                                }
                            }

                        }
                    }

                }
            }
        } finally {
            //Release all resources
            restApi.close();
        }
    }
}