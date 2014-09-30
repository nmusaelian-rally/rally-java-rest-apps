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

public class UpdateTestCaseWithTag {

	public static void main(String[] args) throws URISyntaxException, IOException {
		

	       String host = "https://rally1.rallydev.com";
	       String apiKey = "_abc123";
	       String workspaceRef = "/workspace/1234567";
	       String applicationName = "RestExample_updateTCwithTag";
	        
		
	       RallyRestApi restApi = new RallyRestApi(new URI(host),apiKey);
           restApi.setApplicationName(applicationName);   

        try {
        	String tagname = "tag1";
        	String testid = "TC32";
        	QueryRequest  tagRequest = new QueryRequest("Tag");
        	tagRequest.setWorkspace(workspaceRef);
        	tagRequest.setQueryFilter(new QueryFilter("Name", "=", tagname));
        	QueryResponse tagQueryResponse = restApi.query(tagRequest);
            if(tagQueryResponse.getTotalResultCount() == 0){
            	System.out.println("Cannot find tag: " + tagname);
                return;
            }
            JsonObject tagJsonObject = tagQueryResponse.getResults().get(0).getAsJsonObject();
 	        String tagRef = tagJsonObject.get("_ref").getAsString();
 	        System.out.println(tagRef);
            QueryRequest testCaseRequest = new QueryRequest("TestCase");
            testCaseRequest.setWorkspace(workspaceRef);
            testCaseRequest.setFetch(new Fetch("FormattedID", "Name", "Tags"));
            testCaseRequest.setQueryFilter(new QueryFilter("FormattedID", "=",  testid));
            QueryResponse testCaseQueryResponse = restApi.query(testCaseRequest);;
            
            if (testCaseQueryResponse.getTotalResultCount() == 0) {
             System.out.println("Cannot find test case : " + testid);
             return;
            }
            JsonObject testCaseJsonObject = testCaseQueryResponse.getResults().get(0).getAsJsonObject();
            String testCaseRef = testCaseJsonObject.get("_ref").getAsString();
            System.out.println(testCaseRef);
            int numberOfTags = testCaseJsonObject.getAsJsonObject("Tags").get("Count").getAsInt();
            System.out.println(numberOfTags + " tag(s) on " + testid);
        	QueryRequest tagCollectionRequest = new QueryRequest(testCaseJsonObject.getAsJsonObject("Tags"));
        	tagCollectionRequest.setFetch(new Fetch("Name"));
    	    JsonArray tags = restApi.query(tagCollectionRequest).getResults();
    	    
    	    for (int j=0;j<numberOfTags;j++){
    	        System.out.println("Tag Name: " + tags.get(j).getAsJsonObject().get("Name"));
    	    }
    	    tags.add(tagJsonObject);
    	    JsonObject testCaseUpdate = new JsonObject();
    	    testCaseUpdate.add("Tags", tags);
    	    UpdateRequest updateTestCaseRequest = new UpdateRequest(testCaseRef,testCaseUpdate);
	        UpdateResponse updateTestCaseResponse = restApi.update(updateTestCaseRequest);
	        if (updateTestCaseResponse.wasSuccessful()) {
				System.out.println("Successfully updated : " + testid + " Tags after update: ");
				QueryRequest tagCollectionRequest2 = new QueryRequest(testCaseJsonObject.getAsJsonObject("Tags"));
				tagCollectionRequest.setFetch(new Fetch("Name"));
	    	    JsonArray tagsAfterUpdate = restApi.query(tagCollectionRequest).getResults();
	    	    int numberOfTagsAfterUpdate = restApi.query(tagCollectionRequest).getResults().size();
	    	    for (int j=0;j<numberOfTagsAfterUpdate;j++){
	    	        System.out.println("Tag Name: " + tagsAfterUpdate.get(j).getAsJsonObject().get("Name"));
	    	    }
	        }
        } finally {
            restApi.close();
        }   

	} 
}