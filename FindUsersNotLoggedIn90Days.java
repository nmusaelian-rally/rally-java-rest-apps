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


public class FindUsersNotLoggedIn90Days {

    public static void main(String[] args) throws URISyntaxException, IOException {


        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123"; //admin
        String applicationName = "find users not logged in 90 days";

        RallyRestApi restApi = null;

        try{

            restApi = new RallyRestApi(new URI(host),apiKey);
            int x = -90;
            Calendar cal = GregorianCalendar.getInstance();
            cal.add( Calendar.DAY_OF_YEAR, x);
            Date ninetyDaysAgoDate = cal.getTime();

            String ninetyDaysAgoString = ninetyDaysAgoDate.toString();
            System.out.println(ninetyDaysAgoString);

            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");



            QueryRequest userRequest = new QueryRequest("User");
            userRequest.setFetch(new Fetch("UserName", "DisplayName", "Disabled", "LastLoginDate"));
            userRequest.setQueryFilter((new QueryFilter("LastLoginDate","=",null).or(new QueryFilter("LastLoginDate", "<", iso.format(ninetyDaysAgoDate)))).and(new QueryFilter("Disabled","=","false")));
            QueryResponse userQueryResponse = restApi.query(userRequest);
            int count = userQueryResponse.getResults().size();
            System.out.println(count);
            if(count >0){
                for (int i=0;i<count;i++){
                    JsonObject userJsonObject = userQueryResponse.getResults().get(i).getAsJsonObject();
                    System.out.println(userJsonObject.get("UserName") + " is currently disabled? " + userJsonObject.get("Disabled") + " Last Login Date: " +  userJsonObject.get("LastLoginDate"));
                    String userRef = userJsonObject.get("_ref").getAsString();
                    //CAUTION: THIS CODE BULK-DISABLES USERS !!!!!!!!!!!!!!!
                    //COMMENTED OUT
                   /* JsonObject userUpdate = new JsonObject();
                    userUpdate.addProperty("Disabled", true);
                    UpdateRequest updateUserDisableRequest = new UpdateRequest(userRef,userUpdate);
                    UpdateResponse updateResponse = restApi.update(updateUserDisableRequest);

                    if (updateResponse.wasSuccessful()) {
                        System.out.println("Successfully updated user: " + userJsonObject.get("UserName") +
                                " to: " + userUpdate.get("Disabled"));
                        System.out.println(String.format("Updated %s", updateResponse.getObject().get("_ref").getAsString()));
                    } else {
                        System.out.println("Error occurred attempting to update UserName: " + userJsonObject.get("UserName"));
                        String[] errorList;
                        errorList = updateResponse.getErrors();
                        for (int e=0;i<errorList.length;e++) {
                            System.out.println(errorList[e]);
                        }
                    }*/
                }
            }
        }catch(Exception e){
            System.out.println("Exception occurred");
            e.printStackTrace();
        }
        finally{
            restApi.close();
        }

    }
}