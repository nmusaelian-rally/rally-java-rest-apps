import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.CreateRequest;
import com.rallydev.rest.request.UpdateRequest;
import com.rallydev.rest.response.CreateResponse;
import com.rallydev.rest.response.UpdateResponse;
import com.rallydev.rest.util.Ref;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class CreateUpdateTC {

	public static void main(String[] args) throws URISyntaxException, IOException {
		

	        String url = "https://rally1.rallydev.com";
	        String username = "user@co.com";
	        String password = "secret";
	        String projectRef = "/project/12345";
	        String applicationName = "create update test case";
	        
		
        RallyRestApi restApi = new RallyRestApi(
        		new URI(url),
        		username,
        		password);
        restApi.setApplicationName(applicationName);   
 

        try {
        	for (int i=0; i<1; i++) { 
	            System.out.println("Creating a test case...");
	            JsonObject newTC = new JsonObject();
	            newTC.addProperty("Name", "some testcase");
	            newTC.addProperty("Project", projectRef); 

	             
	            CreateRequest createRequest = new CreateRequest("testcase", newTC);
	            CreateResponse createResponse = restApi.create(createRequest);  
	            if (createResponse.wasSuccessful()) {
	            	
	            	System.out.println(String.format("Created %s", createResponse.getObject().get("_ref").getAsString()));          
	            	
		            //Read tc
		            String tcRef = Ref.getRelativeRef(createResponse.getObject().get("_ref").getAsString());
		            System.out.println(String.format("\nReading test case %s...", tcRef));
		            //Update tc
		            JsonObject tcUpdate = new JsonObject();
			        tcUpdate.addProperty("c_CustomString", url);
			        UpdateRequest updateRequest = new UpdateRequest(tcRef,tcUpdate);
			        UpdateResponse updateResponse = restApi.update(updateRequest);
			        if (updateResponse.wasSuccessful()) {
						System.out.println("Successfully updated test case: " + newTC.get("Name") +
								" CustomString: " + tcUpdate.get("c_CustomString"));
			        }
			        else {
			        	String[] updateErrors;
		            	updateErrors = createResponse.getErrors();
		        		System.out.println("Error");
		            	for (int j=0; i<updateErrors.length;j++) {
		            		System.out.println(updateErrors[j]);
		            	}
			        }
			        
	            } else {
	            	String[] createErrors;
	            	createErrors = createResponse.getErrors();
	        		System.out.println("Error");
	            	for (int j=0; i<createErrors.length;j++) {
	            		System.out.println(createErrors[j]);
	            	}
	            }
        	}
        	
        } finally {
            //Release all resources
            restApi.close();
        }   

	} 
}