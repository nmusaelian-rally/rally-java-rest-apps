import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class QueryStoryRevisions {

    public static void main(String[] args) throws URISyntaxException, IOException {
        String rallyURL = new String("https://rally1.rallydev.com");
        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123"; 
        String applicationName = new String("QueryRevisions");

        String projectRef = "/project/14018981229";     

        RallyRestApi restApi = new RallyRestApi(new URI(host),apiKey);

        restApi.setApplicationName(applicationName);

        QueryRequest storyRequest = new QueryRequest("HierarchicalRequirement");
        storyRequest.setFetch(new Fetch("FormattedID","Name","Description", "RevisionHistory", "Revisions"));
        storyRequest.setQueryFilter(new QueryFilter("FormattedID", "=", "US24818"));
        storyRequest.setProject(projectRef);
        QueryResponse storyQueryResponse = restApi.query(storyRequest);
        JsonObject storyJsonObject = storyQueryResponse.getResults().get(0).getAsJsonObject();

        JsonObject revHistoryObject = storyJsonObject.get("RevisionHistory").getAsJsonObject();
        JsonObject revisionsCollection = revHistoryObject.get("Revisions").getAsJsonObject();

        QueryRequest revisionsQuery = new QueryRequest(revisionsCollection);
        revisionsQuery.setFetch(new Fetch("CreationDate", "RevisionNumber", "Description"));
        revisionsQuery.setOrder("CreationDate ASC");

        JsonArray revisionsOfStory = restApi.query(revisionsQuery).getResults();

        for (int i=0; i<revisionsOfStory.size(); i++) {
            System.out.println("Name: " + revisionsOfStory.get(i).getAsJsonObject().get("RevisionNumber") + "; " + revisionsOfStory.get(i).getAsJsonObject().get("Description").getAsString());
        }

        restApi.close();
    }
}