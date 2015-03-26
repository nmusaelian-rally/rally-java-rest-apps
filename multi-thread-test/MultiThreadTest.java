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

public class MultiThreadTest
{
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException
    {
        final String[] projectOids = {"29047521165","29047523050"};
        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123";
        String applicationName = "Nick multi thread example";
        final RallyRestApi restApi = new RallyRestApi(new URI(host),apiKey);
        restApi.setApplicationName(applicationName);


        class MyRunnable implements Runnable {
            private String oid;
            public MyRunnable(String oid) {
                this.oid = oid;
            }

            public void run() {
                try{
                    getRally(restApi, this.oid);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        Thread [] threads = new Thread[projectOids.length];
        for (int i = 0; i < threads.length; i++)
        {
            threads[i] = new Thread(new MyRunnable(projectOids[i]));
            threads[i].start();
            threads[i].join();
        }
        if (restApi != null) {
            restApi.close();
        }


    }
    private static void getRally(RallyRestApi rallyRestApi,String ref) throws Exception
    {
        try{
            String projectRef = "/project/" + ref;
            QueryRequest  releaseRequest = new QueryRequest("Release");
            releaseRequest.setFetch(new Fetch("ReleaseStartDate", "ReleaseDate"));
            releaseRequest.setScopedDown(false);
            releaseRequest.setScopedUp(false);
            releaseRequest.setProject(projectRef);
            releaseRequest.setQueryFilter(new QueryFilter("Name", "=", "R1"));
            QueryResponse releaseQueryResponse = rallyRestApi.query(releaseRequest);

            JsonObject releaseJsonObject = releaseQueryResponse.getResults().get(0).getAsJsonObject();

            String rsd = releaseJsonObject.get("ReleaseStartDate").getAsString();
            String rd = releaseJsonObject.get("ReleaseDate").getAsString();

            QueryRequest  iterationRequest = new QueryRequest("Iteration");
            iterationRequest.setFetch(new Fetch("Name","StartDate","EndDate"));
            iterationRequest.setScopedDown(false);
            iterationRequest.setScopedUp(false);
            iterationRequest.setProject(projectRef);
            iterationRequest.setQueryFilter(new QueryFilter("StartDate", ">=", rsd).and(new QueryFilter("EndDate", "<=", rd)));

            QueryResponse iterationQueryResponse = rallyRestApi.query(iterationRequest);
            int numberOfIterations = iterationQueryResponse.getTotalResultCount();
            System.out.println(numberOfIterations);
            if(numberOfIterations >0){
                for (int i=0;i<numberOfIterations;i++){
                    JsonObject iterationJsonObject = iterationQueryResponse.getResults().get(i).getAsJsonObject();
                    System.out.println(iterationJsonObject.get("Name") + " : " + iterationJsonObject.get("_ref"));
                }
            }
        }
        finally {
            System.out.println("done");
        }
    }
}