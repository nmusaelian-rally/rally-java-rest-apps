import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import java.net.URI;


public class GetStoriesByTag {
	
	public static void main(String[] args) throws Exception {

	String host = "https://rally1.rallydev.com";
	String username = "user@co.com";
        String password = "secret";
        String applicationName = "Find Stories by Tag";
        String workspaceRef = "/workspace/12352608129";
        
        RallyRestApi restApi = null;
		try {
	        restApi = new RallyRestApi(
	        		new URI(host),
	        		username,
	        		password);
	        restApi.setApplicationName(applicationName); 
	     
	        QueryRequest storyRequest = new QueryRequest("HierarchicalRequirement");
	        storyRequest.setWorkspace(workspaceRef);

	        storyRequest.setFetch(new Fetch(new String[] {"Name", "FormattedID", "Tags"}));
	        storyRequest.setLimit(1000);
	        storyRequest.setScopedDown(false);
	        storyRequest.setScopedUp(false);
	        
	        storyRequest.setQueryFilter((new QueryFilter("Tags.Name", "contains", "\"tag1\"")).and(new QueryFilter("ScheduleState", "<", "Completed")));
	        
	        QueryResponse storyQueryResponse = restApi.query(storyRequest);
	        System.out.println("Successful: " + storyQueryResponse.wasSuccessful());
	        System.out.println("Size: " + storyQueryResponse.getTotalResultCount());
	        System.out.println("Results Size: " + storyQueryResponse.getResults().size());
	        
	        for (int i=0; i<storyQueryResponse.getResults().size();i++){
	        	JsonObject storyJsonObject = storyQueryResponse.getResults().get(i).getAsJsonObject();
	        	System.out.println("Name: " + storyJsonObject.get("Name") + " FormattedID: " + storyJsonObject.get("FormattedID"));
	        	int numberOfTags = storyJsonObject.getAsJsonObject("Tags").get("Count").getAsInt();
	        	QueryRequest tagRequest = new QueryRequest(storyJsonObject.getAsJsonObject("Tags"));
        	    tagRequest.setFetch(new Fetch("Name","FormattedID"));
        	    //load the collection
        	    JsonArray tags = restApi.query(tagRequest).getResults();
        	    for (int j=0;j<numberOfTags;j++){
        	        System.out.println("Tag Name: " + tags.get(j).getAsJsonObject().get("Name"));
        	    }
			}
	        
	        
		} finally {
			if (restApi != null) {
				restApi.close();
			}
		}
	}

}