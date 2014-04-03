import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import com.rallydev.rest.request.UpdateRequest;
import com.rallydev.rest.response.UpdateResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class GetTeamMembers {

	public static void main(String[] args) throws URISyntaxException, IOException {
		String host = "https://rally1.rallydev.com";
        String username = "user@co.com";
        String password = "secret";
        String workspaceRef = "/workspace/12352608129"; 
        String applicationName = "RESTExample get team members";
	
	    RallyRestApi restApi = new RallyRestApi(
	    		new URI(host),
	    		username,
	    		password);
	    restApi.setApplicationName(applicationName); 
	    System.out.println(restApi.getWsapiVersion()); 
	    /*
		query on projects where Name contains "Team"
		get the TeamMembers collection
		query on the collection
		the output will look like this:
		
		Project: "Team 2"
		UserA
		UserB
		UserC
		
	    */
	    try{
	    	 QueryRequest projectRequest = new QueryRequest("Project");
	    	 projectRequest.setFetch(new Fetch("Name", "TeamMembers"));
	    	 projectRequest.setWorkspace(workspaceRef);
	    	 projectRequest.setQueryFilter(new QueryFilter("Name", "contains", "Team"));   
		     QueryResponse projectQueryResponse = restApi.query(projectRequest);
		     int count = projectQueryResponse.getResults().size();
		     System.out.println(count);
		     if(count > 0){
		    	 for (int i=0;i<count;i++){
		    		 JsonObject projectObject = projectQueryResponse.getResults().get(i).getAsJsonObject();
		    		 System.out.println("Project: " + projectObject.get("_refObjectName"));
				     int numberOfTeamMembers = projectObject.getAsJsonObject("TeamMembers").get("Count").getAsInt();
				     if(numberOfTeamMembers > 0) {
			                QueryRequest teamRequest = new QueryRequest(projectObject.getAsJsonObject("TeamMembers"));
			                JsonArray teammates = restApi.query(teamRequest).getResults();
			                for (int j=0;j<numberOfTeamMembers;j++){
			                    System.out.println(teammates.get(j).getAsJsonObject().get("_refObjectName").getAsString());
			                }
			            }
		    	 }
		     }
		    
	    }catch(Exception e){
			e.printStackTrace();
	    	
	    } finally{
		    restApi.close();
	    }
	}
}