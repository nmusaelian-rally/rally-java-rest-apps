import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class FindDeleted {

	public static void main(String[] args) throws URISyntaxException, IOException {


		String host = "https://rally1.rallydev.com";
	        String username = "user@co.com";
	        String password = "secret";
	        String projectRef = "/project/12352608219";             //use your OID
	        String workspaceRef = "/workspace/12352608129";   //use your OID
	        String applicationName = "Query recycle bin";
	        
	        RallyRestApi restApi = null;
	        
	    try {
		        restApi = new RallyRestApi(
		        		new URI(host),
		        		username,
		        		password);
		        restApi.setApplicationName(applicationName); 
		       
		        QueryRequest  recyclebinRequest = new QueryRequest("RecycleBin");
		        recyclebinRequest.setFetch(new Fetch("ObjectID","Name","DeletedBy","DeletionDate","Type"));
		        recyclebinRequest.setLimit(10);
		        recyclebinRequest.setScopedDown(true);
		        recyclebinRequest.setScopedUp(false);
		        //storyRequest.setWorkspace(workspaceRef);
		        recyclebinRequest.setProject(projectRef);
		        
		        QueryResponse recyclebinQueryResponse = restApi.query(recyclebinRequest);
		        int resultCount = recyclebinQueryResponse.getTotalResultCount();
		        System.out.println(resultCount);
		        
		        if(resultCount >0){
		            for (int i=0;i<resultCount;i++){
		            	JsonObject recyclebinJsonObject = recyclebinQueryResponse.getResults().get(i).getAsJsonObject();
		            	System.out.println(recyclebinJsonObject.get("Name"));
		            }
		        }
		        else{
		        	System.out.println("No results");
		        }
	    }
	    finally{
	    	if (restApi != null) {
				restApi.close();
			}
	    }
         
	}
}