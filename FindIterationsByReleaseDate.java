import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class FindIterationsByReleaseDate {

	public static void main(String[] args) throws URISyntaxException, IOException {


		String host = "https://rally1.rallydev.com";
	        String username = "user@co.com";
	        String password = "secret";
	        String projectRef = "/project/1234";
	        String applicationName = "RESTExampleFindReleasesByProject";
	        
	        RallyRestApi restApi = null;
	        
	    try {
		        restApi = new RallyRestApi(
		        		new URI(host),
		        		username,
		        		password);
		        restApi.setApplicationName(applicationName); 
		        
		        System.out.println(restApi.getWsapiVersion()); 

		        QueryRequest  releaseRequest = new QueryRequest("Release");
		        releaseRequest.setFetch(new Fetch("ReleaseStartDate", "ReleaseDate"));
		        releaseRequest.setScopedDown(false);
		        releaseRequest.setScopedUp(false);
		        releaseRequest.setProject(projectRef);
		        releaseRequest.setQueryFilter(new QueryFilter("Name", "=", "r1"));
		        
		        QueryResponse releaseQueryResponse = restApi.query(releaseRequest);

		        JsonObject releaseJsonObject = releaseQueryResponse.getResults().get(0).getAsJsonObject();
		        
		        String rsd = releaseJsonObject.get("ReleaseStartDate").getAsString();
		        String rd = releaseJsonObject.get("ReleaseDate").getAsString();
		        
		        QueryRequest  iterationRequest = new QueryRequest("Iteration");
		        iterationRequest.setFetch(new Fetch("Name","StartDate","EndDate"));
		        iterationRequest.setScopedDown(false);
		        iterationRequest.setScopedUp(false);
		        iterationRequest.setProject(projectRef);
		        iterationRequest.setQueryFilter(new QueryFilter("StartDate", ">=", rsd).and(new QueryFilter("EndDate", "<=", rd)));
		        
		        QueryResponse iterationQueryResponse = restApi.query(iterationRequest);
		        int numberOfIteraitons = iterationQueryResponse.getTotalResultCount();
		        System.out.println(numberOfIteraitons);
		        if(numberOfIteraitons >0){
		            for (int i=0;i<numberOfIteraitons;i++){
		            	JsonObject iterationJsonObject = iterationQueryResponse.getResults().get(i).getAsJsonObject();
		            	System.out.println(iterationJsonObject.get("Name"));
		            }
		        }       
	    }
	    finally{
	    	if (restApi != null) {
				restApi.close();
			}
	    }
         
	}
}