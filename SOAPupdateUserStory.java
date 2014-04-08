import com.rallydev.webservice.v1_43.domain.*;
import com.rallydev.webservice.v1_43.service.*;

import org.apache.axis.client.Stub;
import java.net.URL;
import java.net.MalformedURLException;

//SOAP AND XML ARE NO LONGER SUPPORTED. IT IS RECOMMENDED TO USE REST INSTEAD OF SOAP. THIS CODE IS ONLY AN EXAMPLE.
public class SOAPupdateUserStory {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		URL rallyURL;
		
		RallyService rallyService = null;
		
		try {
			rallyURL = new URL("https://rally1.rallydev.com/slm/webservice/1.43/rallyservice");
			rallyService = (new RallyServiceServiceLocator()).getRallyService(rallyURL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new Exception("RallyWebServiceClient.main problem in creating the URL.");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("RallyWebServiceClient.main problem in creating the service.");
		}
		
		if (rallyService == null) {
			throw new Exception("RallyWebServiceClient.main service null!");
		}
		
		Stub serviceStub = (Stub) rallyService;
		serviceStub.setUsername("user@co.com");
		serviceStub.setPassword("secret");
		
		serviceStub.setMaintainSession(true);
		
		Workspace workspace = new Workspace();
		workspace.setRef("https://rally1.rallydev.com/slm/webservice/v2.0/workspace/1011574887"); 
		
		String queryString = "(FormattedID = \"US2736\")";
		
		String order = "FormattedID desc";
		
		String artifactType = "HierarchicalRequirement";
		
		long start = 1;
		long pageSize = 20;
		

		boolean fetchFullObjects = true;
				
		QueryResult queryResult = rallyService.query(workspace, artifactType, queryString, order, fetchFullObjects, start, pageSize);
		

		System.out.println("Query returned: " + queryResult.getTotalResultCount() + " objects.");

		System.out.println("Query returned: " + queryResult.getTotalResultCount() + " objects.");
		
		
		DomainObject[] queryResults = queryResult.getResults();
				
		for (int i=0; i < queryResults.length; i++) {
			DomainObject rallyObject = (DomainObject) queryResults[i];
			System.out.println("  result[" + i + "] = " + rallyObject);
			System.out.println("           ref = " + rallyObject.getClass());
			
			HierarchicalRequirement myStory = (HierarchicalRequirement) rallyObject;
			System.out.println("           Story Name: " + myStory.getName());
			System.out.println("           Story ScheduleState: " +myStory.getScheduleState());
			
			myStory.setScheduleState("In-Progress");
			myStory.setDescription("my description &()#*%");
			
			OperationResult operationResult = rallyService.update(myStory);
			System.out.println("           Story State after update = " + myStory.getScheduleState());
			System.out.println("           Story Description after update = " + myStory.getDescription());
			
		}	

	}
}