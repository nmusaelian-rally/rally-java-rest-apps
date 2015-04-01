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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class GetRevisionsInWorkspaces {
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
        try{
            //start sub threads
            String host = "https://rally1.rallydev.com";
            String apiKey = "_abc123";
            String applicationName = "Nick multi thread example " + oid;
            RallyRestApi restApi = new RallyRestApi(new URI(host), apiKey);
            restApi.setApplicationName(applicationName);
            String workspaceRef = "/workspace/" + oid;
            String date = getDate();
            QueryRequest  request = new QueryRequest("Revision");
            request.setWorkspace(workspaceRef);
            request.setQueryFilter(new QueryFilter("CreationDate", ">", date));
            request.setFetch(new Fetch("CreationDate", "ObjectID", "Subscription", "Workspace", "Description", "RevisionHistory", "RevisionNumber", "User"));
            int limit = 1000000;
            request.setLimit(limit);
            QueryResponse response = restApi.query(request);
            int count = response.getTotalResultCount();
            System.out.println("result count: " + count);
            if(count > 0){
                for (int i=0;i<count;i++){
                    JsonObject revisionObject = response.getResults().get(i).getAsJsonObject();
                    System.out.println(revisionObject.get("CreationDate") + "\n" +
                            revisionObject.get("CreationDate") + "\n" +
                            revisionObject.get("ObjectID")+ "\n" +
                            "Subscription: " + revisionObject.get("Subscription").getAsJsonObject().get("ObjectID")+ "\n" +
                            "Workspace: " +    revisionObject.get("Workspace").getAsJsonObject().get("ObjectID")+ "\n" +
                            revisionObject.get("RevisionHistory").getAsJsonObject().get("_ref") + "\n" +
                            "RevisionNumber: " + revisionObject.get("RevisionNumber")+ "\n" +
                            "User: " + revisionObject.get("User").getAsJsonObject().get("_refObjectName"));
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