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

public class UpdateStory {

	public static void main(String[] args) throws URISyntaxException, IOException {


		    String host = "https://rally1.rallydev.com";
	        String username = "user@co.com";
	        String password = "secret";
	        String projectRef = "/project/2222";             
	        String workspaceRef = "/workspace/11111";         
	        String applicationName = "Nick M: example of updating a story";
	        //String wsapiVersion = "v2.0";
		
        RallyRestApi restApi = new RallyRestApi(
        		new URI(host),
        		username,
        		password);
        restApi.setApplicationName(applicationName); 
        //restApi.setWsapiVersion(wsapiVersion);
        System.out.println(restApi.getWsapiVersion()); //v.2.0 by default when using 2.0.2 jar and up
      
        /**********************************FIND STORY***********************************************************/
        try{
	        QueryRequest storyRequest = new QueryRequest("HierarchicalRequirement");
	        storyRequest.setFetch(new Fetch("Name","Owner","UserName","PlanEstimate", "c_CustomText"));
	        storyRequest.setLimit(1000);
	        storyRequest.setScopedDown(false);
	        storyRequest.setScopedUp(false);
	        storyRequest.setWorkspace(workspaceRef);
	        storyRequest.setProject(projectRef);
	        storyRequest.setQueryFilter(new QueryFilter("FormattedID", "=", "US359"));
	        
	        QueryResponse storyQueryResponse = restApi.query(storyRequest);
	        JsonObject storyJsonObject = storyQueryResponse.getResults().get(0).getAsJsonObject();
	        String storyRef = storyJsonObject.get("_ref").getAsString();
	        System.out.println("Name: " + storyJsonObject.get("Name") + " PlanEstimate: " + storyJsonObject.get("PlanEstimate"));
	
	        JsonObject userObject = storyJsonObject.get("Owner").getAsJsonObject().getAsJsonObject();
	        System.out.println(userObject.get("UserName"));
	        
	    /*****************************************************UPDATE STORY PLAN ESTIMATE****************************************/
	        JsonObject storyUpdate = new JsonObject();
	        storyUpdate.addProperty("Description", "my description &()#*%");
	        storyUpdate.addProperty("PlanEstimate", 20);
	        storyUpdate.addProperty("c_CustomText", "this is my story...&()#*%");
	        UpdateRequest updateStoryRequest = new UpdateRequest(storyRef,storyUpdate);
	        UpdateResponse updateResponse = restApi.update(updateStoryRequest);
	        if (updateResponse.wasSuccessful()) {
				System.out.println("Successfully updated story: " + storyJsonObject.get("PlanEstimate") +
						" to: " + storyUpdate.get("PlanEstimate"));
				System.out.println("Successfully updated story: " + storyJsonObject.get("c_CustomText") +
						" to: " + storyUpdate.get("CustomText"));
				System.out.println(String.format("Updated %s", updateResponse.getObject().get("_ref").getAsString()));
				String[] warningList;
				warningList = updateResponse.getWarnings();
				for (int i=0;i<warningList.length;i++) {
				    System.out.println(warningList[i]);
					}
			    } else {
			    	System.out.println("Error occurred attempting to update story: " + storyJsonObject.get("Name"));
			    	String[] errorList;
			    	errorList = updateResponse.getErrors();
			    	for (int i=0;i<errorList.length;i++) {
			    		System.out.println(errorList[i]);
			    	}
			    }
        }catch(Exception e){
        	System.out.println("Exception occurred....");
    		e.printStackTrace();
        }
        finally{
        	//Release all resources
    	    restApi.close();
        }
	}
}