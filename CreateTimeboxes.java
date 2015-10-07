import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.Ref;
import com.rallydev.rest.request.CreateRequest;
import com.rallydev.rest.response.CreateResponse;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class CreateTimeboxes {
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {

        //start main thread
        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123";
        String applicationName = "Nick:createTimeboxes";
        RallyRestApi restApi = new RallyRestApi(new URI(host), apiKey);
        restApi.setApplicationName(applicationName);
        String workspaceRef = "/workspace/1234"; 
        List<String> projects = new ArrayList<String>();
        QueryRequest projectsRequest = new QueryRequest("Project");
        projectsRequest.setWorkspace(workspaceRef);
        //Workspace cannot be used in queries, this will not wor:
        //projectsRequest.setQueryFilter(new QueryFilter("Workspace", "=", workspaceRef));
        projectsRequest.setFetch(new Fetch("ObjectID"));

        QueryResponse projectsResponse = restApi.query(projectsRequest);
        int numberOfProjects = projectsResponse.getTotalResultCount();
        System.out.println(numberOfProjects);
        if (numberOfProjects > 0) {
            for (int i = 0; i < numberOfProjects; i++) {
                JsonObject projectObject = projectsResponse.getResults().get(i).getAsJsonObject();
                String projectRef = projectObject.get("ObjectID").getAsString();
                projects.add(projectRef);
            }
        }
        System.out.println("projects " + projects);


        class MyRunnable implements Runnable {
            private String oid;

            public MyRunnable(String oid) {
                this.oid = oid;

            }

            public void run() {
                try{
                    createReleases(this.oid);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        Thread [] threads = new Thread[numberOfProjects];
        for (int i = 0; i < threads.length; i++)
        {
            threads[i] = new Thread(new MyRunnable(projects.get(i)));
            threads[i].start();
            threads[i].join();
        }
        if (restApi != null) {
            restApi.close();
        }


    }

    private static void createReleases(String oid) throws Exception
    {
        try{
            //start sub threads
            String host = "https://rally1.rallydev.com";
            String apiKey = "_abc123";
            String applicationName = "Nick:createTimeboxes " + oid;
            RallyRestApi restApi = new RallyRestApi(new URI(host), apiKey);
            restApi.setApplicationName(applicationName);
            String projectRef = "/project/" + oid;
            JsonObject release = new JsonObject();
            release.addProperty("Name", "test-release-2");
            release.addProperty("Project", projectRef);
            release.addProperty("ReleaseStartDate", getReleaseStartDate());
            release.addProperty("ReleaseDate", getReleaseDate());
            release.addProperty("State","Planning");
            CreateRequest createRequest = new CreateRequest("release", release);
            CreateResponse createResponse = restApi.create(createRequest);
            if (createResponse.wasSuccessful()) {
                String releaseRef = Ref.getRelativeRef(createResponse.getObject().get("_ref").getAsString());
                System.out.println("Release..." + releaseRef);
            }

        }
        finally {
            System.out.println("done with thread " + oid + "\n--------------------------------------------------------");
        }
    }
    private static String getReleaseStartDate()
    {
        Calendar cal = GregorianCalendar.getInstance();
        Date now = cal.getTime();
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        return iso.format(now);
    }

    private static String getReleaseDate()
    {
        int x = 14;
        Calendar cal = GregorianCalendar.getInstance();
        cal.add( Calendar.DAY_OF_YEAR, x);
        Date fewDaysForward = cal.getTime();
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        return iso.format(fewDaysForward);
    }
}