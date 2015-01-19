/*
WARNING: THIS CODE EXAMPLE DELETES DATA, RUN IT AT YOUR OWN RISK
MAKE SURE THAT YOUR QUERY RETURNS TESTCASES YOU WANT TO DELETE
THE CODE IS FOR DEMO PURPOSES ONLY AND IS NOT SUPPORTED BY RALLY
*/

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.request.DeleteRequest;
import com.rallydev.rest.response.DeleteResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import java.net.URI;


public class GetTestCasesByTagAndBulkDelete {

    public static void main(String[] args) throws Exception {

        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123"; //use your api key
        String applicationName = "Find TestCases by Tag and bulk delete";
        String workspaceRef = "/workspace/12352608129";

        RallyRestApi restApi = null;
        try {
            restApi = new RallyRestApi(new URI(host),apiKey);
            restApi.setApplicationName(applicationName);

            QueryRequest request = new QueryRequest("TestCase");
            request.setWorkspace(workspaceRef);

            request.setFetch(new Fetch(new String[] {"Name", "FormattedID", "Tags"}));
            request.setLimit(1000);
            request.setScopedDown(false);
            request.setScopedUp(false);

            request.setQueryFilter(new QueryFilter("Tags.Name", "contains", "\"tag1\""));

            QueryResponse response = restApi.query(request);
            System.out.println("Successful: " + response.wasSuccessful());
            System.out.println("Results Size: " + response.getResults().size());

            for (int i=0; i<response.getResults().size();i++){
                JsonObject tcJsonObject = response.getResults().get(i).getAsJsonObject();
                System.out.println("Name: " + tcJsonObject.get("Name") + " FormattedID: " + tcJsonObject.get("FormattedID"));
                int numberOfTags = tcJsonObject.getAsJsonObject("Tags").get("Count").getAsInt();
                QueryRequest tagRequest = new QueryRequest(tcJsonObject.getAsJsonObject("Tags"));
                tagRequest.setFetch(new Fetch("Name","FormattedID"));
                //load the collection
                JsonArray tags = restApi.query(tagRequest).getResults();
                for (int j=0;j<numberOfTags;j++){
                    System.out.println("Tag Name: " + tags.get(j).getAsJsonObject().get("Name"));
                }
                System.out.println("deleting " + tcJsonObject.get("FormattedID")) ;
                DeleteRequest deleteRequest = new DeleteRequest(tcJsonObject.get("_ref").getAsString());
                DeleteResponse deleteResponse = restApi.delete(deleteRequest);
            }


        } finally {
            if (restApi != null) {
                restApi.close();
            }
        }
    }
}