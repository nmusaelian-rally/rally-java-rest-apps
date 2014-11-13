import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import java.net.URI;


public class FindStoryByFormattedID {
	
	public static void main(String[] args) throws Exception {

		String host = "https://rally1.rallydev.com";
		String apiKey = "_abc123";
		String applicationName = "Find story by FID";
		String workspaceRef = "/workspace/12343"; //User your workspace ObjectID
        
		RallyRestApi restApi = null;
			try {
				restApi = new RallyRestApi(new URI(host),apiKey);
				QueryRequest request = new QueryRequest("HierarchicalRequirement");
				request.setWorkspace(workspaceRef);
				restApi.setApplicationName(applicationName);  
				request.setFetch(new Fetch("ObjectID"));
				request.setLimit(1000);
				request.setScopedDown(false);
				request.setScopedUp(false);
	        
				request.setQueryFilter(new QueryFilter("FormattedID", "=", "US7")); ////User your story FormattedID
	        
				QueryResponse response = restApi.query(request);
				if(response.wasSuccessful()){
					System.out.println("Number of results: " + response.getResults().size());
					for (int i=0; i<response.getTotalResultCount();i++){
						JsonObject jsonObject = response.getResults().get(i).getAsJsonObject();
						System.out.println("OID: " + jsonObject.get("ObjectID"));
					}
				}  
				else{
					System.out.println("false? " + response.wasSuccessful());
				}
			} finally {
				if (restApi != null) {
					restApi.close();
				}
		}
	}
}