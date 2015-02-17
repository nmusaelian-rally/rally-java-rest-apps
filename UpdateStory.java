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
        String apiKey = "_abc123"; 
        String applicationName = "Find story by FormattedID"; //Use your API Key or your username/password.
        //String workspaceRef = "/workspace/12352608219";     //Use your workspace and project ObjectIDs
        String projectRef = "/project/12352608219";
        RallyRestApi restApi = null;
        restApi = new RallyRestApi(new URI(host),apiKey);
        restApi.setApplicationName(applicationName);
        System.out.println(restApi.getWsapiVersion());

        /**********************************FIND STORY***********************************************************/
        try{
            QueryRequest storyRequest = new QueryRequest("HierarchicalRequirement");
            storyRequest.setFetch(new Fetch("Name","Owner","UserName","PlanEstimate", "c_CustomText"));
            storyRequest.setLimit(1000);
            storyRequest.setScopedDown(false);
            storyRequest.setScopedUp(false);
            //storyRequest.setWorkspace(workspaceRef);
            storyRequest.setProject(projectRef);
            storyRequest.setQueryFilter(new QueryFilter("FormattedID", "=", "US3074"));

            QueryResponse storyQueryResponse = restApi.query(storyRequest);
            JsonObject storyJsonObject = storyQueryResponse.getResults().get(0).getAsJsonObject();
            String storyRef = storyJsonObject.get("_ref").getAsString();
            System.out.println("Name: " + storyJsonObject.get("Name") + " PlanEstimate: " + storyJsonObject.get("PlanEstimate"));

            JsonObject userObject = storyJsonObject.get("Owner").getAsJsonObject().getAsJsonObject();
            System.out.println(userObject.get("UserName"));

            /*****************************************************UPDATE STORY PLAN ESTIMATE****************************************/
            JsonObject storyUpdate = new JsonObject();
            storyUpdate.addProperty("PlanEstimate", 0); //test setting PlanEstimate to 0
            UpdateRequest updateStoryRequest = new UpdateRequest(storyRef,storyUpdate);
            UpdateResponse updateResponse = restApi.update(updateStoryRequest);
            if (updateResponse.wasSuccessful()) {
                System.out.println("Successfully updated story: " + storyJsonObject.get("PlanEstimate") +
                        " to: " + storyUpdate.get("PlanEstimate"));
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