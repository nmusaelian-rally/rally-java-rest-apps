import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.CreateRequest;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.request.UpdateRequest;
import com.rallydev.rest.response.CreateResponse;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.response.UpdateResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import com.rallydev.rest.util.Ref;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class CreateChangeset {
	public static void main(String[] args) throws URISyntaxException, IOException {
	       String host = "https://rally1.rallydev.com";
	       String apiKey = "_abc123";
	       String workspaceRef = "/workspace/12345";  //user your workspace ref
	       String applicationName = "RestExample_createChangeset";
	        

	    RallyRestApi restApi = new RallyRestApi(new URI(host),apiKey);
        restApi.setApplicationName(applicationName);   
        try {
			restApi = new RallyRestApi(new URI(host),apiKey);
	        QueryRequest request = new QueryRequest("HierarchicalRequirement");
	        request.setWorkspace(workspaceRef);
	        restApi.setApplicationName(applicationName);  
	        request.setFetch(new Fetch("Name","FormattedID"));
	        request.setLimit(1000);
	        request.setScopedDown(false);
	        request.setScopedUp(false);
	        
	        request.setQueryFilter(new QueryFilter("FormattedID", "=", "US2936")); //user your story formattedid
	        
	        QueryResponse response = restApi.query(request);
	        if(response.getResults().size()>0){
		        JsonObject jsonObject = response.getResults().get(0).getAsJsonObject();
		        System.out.println("Found User Story: " + jsonObject.get("FormattedID") + " Name: " + jsonObject.get("Name"));
		        String message = "again worked on " + jsonObject.get("FormattedID") + "<br />line2<br />line3<p>paragraph</p>";
		        DateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		        String timestamp = iso.format(new Date())+"";
		        JsonObject newChangeset = new JsonObject();
		        newChangeset.addProperty("SCMRepository", "/scmrepository/16953479942");  //user your scmrepository ref
		        newChangeset.addProperty("CommitTimestamp", timestamp); 
		        newChangeset.addProperty("Revision", "4");
		        newChangeset.addProperty("Message", message);
		        CreateRequest createRequest = new CreateRequest("changeset", newChangeset);
	            CreateResponse createResponse = restApi.create(createRequest);  
	            if (createResponse.wasSuccessful()) {
	            	JsonObject changesetJsonObject = createResponse.getObject();
	            	String changesetRef = Ref.getRelativeRef(createResponse.getObject().get("_ref").getAsString());
	            	System.out.println(String.format("Created %s", changesetRef));  
	            	JsonObject artifactsOfThisChangeset = (JsonObject) changesetJsonObject.get("Artifacts");
	            	int numberOfArtifacts = artifactsOfThisChangeset.get("Count").getAsInt();
	            	QueryRequest artifactsOfThisChangesetRequest = new QueryRequest(artifactsOfThisChangeset);
	            	JsonArray artifacts = restApi.query(artifactsOfThisChangesetRequest).getResults();
	            	artifacts.add(jsonObject);

		            JsonObject changesetUpdate = new JsonObject();
			        changesetUpdate.add("Artifacts", artifacts);
			        UpdateRequest updateChangesetRequest = new UpdateRequest(changesetRef,changesetUpdate);
			        UpdateResponse updateResponse = restApi.update(updateChangesetRequest);
			        if (updateResponse.wasSuccessful()) {
						System.out.println("Successfully updated changeset");
			        }
			        else {
			        	String[] updateErrors;
		            	updateErrors = createResponse.getErrors();
		        		System.out.println("Error");
		            	for (int i=0; i<updateErrors.length;i++) {
		            		System.out.println(updateErrors[i]);
		            	}
			        }
		             
		            
	            } else {
	            	String[] createErrors;
	            	createErrors = createResponse.getErrors();
	        		System.out.println("Error occurred creating a changeset: ");
	            	for (int i=0; i<createErrors.length;i++) {
	            		System.out.println(createErrors[i]);
	            	}
	            }
            	
	        }  
	        else{
	        	System.out.println("false? " + response.wasSuccessful());
	        }
        	
	
        } finally {
            //Release all resources
            restApi.close();
        }   

	} 
	
}