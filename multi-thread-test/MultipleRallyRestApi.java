import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.GetRequest;
import com.rallydev.rest.response.GetResponse;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.request.UpdateRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.response.UpdateResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

//start main tread, get all workspaces
//spin multiple treads, pass a workspace to each

public class MultipleRallyRestApi {
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {

        //start main thread
        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123";
        String applicationName = "Nick multi thread example";
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

            public MyRunnable(int number, String oid) {
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
            threads[i] = new Thread(new MyRunnable(i, workspaces.get(i)));
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
            String apiKey = "_abc123";
            String applicationName = "Nick multi thread example " + oid;
            RallyRestApi restApi = new RallyRestApi(new URI(host), apiKey);
            restApi.setApplicationName(applicationName);
            String workspaceRef = "/workspace/" + oid;
            QueryRequest  releaseRequest = new QueryRequest("Release");
            releaseRequest.setFetch(new Fetch("Name","ObjectID","ReleaseStartDate","ReleaseDate", "Project","RevisionHistory","Revisions"));
            String date = getDate();
            releaseRequest.setQueryFilter(new QueryFilter("ReleaseDate", ">", date));
            releaseRequest.setWorkspace(workspaceRef);
            QueryResponse releaseQueryResponse = restApi.query(releaseRequest);
            int numberOfReleases = releaseQueryResponse.getTotalResultCount();
            System.out.println("number of releases in workspace : " + oid + " with ReleaseDate > " + date + ":"+ numberOfReleases);
            if(numberOfReleases >0)
            {
                for (int i=0;i<numberOfReleases;i++) {
                    JsonObject releaseJsonObject = releaseQueryResponse.getResults().get(i).getAsJsonObject();
                    System.out.println(releaseJsonObject.get("Name") + " : " + releaseJsonObject.get("ObjectID") + " in project " + releaseJsonObject.get("Project").getAsJsonObject().get("Name"));
                    String rsd = releaseJsonObject.get("ReleaseStartDate").getAsString();
                    String rd = releaseJsonObject.get("ReleaseDate").getAsString();

                    JsonObject revisionHistory = releaseJsonObject.get("RevisionHistory").getAsJsonObject();
                    QueryRequest revisionsRequest = new QueryRequest(revisionHistory.getAsJsonObject("Revisions"));
                    revisionsRequest.setFetch(new Fetch("Description","User","UserName"));
                    revisionsRequest.setQueryFilter(new QueryFilter("Description", "contains", "Unscheduled"));
                    QueryResponse revisionsResponse = restApi.query(revisionsRequest);
                    int numberOfRevisions = revisionsResponse.getTotalResultCount();
                    if (numberOfRevisions > 0) {
                        System.out.println("number of revisions when work items were removed from release: " + numberOfRevisions);
                        for (int x = 0; x < numberOfRevisions; x++) {
                            JsonObject revisionJsonObject = revisionsResponse.getResults().get(x).getAsJsonObject();
                            System.out.println(revisionJsonObject.get("Description") + "\n by:" + revisionJsonObject.get("User").getAsJsonObject().get("UserName"));
                        }
                    }
                    else{
                        System.out.println("no work items were removed from release");
                    }

                    QueryRequest iterationRequest = new QueryRequest("Iteration");
                    iterationRequest.setFetch(new Fetch("Name","ObjectID","StartDate","EndDate","Project","RevisionHistory"));
                    iterationRequest.setWorkspace(workspaceRef);
                    iterationRequest.setQueryFilter(new QueryFilter("StartDate", ">=", rsd).and(new QueryFilter("EndDate", "<=", rd)));

                    QueryResponse iterationQueryResponse = restApi.query(iterationRequest);
                    int numberOfIterations = iterationQueryResponse.getTotalResultCount();
                    System.out.println("number of iterations within release " + releaseJsonObject.get("ObjectID") + ": " + numberOfIterations);
                    if (numberOfIterations > 0) {
                        for (int j = 0; j < numberOfIterations; j++) {
                            JsonObject iterationJsonObject = iterationQueryResponse.getResults().get(j).getAsJsonObject();
                            System.out.println(iterationJsonObject.get("Name") + " : " + iterationJsonObject.get("ObjectID") + " in project " + iterationJsonObject.get("Project").getAsJsonObject().get("Name"));                      }
                        }
                    }
                }
            }

        }
        finally {
            System.out.println("done with thread " + oid + "\n--------------------------------------------------------");
        }
    }

    private static String getDate()
    {
        int x = -90;
        Calendar cal = GregorianCalendar.getInstance();
        cal.add( Calendar.DAY_OF_YEAR, x);
        Date ninetyDaysAgoDate = cal.getTime();

        String ninetyDaysAgoString = ninetyDaysAgoDate.toString();

        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        return iso.format(ninetyDaysAgoDate);
    }
}