import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import com.google.gson.JsonArray;
import java.util.List;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.request.GetRequest;
import com.rallydev.rest.response.GetResponse;
import com.rallydev.rest.util.Fetch;




public class GetTotalCountOfStoriesInSub {
    public static int totalNumOfStories = 0;

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {

        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123"; 
        String applicationName = "Nick:get count of all stories in a sub";
        RallyRestApi restApi = new RallyRestApi(new URI(host), apiKey);
        restApi.setApplicationName(applicationName);


        try {

            GetRequest getRequest = new GetRequest("/user");
            GetResponse getResponse = restApi.get(getRequest);
            JsonObject currentUser = getResponse.getObject();
            String currentUserRef = currentUser.get("_ref").getAsString();
            String currentUserName = currentUser.get("_refObjectName").getAsString();
            System.out.println("current user " + " " + currentUserName + " " + currentUserRef);

            List<String> workspaces = new ArrayList<String>();
            GetRequest getSubscriptionRequest = new GetRequest("/subscription");
            getSubscriptionRequest.setFetch(new Fetch("Workspaces"));
            GetResponse getSubscriptionResponse = restApi.get(getSubscriptionRequest);
            JsonObject subJsonObject = getSubscriptionResponse.getObject();
            String subRef = subJsonObject.get("_ref").getAsString();
            System.out.println("subscription " + " " + subRef);
            int numberOfWorkspaces = subJsonObject.getAsJsonObject("Workspaces").get("Count").getAsInt();
            System.out.println("number of workspaces in the sub " + " " + numberOfWorkspaces);
            if(numberOfWorkspaces > 0) {
                QueryRequest workspaceRequest = new QueryRequest(subJsonObject.getAsJsonObject("Workspaces"));
                workspaceRequest.setFetch(new Fetch("Name","ObjectID","State"));
                workspaceRequest.setLimit(1000000);
                JsonArray workspacesInSub = restApi.query(workspaceRequest).getResults();

                for (int i=0;i<numberOfWorkspaces;i++){
                    System.out.println("Name: " + workspacesInSub.get(i).getAsJsonObject().get("Name")
                            + " OID: " + workspacesInSub.get(i).getAsJsonObject().get("ObjectID").getAsString()
                            + " State:" + workspacesInSub.get(i).getAsJsonObject().get("State").getAsString());

                    if(workspacesInSub.get(i).getAsJsonObject().get("State").getAsString().equals("Open") ){
                        workspaces.add(workspacesInSub.get(i).getAsJsonObject().get("ObjectID").getAsString());
                    }

                }
            }

            System.out.println("workspaces: " + workspaces);

            class MyRunnable implements Runnable {
                private String oid;
                private String state;

                public MyRunnable(String oid) {
                    this.oid = oid;

                }

                public void run() {
                    try{
                        int count = getRally(this.oid);
                        totalNumOfStories = totalNumOfStories + count;
                        System.out.println("Running total number of stories in Subscription: " + totalNumOfStories);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            Thread [] threads = new Thread[numberOfWorkspaces];
            for (int i = 0; i < threads.length; i++)
            {
                threads[i] = new Thread(new MyRunnable(workspaces.get(i)));
                threads[i].start();
                threads[i].join();
            }
            if (restApi != null) {
                restApi.close();
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            restApi.close();
        }
    }
    private static int getRally(String oid) throws Exception
    {
        int numberOfStories = 0;
        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123";
        String applicationName = "Nick:get count of all stories and defects in a sub";
        RallyRestApi restApi = new RallyRestApi(new URI(host), apiKey);
        restApi.setApplicationName(applicationName);
        try {

            String workspaceRef = "/workspace/" + oid;
            QueryRequest  storyRequest = new QueryRequest("HierarchicalRequirement");
            storyRequest.setWorkspace(workspaceRef);
            storyRequest.setLimit(1000000);
            QueryResponse storyResponse = restApi.query(storyRequest);
            numberOfStories = storyResponse.getTotalResultCount();
            System.out.println("\n########## Workspace : " + oid + " ###########");
            System.out.println("number of stories in workspace : " + oid +  " : "+ numberOfStories);


        } catch (Exception e) {
            System.out.println(e);
        } finally {
            restApi.close();
        }
        return numberOfStories;
    }

}