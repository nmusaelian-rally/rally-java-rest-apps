import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import java.net.URI;


public class GetChildStories {
	
	public static void main(String[] args) throws Exception {

		String host = "https://rally1.rallydev.com";
		String apiKey = "_abc123";
                String applicationName = "Find Child Stories of Epics";
                //String workspaceRef = "/workspace/12352608129";
		String projectRef = "/project/14018981229";     
                RallyRestApi restApi = null;
		try {
			restApi = new RallyRestApi(new URI(host),apiKey);
			QueryRequest storyRequest = new QueryRequest("HierarchicalRequirement");
			//storyRequest.setWorkspace(workspaceRef);
			storyRequest.setProject(projectRef);
			restApi.setApplicationName(applicationName);  
			storyRequest.setFetch(new Fetch(new String[] {"Name", "FormattedID", "Tags", "Children"}));
			storyRequest.setLimit(1000);
			storyRequest.setScopedDown(true);
			storyRequest.setScopedUp(false);
			//storyRequest.setQueryFilter((new QueryFilter("Tags.Name", "contains", "\"tag1\"")).and(new QueryFilter("DirectChildrenCount", ">", "0")));
			storyRequest.setQueryFilter((new QueryFilter("LastUpdateDate", ">", "\"2014-01-01\"")).and(new QueryFilter("ScheduleState", "<", "Completed")));
			QueryResponse storyQueryResponse = restApi.query(storyRequest);
			System.out.println("Successful: " + storyQueryResponse.wasSuccessful());
			System.out.println("Size: " + storyQueryResponse.getTotalResultCount());
			
			for (int i=0; i<storyQueryResponse.getTotalResultCount();i++){
				JsonObject storyJsonObject = storyQueryResponse.getResults().get(i).getAsJsonObject();
				System.out.println("Name: " + storyJsonObject.get("Name") + " FormattedID: " + storyJsonObject.get("FormattedID"));
				QueryRequest childrenRequest = new QueryRequest(storyJsonObject.getAsJsonObject("Children"));
				childrenRequest.setFetch(new Fetch("Name","FormattedID"));
				int numberOfChildren = storyJsonObject.get("DirectChildrenCount").getAsInt();
				System.out.println(numberOfChildren);
			    //load the collection
			    JsonArray children = restApi.query(childrenRequest).getResults();
			    for (int j=0;j<numberOfChildren;j++){
				System.out.println("Name: " + children.get(j).getAsJsonObject().get("Name") + children.get(j).getAsJsonObject().get("FormattedID").getAsString());
			    System.out.println("Name: " + children.get(0).getAsJsonObject().get("Name") + children.get(0).getAsJsonObject().get("FormattedID").getAsString());
			    }
				}
			
	        
		} finally {
			if (restApi != null) {
				restApi.close();
			}
		}
	}

}
