import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.request.UpdateRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.response.UpdateResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;

public class AddStepsToTestCase {

    public static void main(String[] args) throws Exception {

        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123"; 
        String applicationName = "Add Steps To TC"; 
        String workspaceRef = "/workspace/12343"; 

        RallyRestApi restApi = null;
        try {
            restApi = new RallyRestApi(new URI(host), apiKey);
            restApi.setApplicationName(applicationName);
            QueryRequest request = new QueryRequest("TestCase");
            request.setFetch(new Fetch("FormattedID", "Name", "Steps"));
            request.setWorkspace(workspaceRef);
            request.setQueryFilter(new QueryFilter("FormattedID", "=", "TC18"));
            QueryResponse response = restApi.query(request);
            JsonObject testCaseJsonObject = response.getResults().get(0).getAsJsonObject();

            String testCaseRef = testCaseJsonObject.get("_ref").getAsString();
            int numberOfSteps = testCaseJsonObject.getAsJsonObject("Steps").get("Count").getAsInt();
            System.out.println(testCaseJsonObject.get("Name") + " ref: " + testCaseRef + "number of steps: " + numberOfSteps + " " + testCaseJsonObject.get("Steps"));
            if (response.wasSuccessful()) {
                JsonObject stepOne = new JsonObject();
                JsonObject stepTwo = new JsonObject();
                stepOne.addProperty("Input", "Open Database Connection");
                stepOne.addProperty("StepIndex", 1);
                stepOne.addProperty("StepIndex", 2);
                stepOne.addProperty("TestCase", testCaseRef);
                stepTwo.addProperty("Input", "2+2");
                stepTwo.addProperty("ExpectedResult", "4");
                stepTwo.addProperty("TestCase", testCaseRef);
                CreateRequest createRequest = new CreateRequest("testcasestep", stepOne);
                CreateResponse createResponse = restApi.create(createRequest);
                CreateRequest createRequest2 = new CreateRequest("testcasestep", stepTwo);
                CreateResponse createResponse2 = restApi.create(createRequest2);
            }else {
                    System.out.println("false? " + response.wasSuccessful());
            }
        } finally {
            if (restApi != null) {
                restApi.close();
            }
        }
    }
}