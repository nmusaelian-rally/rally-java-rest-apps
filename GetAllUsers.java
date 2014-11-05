
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import java.net.URI;


public class GetAllUsers {
	
	public static void main(String[] args) throws Exception {

		String host = "https://rally1.rallydev.com";
		String apiKey = "_abc123";
               String applicationName = "get all users";
        
        RallyRestApi restApi = null;
		try {
			restApi = new RallyRestApi(new URI(host),apiKey);
	        QueryRequest request = new QueryRequest("User");
	        restApi.setApplicationName(applicationName);  
	        request.setFetch(new Fetch(new String[] {"UserName"}));
	        request.setLimit(1000);
	        //request.setQueryFilter(((new QueryFilter("CreationDate", "<", "2014-01-01").and(new QueryFilter("LastLoginDate","=",null))).or(new QueryFilter("LastLoginDate", "<", "2014-11-01"))).and(new QueryFilter("Disabled","=","false")));
	        
	        QueryResponse response = restApi.query(request);
	        System.out.println("TotalResultCount: " + response.getTotalResultCount());
	        
	        for (int i=0; i<response.getTotalResultCount();i++){
	        	JsonObject userJsonObject = response.getResults().get(i).getAsJsonObject();
	        	System.out.println("Name: " + userJsonObject.get("UserName"));
			}
	        
	        
		} finally {
			if (restApi != null) {
				restApi.close();
			}
		}
	}
}