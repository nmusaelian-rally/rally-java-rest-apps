import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class FindDefectsNotUpdatedIn30Days {

	public static void main(String[] args) throws URISyntaxException, IOException {

		String host = "https://rally1.rallydev.com";
		String apiKey = "_myKey";
	        String applicationName = "FindDefectsNotUpdatedIn30Days";
	        String projectRef = "/project/12352608219";
	        
	        
	        RallyRestApi restApi = null;
			try {
		        restApi = new RallyRestApi(new URI(host), apiKey);
		        restApi.setApplicationName(applicationName); 
		        
		       
		        int x = -30;
		        Calendar cal = GregorianCalendar.getInstance();
		        cal.add( Calendar.DAY_OF_YEAR, x);
		        Date nDaysAgoDate = cal.getTime();
		       
		        System.out.println(nDaysAgoDate);
		        
		        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
		     
		        QueryRequest defectRequest = new QueryRequest("Defect");
		        defectRequest.setProject(projectRef);
		        defectRequest.setFetch(new Fetch(new String[] {"Name", "FormattedID","State", "Priority", "Severity", "CreationDate"}));
		        defectRequest.setLimit(1000);
		        defectRequest.setScopedDown(false);
		        defectRequest.setScopedUp(false);
		        defectRequest.setQueryFilter(new QueryFilter("LastUpdateDate", ">", iso.format(nDaysAgoDate)));
		        

		        
		        QueryResponse defectQueryResponse = restApi.query(defectRequest);
		        System.out.println("Successful: " + defectQueryResponse.wasSuccessful());
		        System.out.println("Size: " + defectQueryResponse.getTotalResultCount());
		        System.out.println("Results Size: " + defectQueryResponse.getResults().size());
		        
		        for (int i=0; i<defectQueryResponse.getResults().size();i++){
		        	JsonObject defectJsonObject = defectQueryResponse.getResults().get(i).getAsJsonObject();
		        	System.out.println("Name: " + defectJsonObject.get("Name") + " State: " + defectJsonObject.get("State") + " Priority: " + defectJsonObject.get("Priority") + " FormattedID: " + defectJsonObject.get("FormattedID"));
				}
		        
		        
			} finally {
				if (restApi != null) {
					restApi.close();
				}
			}
		}
}