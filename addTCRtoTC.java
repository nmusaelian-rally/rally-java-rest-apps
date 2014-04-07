import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.CreateRequest;
import com.rallydev.rest.request.GetRequest;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.CreateResponse;
import com.rallydev.rest.response.GetResponse;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import com.rallydev.rest.util.Ref;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class addTCRtoTC {

	public static void main(String[] args) throws URISyntaxException, IOException {
		String host = "https://rally1.rallydev.com";
	        String username = "user@co.com";
	        String password = "secret";
	        String wsapiVersion = "v2.0";
	        String projectRef = "/project/2222";      
	        String workspaceRef = "/workspace/11111"; 
	        String applicationName = "RestExample_AddTCR";
	        
		
        RallyRestApi restApi = new RallyRestApi(
        		new URI(host),
        		username,
        		password);
        restApi.setWsapiVersion(wsapiVersion);
        restApi.setApplicationName(applicationName);   
        
 
        
        //Read User
        QueryRequest userRequest = new QueryRequest("User");
        userRequest.setFetch(new Fetch("UserName", "Subscription", "DisplayName", "SubscriptionAdmin"));
        userRequest.setQueryFilter(new QueryFilter("UserName", "=", "nick@wsapi.com"));
        QueryResponse userQueryResponse = restApi.query(userRequest);
        JsonArray userQueryResults = userQueryResponse.getResults();
        JsonElement userQueryElement = userQueryResults.get(0);
        JsonObject userQueryObject = userQueryElement.getAsJsonObject();
        String userRef = userQueryObject.get("_ref").getAsString();  
        System.out.println(userRef);
      
        // Query for Test Case to which we want to add results
        QueryRequest testCaseRequest = new QueryRequest("TestCase");
        testCaseRequest.setFetch(new Fetch("FormattedID","Name"));
        testCaseRequest.setWorkspace(workspaceRef);
        testCaseRequest.setQueryFilter(new QueryFilter("FormattedID", "=", "TC6"));
        QueryResponse testCaseQueryResponse = restApi.query(testCaseRequest);
        JsonObject testCaseJsonObject = testCaseQueryResponse.getResults().get(0).getAsJsonObject();
        String testCaseRef = testCaseQueryResponse.getResults().get(0).getAsJsonObject().get("_ref").getAsString(); 
	
        try {
        	for (int i=0; i<2; i++) {

	            //Add a Test Case Result    
        		System.out.println(testCaseRef);
	            System.out.println("Creating Test Case Result...");
	            JsonObject newTestCaseResult = new JsonObject();
	            newTestCaseResult.addProperty("Verdict", "Pass");
	            newTestCaseResult.addProperty("Date", "2014-03-07T18:00:00.000Z");
	            newTestCaseResult.addProperty("Notes", "Some Scheduled Test");
	            newTestCaseResult.addProperty("Build", "2.0");
	            newTestCaseResult.addProperty("Tester", userRef);
	            newTestCaseResult.addProperty("TestCase", testCaseRef);
	            newTestCaseResult.addProperty("Workspace", workspaceRef);
	            
	            CreateRequest createRequest = new CreateRequest("testcaseresult", newTestCaseResult);
	            CreateResponse createResponse = restApi.create(createRequest);  
	            if (createResponse.wasSuccessful()) {
	            	
	            	System.out.println(String.format("Created %s", createResponse.getObject().get("_ref").getAsString()));          
	            	
		            //Read Test Case
		            String ref = Ref.getRelativeRef(createResponse.getObject().get("_ref").getAsString());
		            System.out.println(String.format("\nReading Test Case Result %s...", ref));
		            GetRequest getRequest = new GetRequest(ref);
		            getRequest.setFetch(new Fetch("Date", "Verdict"));
		            GetResponse getResponse = restApi.get(getRequest);
		            JsonObject obj = getResponse.getObject();
		            System.out.println(String.format("my Read Test Case Result. Date = %s, Verdict = %s",
		                    obj.get("Date").getAsString(), obj.get("Verdict").getAsString()));		           
	            } else {
	            	String[] createErrors;
	            	createErrors = createResponse.getErrors();
	        		System.out.println("Error occurred creating Test Case Result: ");
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