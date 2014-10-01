import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.request.UpdateRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.response.UpdateResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;

public class UpdateTestCase {

	public static void main(String[] args) throws URISyntaxException, IOException {
		

	       String host = "https://rally1.rallydev.com";
	       String apiKey = "_abc123";
	       String workspaceRef = "/workspace/123456";
	       String applicationName = "RestExample_updateWorkProductOnTestCase";
	        
		
	       RallyRestApi restApi = new RallyRestApi(new URI(host),apiKey);
           restApi.setApplicationName(applicationName);   

        try {
        	String testid = "TC12";
        	String storyid = "US34";
        	
        	QueryRequest testCaseRequest = new QueryRequest("TestCase");
            testCaseRequest.setWorkspace(workspaceRef);
            testCaseRequest.setFetch(new Fetch("FormattedID", "Name", "WorkProduct"));
            testCaseRequest.setQueryFilter(new QueryFilter("FormattedID", "=",  testid));
            QueryResponse testCaseQueryResponse = restApi.query(testCaseRequest);;
            
            if (testCaseQueryResponse.getTotalResultCount() == 0) {
             System.out.println("Cannot find test case : " + testid);
             return;
            }
            JsonObject testCaseJsonObject = testCaseQueryResponse.getResults().get(0).getAsJsonObject();
            String testCaseRef = testCaseJsonObject.get("_ref").getAsString();
            System.out.println(testCaseRef);
            
            QueryRequest storyRequest = new QueryRequest("HierarchicalRequirement");
            storyRequest.setWorkspace(workspaceRef);
            storyRequest.setFetch(new Fetch("FormattedID", "Name"));
            storyRequest.setQueryFilter(new QueryFilter("FormattedID", "=",  storyid));
            QueryResponse storyQueryResponse = restApi.query(storyRequest);;
            
            if (storyQueryResponse.getTotalResultCount() == 0) {
             System.out.println("Cannot find test story : " + storyid);
             return;
            }
            JsonObject storyJsonObject = storyQueryResponse.getResults().get(0).getAsJsonObject();
            String storyRef = storyJsonObject.get("_ref").getAsString();
            System.out.println(storyRef);
            
            JsonObject testCaseUpdate = new JsonObject();
            testCaseUpdate.addProperty("WorkProduct", storyRef);
            UpdateRequest updateTestCaseRequest = new UpdateRequest(testCaseRef,testCaseUpdate);
	        UpdateResponse updateTestCaseResponse = restApi.update(updateTestCaseRequest);
	        if (updateTestCaseResponse.wasSuccessful()) {
				System.out.println("Successfully updated : " + testid + " WorkProduct after update: " + testCaseUpdate.get("WorkProduct"));
				
	        }
        	
        } finally {
            restApi.close();
        }   
	} 
}