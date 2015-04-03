import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class GetLastRevisionOfIterations {
	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
		RallyRestApi restApi = null;
		try{
			String host = "https://rally1.rallydev.com";
                        String apiKey = "_abc123";
                        String applicationName = "Nick get-revs-of-iterations";
                        String workspaceRef = "/workspace/29047390143";
                        restApi = new RallyRestApi(new URI(host), apiKey);
                        restApi.setApplicationName(applicationName);
                        String date = getDate(-60);
                        
                        QueryRequest  request = new QueryRequest("Iteration");
                        request.setWorkspace(workspaceRef);
                        request.setQueryFilter(new QueryFilter("EndDate", ">", date));
                        request.setFetch(new Fetch("Name","ObjectID","StartDate","EndDate", "Project","RevisionHistory","Revisions"));
                        QueryResponse response = restApi.query(request);
                        int numberOfIterations = response.getTotalResultCount();
                        if(numberOfIterations >0)
                        {
                            for (int i=0;i<numberOfIterations;i++) {
                                    JsonObject iterationObject = response.getResults().get(i).getAsJsonObject();
                                System.out.println(iterationObject.get("Name") + " : " + iterationObject.get("ObjectID") + " in project " + iterationObject.get("Project").getAsJsonObject().get("Name"));
                                String start = iterationObject.get("StartDate").getAsString();
                                String end = iterationObject.get("EndDate").getAsString();
                                System.out.println("Start: " + start + " End: " + end);
                                JsonObject revisionHistory = iterationObject.get("RevisionHistory").getAsJsonObject();
                                QueryRequest revisionsRequest = new QueryRequest(revisionHistory.getAsJsonObject("Revisions"));
                                revisionsRequest.setFetch(new Fetch("CreationDate"));
                                QueryResponse revisionsResponse = restApi.query(revisionsRequest);
                                int numberOfRevisions = revisionsResponse.getTotalResultCount();
                                System.out.println("number of revisions of iteration: " + numberOfRevisions);
                                JsonObject revisionObject = revisionsResponse.getResults().get(numberOfRevisions-1).getAsJsonObject();
                                System.out.println("Iteration last updated: " + revisionObject.get("CreationDate"));
                                
                            }
                        }
                
		}
		finally{
			if (restApi != null) {
	            restApi.close();
	        }
		}
	}
	private static String getDate(int lookback)
    {
        Calendar cal = GregorianCalendar.getInstance();
        cal.add( Calendar.DAY_OF_YEAR, lookback);
        Date ninetyDaysAgoDate = cal.getTime();
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        return iso.format(ninetyDaysAgoDate);
    }
}