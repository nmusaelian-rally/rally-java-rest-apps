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

public class UpdateTestSetsOnTestCase {

    public static void main(String[] args) throws URISyntaxException, IOException {


        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123";
        String workspaceRef = "/workspace/\uFEFF12352608129";
        String applicationName = "RestExample_updateTestSetsOnTC";


        RallyRestApi restApi = new RallyRestApi(new URI(host),apiKey);
        restApi.setApplicationName(applicationName);

        try {
            String setID = "TS24";
            String testid = "TC3";
            QueryRequest  tsRequest = new QueryRequest("TestSet");
            tsRequest.setWorkspace(workspaceRef);
            tsRequest.setQueryFilter(new QueryFilter("FormattedID", "=", setID));
            QueryResponse tsQueryResponse = restApi.query(tsRequest);
            if(tsQueryResponse.getTotalResultCount() == 0){
                System.out.println("Cannot find tag: " + setID);
                return;
            }
            JsonObject tsJsonObject = tsQueryResponse.getResults().get(0).getAsJsonObject();
            String tsRef = tsJsonObject.get("_ref").getAsString();
            System.out.println(tsRef);

            QueryRequest testCaseRequest = new QueryRequest("TestCase");
            testCaseRequest.setWorkspace(workspaceRef);
            testCaseRequest.setFetch(new Fetch("FormattedID", "Name", "TestSets"));
            testCaseRequest.setQueryFilter(new QueryFilter("FormattedID", "=",  testid));
            QueryResponse testCaseQueryResponse = restApi.query(testCaseRequest);;

            if (testCaseQueryResponse.getTotalResultCount() == 0) {
                System.out.println("Cannot find test case : " + testid);
                return;
            }
            JsonObject testCaseJsonObject = testCaseQueryResponse.getResults().get(0).getAsJsonObject();
            String testCaseRef = testCaseJsonObject.get("_ref").getAsString();
            System.out.println(testCaseRef);

            int numberOfTestSets = testCaseJsonObject.getAsJsonObject("TestSets").get("Count").getAsInt();
            System.out.println(numberOfTestSets + " testset(s) on " + testid);
            QueryRequest testsetCollectionRequest = new QueryRequest(testCaseJsonObject.getAsJsonObject("TestSets"));
            testsetCollectionRequest.setFetch(new Fetch("FormattedID"));
            JsonArray testsets = restApi.query(testsetCollectionRequest).getResults();

            for (int j=0;j<numberOfTestSets;j++){
                System.out.println("FormattedID: " + testsets.get(j).getAsJsonObject().get("FormattedID"));
            }
            testsets.add(tsJsonObject);
            JsonObject testCaseUpdate = new JsonObject();
            testCaseUpdate.add("TestSets", testsets);
            UpdateRequest updateTestCaseRequest = new UpdateRequest(testCaseRef,testCaseUpdate);
            UpdateResponse updateTestCaseResponse = restApi.update(updateTestCaseRequest);
            if (updateTestCaseResponse.wasSuccessful()) {
                QueryRequest testsetCollectionRequest2 = new QueryRequest(testCaseJsonObject.getAsJsonObject("TestSets"));
                testsetCollectionRequest2.setFetch(new Fetch("FormattedID"));
                JsonArray testsetsAfterUpdate = restApi.query(testsetCollectionRequest2).getResults();
                int numberOfTestSetsAfterUpdate = restApi.query(testsetCollectionRequest2).getResults().size();
                System.out.println("Successfully updated : " + testid + " TestSets after update: " + numberOfTestSetsAfterUpdate);
                for (int j=0;j<numberOfTestSetsAfterUpdate;j++){
                    System.out.println("FormattedID: " + testsetsAfterUpdate.get(j).getAsJsonObject().get("FormattedID"));
                }
            }
        } finally {
            restApi.close();
        }

    }
}