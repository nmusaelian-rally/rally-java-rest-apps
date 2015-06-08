import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import java.net.URI;


public class GetTasksOfStories {

    public static void main(String[] args) throws Exception {

        String host = "https://rally1.rallydev.com";
        String applicationName = "Example: get Tasks of Stories";
        String projectRef = "/project/14018981229";     
        String apiKey = "_abc123"; 
        RallyRestApi restApi = null;
        try {
            restApi = new RallyRestApi(new URI(host),apiKey);
            restApi.setApplicationName(applicationName);
            QueryRequest storyRequest = new QueryRequest("HierarchicalRequirement");
            storyRequest.setProject(projectRef);

            storyRequest.setFetch(new Fetch(new String[] {"Name", "FormattedID","Tasks"}));
	    storyRequest.setQueryFilter((new QueryFilter("LastUpdateDate", ">", "\"2014-01-01\"")).and(new QueryFilter("ScheduleState", "<", "Completed")));
            storyRequest.setLimit(25000);
            storyRequest.setScopedDown(false);
            storyRequest.setScopedUp(false);

            QueryResponse storyQueryResponse = restApi.query(storyRequest);
            System.out.println("Successful: " + storyQueryResponse.wasSuccessful());
            System.out.println("Size: " + storyQueryResponse.getTotalResultCount());
            int totalTasks = 0;
            for (int i=0; i<storyQueryResponse.getResults().size();i++){
                JsonObject storyJsonObject = storyQueryResponse.getResults().get(i).getAsJsonObject();
                System.out.println("Name: " + storyJsonObject.get("Name") + " FormattedID: " + storyJsonObject.get("FormattedID"));
                int numberOfTasks = storyJsonObject.getAsJsonObject("Tasks").get("Count").getAsInt();

                if(numberOfTasks > 0) {
                    totalTasks += numberOfTasks;
                    QueryRequest taskRequest = new QueryRequest(storyJsonObject.getAsJsonObject("Tasks"));
                    taskRequest.setFetch(new Fetch("Name","FormattedID"));
                    //load the collection
                    JsonArray tasks = restApi.query(taskRequest).getResults();
                    for (int j=0;j<numberOfTasks;j++){
                        System.out.println("Name: " + tasks.get(j).getAsJsonObject().get("Name") + tasks.get(j).getAsJsonObject().get("FormattedID").getAsString());
                    }
                }
            }
            System.out.println("Total number of tasks: " + totalTasks);
        } finally {
            if (restApi != null) {
                restApi.close();
            }
        }
    }
}