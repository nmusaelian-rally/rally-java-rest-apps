import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import java.net.URI;


public class GetPI {
	
	public static void main(String[] args) throws Exception {

		String host = "https://rally1.rallydev.com";
		String apiKey = "_abc123";
		String applicationName = "Find PIs";
		String workspaceRef = "/workspace/12345"; //Use valid OID in your subscription
        
		RallyRestApi restApi = null;
		try {
			restApi = new RallyRestApi(new URI(host),apiKey);
			QueryRequest portfolioRequest = new QueryRequest("PortfolioItem");
			portfolioRequest.setWorkspace(workspaceRef);
			restApi.setApplicationName(applicationName);  
			portfolioRequest.setFetch(new Fetch("State,FormattedID,Name,UserStories,DirectChildrenCount,PercentDoneByStoryPlanEstimate"));
			portfolioRequest.setLimit(1000);
			portfolioRequest.setScopedDown(false);
			portfolioRequest.setScopedUp(false);
	        
			portfolioRequest.setQueryFilter(new QueryFilter("DirectChildrenCount", ">", "0").and(new QueryFilter("State.Name", "!=", "Complete")));
	        
			QueryResponse portfolioResponse = restApi.query(portfolioRequest);
			System.out.println("Successful: " + portfolioResponse.wasSuccessful());
	        
	        
			for (int i=0; i<portfolioResponse.getTotalResultCount();i++){
				JsonObject portfolioJsonObject = portfolioResponse.getResults().get(i).getAsJsonObject();
				System.out.println("Name: " + portfolioJsonObject.get("Name") + " FormattedID: " + portfolioJsonObject.get("FormattedID"));
			}
	        
		} finally {
			if (restApi != null) {
				restApi.close();
			}
		}
	}
}