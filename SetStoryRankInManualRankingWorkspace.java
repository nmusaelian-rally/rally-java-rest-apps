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

public class SetStoryRankInManualRankingWorkspace {

    public static void main(String[] args) throws URISyntaxException, IOException {

        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123"; 
        String applicationName = "NickM:SetStoryRank"; 
        String workspaceRef = "/workspace/1234";//manual ranking workspace
        RallyRestApi restApi = null;
        restApi = new RallyRestApi(new URI(host),apiKey);
        restApi.setApplicationName(applicationName);

        /**********************************FIND STORY***********************************************************/
        try{
            QueryRequest storyRequest = new QueryRequest("HierarchicalRequirement");
            storyRequest.setFetch(new Fetch("Name","Rank","PlanEstimate"));
            storyRequest.setWorkspace(workspaceRef);
            storyRequest.setQueryFilter(new QueryFilter("FormattedID", "=", "US71"));
            QueryResponse storyQueryResponse = restApi.query(storyRequest);
            JsonObject storyJsonObject = storyQueryResponse.getResults().get(0).getAsJsonObject();
            String storyRef = storyJsonObject.get("_ref").getAsString();
            System.out.println("Name: " + storyJsonObject.get("Name") + " Rank: " + storyJsonObject.get("Rank"));

            /*****************************************************UPDATE RANK****************************************/
            JsonObject storyUpdate = new JsonObject();
            storyUpdate.addProperty("Rank", 340.0);
            //storyUpdate.addProperty("PlanEstimate", 10);
            UpdateRequest updateStoryRequest = new UpdateRequest(storyRef,storyUpdate);
            //Rank update will not work without setting workspace parameter if workspace is not default:
            updateStoryRequest.addParam("workspace","/workspace/1234"); 
            UpdateResponse updateResponse = restApi.update(updateStoryRequest);

            if (updateResponse.wasSuccessful()) {
                System.out.println("Successfully updated story: " + storyJsonObject.get("Rank") +
                        " to: " + storyUpdate.get("Rank"));
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