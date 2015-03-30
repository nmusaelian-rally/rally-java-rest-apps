import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.GetRequest;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.GetResponse;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;


public class GetTasksOfStories {
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {

        //start main thread
        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123";
        String applicationName = "Nick:HUtest";
        RallyRestApi restApi = new RallyRestApi(new URI(host), apiKey);
        restApi.setApplicationName(applicationName);

        //get current user
        GetRequest getRequest = new GetRequest("/user");
        GetResponse getResponse = restApi.get(getRequest);
        JsonObject currentUser = getResponse.getObject();
        String currentUserRef = currentUser.get("_ref").getAsString();
        System.out.println("current user " + currentUserRef);


        //get workspaces where the current user has permission
        List<String> workspaces = new ArrayList<String>();
        QueryRequest workspacePermissionsRequest = new QueryRequest("WorkspacePermissions");
        workspacePermissionsRequest.setQueryFilter(new QueryFilter("User", "=", currentUserRef));
        workspacePermissionsRequest.setFetch(new Fetch("Workspace","ObjectID"));

        QueryResponse workspacePermissionsResponse = restApi.query(workspacePermissionsRequest);
        int numberOfPermissoins = workspacePermissionsResponse.getTotalResultCount();
        System.out.println(numberOfPermissoins);
        if (numberOfPermissoins > 0) {
            for (int i = 0; i < numberOfPermissoins; i++) {
                JsonObject workspacePermissionObject = workspacePermissionsResponse.getResults().get(i).getAsJsonObject();
                String workspaceRef = workspacePermissionObject.get("Workspace").getAsJsonObject().get("ObjectID").getAsString();
                workspaces.add(workspaceRef);
            }
        }
        System.out.println("workspaces " + workspaces);


        class MyRunnable implements Runnable {
            private String oid;

            public MyRunnable(String oid) {
                this.oid = oid;

            }

            public void run() {
                try{
                    getRally(this.oid);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        Thread [] threads = new Thread[numberOfPermissoins];
        for (int i = 0; i < threads.length; i++)
        {
            threads[i] = new Thread(new MyRunnable(workspaces.get(i)));
            threads[i].start();
            threads[i].join();
        }
        if (restApi != null) {
            restApi.close();
        }


    }

    private static void getRally(String oid) throws Exception
    {
        try{
            //start sub threads
            String host = "https://rally1.rallydev.com";
            String apiKey = "_WrzFG5niQoOQ97Jj6GAxKYfuISNlELjmKeNTsRYgA";
            String applicationName = "Nick:HUtest:" + oid;
            RallyRestApi restApi = new RallyRestApi(new URI(host), apiKey);
            restApi.setApplicationName(applicationName);
            String workspaceRef = "/workspace/" + oid;
            QueryRequest storyRequest = new QueryRequest("HierarchicalRequirement");
            storyRequest.setFetch(new Fetch(new String[] {"Name", "FormattedID","Tasks"}));
            storyRequest.setWorkspace(workspaceRef);
            storyRequest.setLimit(10000);
            storyRequest.setPageSize(200);
            QueryResponse storyQueryResponse = restApi.query(storyRequest);
            System.out.println("Stories - TotalResultCount: " + storyQueryResponse.getTotalResultCount());
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
            System.out.println("Number of tasks of all stories: " + totalTasks);
        }
        finally {
            System.out.println("done with thread " + oid + "\n--------------------------------------------------------");
        }
    }
}