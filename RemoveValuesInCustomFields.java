import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import com.rallydev.rest.request.UpdateRequest;
import com.rallydev.rest.response.UpdateResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class RemoveValuesInCustomFields {

    public static void main(String[] args) throws URISyntaxException, IOException {

        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123"; 
        String applicationName = "Remove values of custom fields"; //Use your API Key or your username/password.
        //String workspaceRef = "/workspace/12352608219";     //Use your workspace and project ObjectIDs
        String projectRef = "/project/12352608219";
        RallyRestApi restApi = null;
        restApi = new RallyRestApi(new URI(host),apiKey);
        restApi.setApplicationName(applicationName);

        //limit query by LastUpdateDate:
        /*
        int x = -30;
        Calendar cal = GregorianCalendar.getInstance();
        cal.add( Calendar.DAY_OF_YEAR, x);
        Date nDaysAgoDate = cal.getTime();
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        */

        /**********************************FIND STORY***********************************************************/
        try{
            QueryRequest storyRequest = new QueryRequest("HierarchicalRequirement");
            storyRequest.setFetch(new Fetch("Name","FormattedID","AdditionalNotes","Approved"));
            //storyRequest.setWorkspace(workspaceRef);
            storyRequest.setLimit(1000);
            storyRequest.setProject(projectRef);
            storyRequest.setScopedDown(false);
            storyRequest.setScopedUp(false);
            //storyRequest.setQueryFilter((new QueryFilter("AdditionalNotes", "!=", "")).or(new QueryFilter("Approved", "=", "true"))); // Will not work: Text field cannot be queried by null or empty string
            //storyRequest.setQueryFilter(new QueryFilter("LastUpdateDate", ">", iso.format(nDaysAgoDate)));
            storyRequest.setQueryFilter((new QueryFilter("FormattedID", "=", "US2926")).or(new QueryFilter("FormattedID", "=", "US2935")));
            QueryResponse storyQueryResponse = restApi.query(storyRequest);
            for (int i=0; i<storyQueryResponse.getResults().size();i++){
                JsonObject storyJsonObject = storyQueryResponse.getResults().get(i).getAsJsonObject();
                String storyRef = storyJsonObject.get("_ref").getAsString();
                /*****************************************************UPDATE STORIES****************************************/
                JsonObject storyUpdate = new JsonObject();
                storyUpdate.addProperty("c_AdditionalNotes", ""); //removing values in AdditionalNotes
                storyUpdate.addProperty("c_Approved", false);    //set Approved to false
                UpdateRequest updateStoryRequest = new UpdateRequest(storyRef,storyUpdate);
                UpdateResponse updateResponse = restApi.update(updateStoryRequest);
                if (updateResponse.wasSuccessful()) {
                    System.out.println("Successfully updated c_AdditionalNotes: " + storyJsonObject.get("c_AdditionalNotes") + " to " + storyUpdate.get("c_AdditionalNotes")
                            + "\nSuccessfully updated c_Approved:" + storyJsonObject.get("c_Approved") +
                            " to: " + storyUpdate.get("c_Approved"));
                    String[] warningList;
                    warningList = updateResponse.getWarnings();
                    for (int x=0;x<warningList.length;x++) {
                        System.out.println(warningList[x]);
                    }
                } else {
                    System.out.println("Error occurred attempting to update story: " + storyJsonObject.get("Name"));
                    String[] errorList;
                    errorList = updateResponse.getErrors();
                    for (int y=0;y<errorList.length;y++) {
                        System.out.println(errorList[y]);
                    }
                }
            }


        }catch(Exception e){
            System.out.println("Exception occurred....");
            e.printStackTrace();
        }
        finally{
            //Release all resources
            restApi.close();
        }
    }
}