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


public class CreateMilestones {
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {

        //start main thread
        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123";
        String applicationName = "Nick:createMilestones";
        RallyRestApi restApi = new RallyRestApi(new URI(host), apiKey);
        restApi.setApplicationName(applicationName);
        String workspaceRef = "/workspace/1234"; 
        List<String> projects = new ArrayList<String>();
        QueryRequest projectsRequest = new QueryRequest("Project");
        projectsRequest.setWorkspace(workspaceRef);
        //Workspace cannot be used in queries, this will not work:
        //projectsRequest.setQueryFilter(new QueryFilter("Workspace", "=", workspaceRef));
        projectsRequest.setFetch(new Fetch("ObjectID", "Children"));

        QueryResponse projectsResponse = restApi.query(projectsRequest);
        int numberOfProjects = projectsResponse.getTotalResultCount();
        System.out.println(numberOfProjects);
        if (numberOfProjects > 0) {
            for (int i = 0; i < numberOfProjects; i++) {
                JsonObject projectObject = projectsResponse.getResults().get(i).getAsJsonObject();

                if(projectObject.getAsJsonObject("Children").getAsJsonObject().get("Count").getAsInt() > 0){
                    String projectRef = projectObject.get("ObjectID").getAsString();
                    projects.add(projectRef);
                }

            }
        }
        System.out.println("projects " + projects);
        int numberOfParnetProjects = projects.size();

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
        Thread [] threads = new Thread[numberOfParnetProjects];
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
            String applicationName = "Nick:createMilestones " + oid;
            RallyRestApi restApi = new RallyRestApi(new URI(host), apiKey);
            restApi.setApplicationName(applicationName);
            String workspaceRef = "/workspace/1234"; 
            String projectRef = "/project/" + oid;
            String name = "test milestone " + oid;
            JsonObject milestone = new JsonObject();
            milestone.addProperty("Name", name);
            milestone.addProperty("TargetProject", projectRef);
            milestone.addProperty("TargetDate", getDate());
            CreateRequest createRequest = new CreateRequest("milestone", milestone);
            createRequest.addParam("workspace", workspaceRef);
            CreateResponse createResponse = restApi.create(createRequest);
            if (createResponse.wasSuccessful()) {
                String milestoneRef = Ref.getRelativeRef(createResponse.getObject().get("_ref").getAsString());
                System.out.println("Milestone..." + milestoneRef);
            }
            else{
                String[] createErrors;
                createErrors = createResponse.getErrors();
                System.out.println("Error occurred creating a milestone: ");
                for (int i=0; i<createErrors.length;i++) {
                    System.out.println(createErrors[i]);
                }
            }
        }
        finally {
            System.out.println("done with thread " + oid + "\n--------------------------------------------------------");
        }
    }

    private static String getDate()
    {
        int x = 30;
        Calendar cal = GregorianCalendar.getInstance();
        cal.add( Calendar.DAY_OF_YEAR, x);
        Date fewDaysForward = cal.getTime();
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        return iso.format(fewDaysForward);
    }
}