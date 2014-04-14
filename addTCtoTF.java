import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.CreateRequest;
import com.rallydev.rest.request.GetRequest;
import com.rallydev.rest.response.CreateResponse;
import com.rallydev.rest.util.Ref;

public class addTCtoTF {

	public static void main(String[] args) throws URISyntaxException, IOException {
		

	       String host = "https://rally1.rallydev.com";
	       String username = "user@co.com";
	       String password = "secret";
	        String wsapiVersion = "v2.0";
	        String projectRef = "/project/12352608219";
	        String applicationName = "RestExample_createTFandTC";
	        
		
        RallyRestApi restApi = new RallyRestApi(
        		new URI(host),
        		username,
        		password);
        restApi.setWsapiVersion(wsapiVersion);
        restApi.setApplicationName(applicationName);   
        
 

        try {
        	for (int i=0; i<1; i++) {
	            System.out.println("Creating a test folder...");
	            JsonObject newTF = new JsonObject();
	            newTF.addProperty("Name", "tf via java");
	            newTF.addProperty("Project", projectRef); 

	             
	            CreateRequest createRequest = new CreateRequest("testfolder", newTF);
	            CreateResponse createResponse = restApi.create(createRequest);  
	            if (createResponse.wasSuccessful()) {
	            	
	            	    System.out.println(String.format("Created %s", createResponse.getObject().get("_ref").getAsString()));          
		            String folderRef = Ref.getRelativeRef(createResponse.getObject().get("_ref").getAsString());
		            System.out.println(String.format("\nReading TestFolder %s...",folderRef));
		            System.out.println("Creating a child test folder...");
		            JsonObject newChildTF = new JsonObject();
		            newChildTF.addProperty("Name", "tf via java");
		            newChildTF.addProperty("Project", projectRef); 
		            newChildTF.addProperty("Parent", folderRef); 

		             
		            CreateRequest createRequest2 = new CreateRequest("testfolder", newChildTF);
		            CreateResponse createResponse2 = restApi.create(createRequest2);  
		            if (createResponse.wasSuccessful()) {
		            	
		            	    System.out.println(String.format("Created %s", createResponse.getObject().get("_ref").getAsString()));          
			            String childFolderRef = Ref.getRelativeRef(createResponse2.getObject().get("_ref").getAsString());
			            System.out.println(String.format("\nReading Child TestFolder %s...",childFolderRef));
			            
			            System.out.println("Creating a test case...");
			            JsonObject newTC = new JsonObject();
			            newTC.addProperty("Name", "tc via java");
			            newTC.addProperty("Project", projectRef); 
			            newTC.addProperty("TestFolder", childFolderRef);
			            newTC.addProperty("Method", "Manual");

			             
			            CreateRequest createRequest3 = new CreateRequest("testcase", newTC);
			            CreateResponse createResponse3 = restApi.create(createRequest3); 
			            if (createResponse.wasSuccessful()) {
			            	
			            	    System.out.println(String.format("Created %s", createResponse3.getObject().get("_ref").getAsString()));          
				            String testCaseRef = Ref.getRelativeRef(createResponse3.getObject().get("_ref").getAsString());
				            System.out.println(String.format("\nReading TestCase %s...",testCaseRef));
			            }
			            
		            }
		            
	            } else {
	            	String[] createErrors;
	            	createErrors = createResponse.getErrors();
	        		System.out.println("Error!");
	            	for (int j=0; i<createErrors.length;j++) {
	            		System.out.println(createErrors[j]);
	            	}
	            }
        	}
        	
	
        } finally {
            restApi.close();
        }   

	} 
	
}