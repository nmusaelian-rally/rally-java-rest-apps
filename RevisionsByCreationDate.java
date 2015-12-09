import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class RevisionsByCreationDate {

    private static final String SERVER_URL = "https://rally1.rallydev.com";
    private static final String API_KEY = "_abc123";
    private static final String WORKSPACE_REF = "/workspace/12352608219";

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {

        RallyRestApi restApi = new RallyRestApi(new URI(SERVER_URL), API_KEY);
        restApi.setApplicationName("Nick:RevisionsByCreationDate");


        int x = -5;
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.MINUTE, x);
        Date interval = cal.getTime();

        System.out.println(interval);

        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");

        try {
            QueryRequest storyRequest = new QueryRequest("HierarchicalRequirement");
            storyRequest.setFetch(new Fetch("FormattedID", "RevisionHistory", "Revisions"));
            storyRequest.setWorkspace(WORKSPACE_REF);
            storyRequest.setQueryFilter(new QueryFilter("LastUpdateDate", ">", iso.format(interval)));
            storyRequest.setLimit(10000);
            QueryResponse storyQueryResponse = restApi.query(storyRequest);
            int resultCount = storyQueryResponse.getTotalResultCount();
            if(resultCount == 0){
                System.out.println("No results");
            }
            else{
                for (int i=0; i<resultCount;i++) {
                    JsonObject storyJsonObject = storyQueryResponse.getResults().get(i).getAsJsonObject();
                    String storyRef = storyJsonObject.get("_ref").getAsString();
                    System.out.println("FormattedID: " + storyJsonObject.get("FormattedID"));
                    JsonObject revHistoryObject = storyJsonObject.get("RevisionHistory").getAsJsonObject();
                    String revHistoryRef = revHistoryObject.get("_ref").getAsString();
                    System.out.println("RevisionHistory: " + revHistoryRef);
                    JsonObject revisionsCollection = revHistoryObject.get("Revisions").getAsJsonObject();
                    QueryRequest revisionsQuery = new QueryRequest(revisionsCollection);
                    revisionsQuery.setFetch(new Fetch("CreationDate", "Description"));
                    revisionsQuery.setOrder("CreationDate ASC");
                    revisionsQuery.setQueryFilter(new QueryFilter("CreationDate", ">=", iso.format(interval)));
                    revisionsQuery.setLimit(10000);
                    JsonArray revisionsOfStory = restApi.query(revisionsQuery).getResults();
                    for (int j = 0; j < revisionsOfStory.size(); j++) {
                        System.out.println(revisionsOfStory.get(j).getAsJsonObject().get("Description").getAsString());
                    }
                }
            }

        } finally {
            restApi.close();
        }
    }

}

