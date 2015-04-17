import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.QueryFilter;
import com.rallydev.rest.util.Fetch;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class FindDeleted {

    public static void main(String[] args) throws URISyntaxException, IOException {


        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123"; 
        String workspaceRef = "/workspace/12352608129";   //use your OID
        String applicationName = "Query recycle bin";

        RallyRestApi restApi = null;

        int x = -30;
        Calendar cal = GregorianCalendar.getInstance();
        cal.add( Calendar.DAY_OF_YEAR, x);
        Date nDaysAgoDate = cal.getTime();

        System.out.println(nDaysAgoDate);

        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");

        try {
            restApi = new RallyRestApi(new URI(host), apiKey);
            restApi.setApplicationName(applicationName);

            QueryRequest  recyclebinRequest = new QueryRequest("RecycleBinEntry");
            recyclebinRequest.setFetch(new Fetch("ID","Name","DeletedBy","DeletionDate","Type"));
            recyclebinRequest.setLimit(10);
            recyclebinRequest.setScopedDown(true);
            recyclebinRequest.setScopedUp(false);
            recyclebinRequest.setWorkspace(workspaceRef);
            recyclebinRequest.setQueryFilter(new QueryFilter("DeletionDate", ">", iso.format(nDaysAgoDate)));

            QueryResponse recyclebinQueryResponse = restApi.query(recyclebinRequest);
            int resultCount = recyclebinQueryResponse.getTotalResultCount();
            System.out.println(resultCount);

            if(resultCount >0){
                for (int i=0;i<resultCount;i++){
                    JsonObject recyclebinJsonObject = recyclebinQueryResponse.getResults().get(i).getAsJsonObject();
                    System.out.println(recyclebinJsonObject.get("Name"));
                }
            }
            else{
                System.out.println("No results");
            }
        }
        finally{
            if (restApi != null) {
                restApi.close();
            }
        }

    }
}