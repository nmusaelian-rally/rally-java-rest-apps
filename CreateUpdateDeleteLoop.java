import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.GetRequest;
import com.rallydev.rest.response.GetResponse;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import com.rallydev.rest.request.CreateRequest;
import com.rallydev.rest.response.CreateResponse;
import com.rallydev.rest.request.DeleteRequest;
import com.rallydev.rest.response.DeleteResponse;
import com.rallydev.rest.util.Ref;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.DateFormat;
import java.io.FileOutputStream;
import java.io.PrintStream;




public class CreateUpdateDeleteInLoop {
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {

        PrintStream pst = new PrintStream(new FileOutputStream("Log.txt", true));
        System.setOut(pst);
        System.setErr(pst);

        System.out.println("#########################" +  new Date() + "#########################");

        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123";

        String applicationName = "Nick multi thread create-update-delete loop";
        RallyRestApi restApi = new RallyRestApi(new URI(host), apiKey);
        restApi.setApplicationName(applicationName);
        restApi.setProxy(new URI("http://127.0.0.1:8080"));

        //get current user
        GetRequest getRequest = new GetRequest("/user");
        GetResponse getResponse = restApi.get(getRequest);
        JsonObject currentUser = getResponse.getObject();
        String currentUserName = currentUser.get("_refObjectName").getAsString();
        String currentUserRef = currentUser.get("_ref").getAsString();
        System.out.println("current user: " + currentUserName + currentUserRef);


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

        try {
            int counter = 3;
            while(counter > 0){
                System.out.println("LOOP: " + counter);
                DateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
                List<String> parentStories = new ArrayList<String>();
                List<String> features = new ArrayList<String>();
                String host = "https://rally1.rallydev.com";
                String apiKey = "_abc123";
                RallyRestApi restApi = new RallyRestApi(new URI(host), apiKey);
                restApi.setProxy(new URI("http://127.0.0.1:8080"));
                String workspaceRef = "/workspace/" + oid;

                QueryRequest projectRequest = new QueryRequest("Project");
                projectRequest.setFetch(new Fetch("Name"));
                projectRequest.setWorkspace(workspaceRef);

                QueryResponse projectQueryResponse = restApi.query(projectRequest);
                JsonObject projectJsonObject = projectQueryResponse.getResults().get(0).getAsJsonObject();
                String projectRef = projectJsonObject.get("_ref").getAsString();
                System.out.println(String.format("------ Creating items in project %s",projectJsonObject.get("Name").getAsString()));
                for (int x=0; x<1; x++) {
                    JsonObject newFeature = new JsonObject();
                    newFeature.addProperty("Name", "feature: " + iso.format(new Date()));
                    newFeature.addProperty("Project", projectRef);
                    CreateRequest createRequest = new CreateRequest("portfolioitem/feature", newFeature);
                    CreateResponse createResponse = restApi.create(createRequest);
                    if (createResponse.wasSuccessful()) {
                        System.out.println(String.format("Created %s", createResponse.getObject().get("FormattedID").getAsString()));
                        System.out.println(String.format(createResponse.getObject().get("_ref").getAsString()));
                        features.add(createResponse.getObject().get("_ref").getAsString());
                        //Read feature
                        String featureRef = Ref.getRelativeRef(createResponse.getObject().get("_ref").getAsString());
                        System.out.println(String.format("\nReading feature %s...",featureRef));
                        for (int y=0; y<2; y++){
                            JsonObject newStory = new JsonObject();
                            newStory.addProperty("Name", "story: " + iso.format(new Date()));
                            newStory.addProperty("Project", projectRef);
                            newStory.addProperty("PortfolioItem", featureRef);
                            CreateRequest createRequest2 = new CreateRequest("hierarchicalrequirement", newStory);
                            CreateResponse createResponse2 = restApi.create(createRequest2);
                            if (createResponse.wasSuccessful()) {
                                System.out.println(String.format("Created %s", createResponse2.getObject().get("FormattedID").getAsString()));
                                System.out.println(String.format(createResponse2.getObject().get("_ref").getAsString()));
                                parentStories.add(createResponse2.getObject().get("_ref").getAsString());
                                //Read story
                                String storyRef = Ref.getRelativeRef(createResponse2.getObject().get("_ref").getAsString());
                                System.out.println(String.format("\nReading child story %s...",storyRef));
                                System.out.println("Creating a child story...");
                                for (int z=0; z<2; z++){
                                    JsonObject newChildStory = new JsonObject();
                                    newChildStory.addProperty("Name", "story: " + iso.format(new Date()));
                                    newChildStory.addProperty("Project", projectRef);
                                    newChildStory.addProperty("Parent", storyRef);
                                    CreateRequest createRequest3 = new CreateRequest("hierarchicalrequirement", newChildStory);
                                    CreateResponse createResponse3 = restApi.create(createRequest3);
                                    if (createResponse.wasSuccessful()) {
                                        System.out.println(String.format("Created %s", createResponse3.getObject().get("FormattedID").getAsString()));
                                        System.out.println(String.format(createResponse3.getObject().get("_ref").getAsString()));
                                        //Read child story
                                        String childStoryRef = Ref.getRelativeRef(createResponse3.getObject().get("_ref").getAsString());
                                        System.out.println(String.format("\nReading child story %s...",childStoryRef));
                                    }
                                }
                            }
                        }
                    }
                }

                for (int s=0; s<parentStories.size();s++) {
                    DeleteRequest deleteStoriesRequest = new DeleteRequest(parentStories.get(s));
                    System.out.println("Deleting ...." + parentStories.get(s));
                    DeleteResponse deleteStoriesResponse = restApi.delete(deleteStoriesRequest);
                }

                for (int f=0; f<features.size();f++) {
                    DeleteRequest deleteFeaturesRequest = new DeleteRequest(features.get(f));
                    System.out.println("Deleting ...." + features.get(f));
                    DeleteResponse deleteFeaturesResponse = restApi.delete(deleteFeaturesRequest);
                }
                counter--;
            }
        }
        finally{
                System.out.println("done with thread " + oid + "\n-------------------------");
        }
    }
}