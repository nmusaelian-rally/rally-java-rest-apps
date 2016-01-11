import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.request.UpdateRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.response.UpdateResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;

public class AddMilestone {

    public static void main(String[] args) throws URISyntaxException, IOException {


        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123";
        String workspaceRef = "/workspace/12352608129";
        String applicationName = "Nick: updateMilestoneCollection";


        RallyRestApi restApi = new RallyRestApi(new URI(host),apiKey);
        restApi.setApplicationName(applicationName);

        try {
            String milestoneName = "Mid January"; 
            String storyId = "US30015"; 
            QueryRequest  milestoneRequest = new QueryRequest("Milestone");
            milestoneRequest.setWorkspace(workspaceRef);
            milestoneRequest.setQueryFilter(new QueryFilter("Name", "=", milestoneName));
            QueryResponse milestoneQueryResponse = restApi.query(milestoneRequest);
            if(milestoneQueryResponse.getTotalResultCount() == 0){
                System.out.println("Cannot find milestone: " + milestoneName);
                return;
            }
            JsonObject milestoneJsonObject = milestoneQueryResponse.getResults().get(0).getAsJsonObject();
            String milestoneRef = milestoneJsonObject.get("_ref").getAsString();
            System.out.println(milestoneRef);
            QueryRequest storyRequest = new QueryRequest("HierarchicalRequirement");
            storyRequest.setWorkspace(workspaceRef);
            storyRequest.setFetch(new Fetch("FormattedID", "Name", "Milestones"));
            storyRequest.setQueryFilter(new QueryFilter("FormattedID", "=",  storyId));
            QueryResponse storyQueryResponse = restApi.query(storyRequest);;

            if (storyQueryResponse.getTotalResultCount() == 0) {
                System.out.println("Cannot find story : " + storyId);
                return;
            }

            JsonObject storyJsonObject = storyQueryResponse.getResults().get(0).getAsJsonObject();
            String storyRef = storyJsonObject.get("_ref").getAsString();
            System.out.println(storyRef);
            int numberOfMilestones = storyJsonObject.getAsJsonObject("Milestones").get("Count").getAsInt();
            System.out.println(numberOfMilestones + " milestone(s) on " + storyId);
            QueryRequest milestoneCollectionRequest = new QueryRequest(storyJsonObject.getAsJsonObject("Milestones"));
            milestoneCollectionRequest.setFetch(new Fetch("Name"));
            JsonArray milestones = restApi.query(milestoneCollectionRequest).getResults();

            for (int j=0;j<numberOfMilestones;j++){
                System.out.println("Milestone Name: " + milestones.get(j).getAsJsonObject().get("Name"));
            }
            milestones.add(milestoneJsonObject);
            JsonObject storyUpdate = new JsonObject();
            storyUpdate.add("Milestones", milestones);
            UpdateRequest updateStoryRequest = new UpdateRequest(storyRef,storyUpdate);
            UpdateResponse updateStoryResponse = restApi.update(updateStoryRequest);
            if (updateStoryResponse.wasSuccessful()) {
                System.out.println("Successfully updated : " + storyId + " Milestones after update: ");
                QueryRequest milestoneCollectionRequest2 = new QueryRequest(storyJsonObject.getAsJsonObject("Milestones"));
                milestoneCollectionRequest2.setFetch(new Fetch("Name"));
                JsonArray milestonesAfterUpdate = restApi.query(milestoneCollectionRequest2).getResults();
                int numberOfMilestonesAfterUpdate = restApi.query(milestoneCollectionRequest2).getResults().size();
                for (int j=0;j<numberOfMilestonesAfterUpdate;j++){
                    System.out.println("Milestone Name: " + milestonesAfterUpdate.get(j).getAsJsonObject().get("Name"));
                }
            }
        } finally {
            restApi.close();
        }

    }
}