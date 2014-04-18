/*
This app creates an epic story, two child stories and three tasks per child story
*/


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.CreateRequest;
import com.rallydev.rest.response.CreateResponse;
import com.rallydev.rest.util.Ref;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class CreateStoriesAndTasks {

	public static void main(String[] args) throws URISyntaxException, IOException {
		

	       String host = "https://rally1.rallydev.com";
	       String username = "user@co.com";
	       String password = "secret";
	        String wsapiVersion = "v2.0";
	        String projectRef = "/project/12345";
	        String applicationName = "RestExample_createEpicStoryStoriesTasks";
	        
		
        RallyRestApi restApi = new RallyRestApi(
        		new URI(host),
        		username,
        		password);
        restApi.setWsapiVersion(wsapiVersion);
        restApi.setApplicationName(applicationName);   
        
        //Date d = new Date();
        DateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");

        try {
        	for (int i=0; i<1; i++) {

	            //Add an epic story  
	            System.out.println("Creating a story...");
	            JsonObject newEpic = new JsonObject();
	            newEpic.addProperty("Name", "epic: " + iso.format(new Date()));
	            newEpic.addProperty("Project", projectRef); 

	             
	            CreateRequest createRequest = new CreateRequest("hierarchicalrequirement", newEpic);
	            CreateResponse createResponse = restApi.create(createRequest);  
	            if (createResponse.wasSuccessful()) {
	            	
	            	System.out.println(String.format("Created %s", createResponse.getObject().get("_ref").getAsString()));          
	            	
		            //Read epic story
		            String epicRef = Ref.getRelativeRef(createResponse.getObject().get("_ref").getAsString());
		            System.out.println(String.format("\nReading epic story %s...",epicRef));
		            System.out.println("Creating child stories...");
		            for (int j=0; j<2; j++){
		            	JsonObject newChildStory = new JsonObject();
		            	newChildStory.addProperty("Name", "story: " + iso.format(new Date()));
		            	newChildStory.addProperty("Project", projectRef); 
		            	newChildStory.addProperty("Parent", epicRef); 

			             
			            CreateRequest createRequest2 = new CreateRequest("hierarchicalrequirement", newChildStory);
			            CreateResponse createResponse2 = restApi.create(createRequest2);  
			            if (createResponse.wasSuccessful()) {
			            	
			            	System.out.println(String.format("Created %s", createResponse.getObject().get("_ref").getAsString()));          
			            	
				            //Read child story
				            String childStoryRef = Ref.getRelativeRef(createResponse2.getObject().get("_ref").getAsString());
				            System.out.println(String.format("\nReading Child TestFolder %s...",childStoryRef));
				            
				            System.out.println("Creating a task...");
				            
				            for (int k=0; k<3; k++){
				            	JsonObject newTask = new JsonObject();
				            	newTask.addProperty("Name", "task: " + iso.format(new Date()));
				            	newTask.addProperty("WorkProduct", childStoryRef);
					            CreateRequest createRequest3 = new CreateRequest("task", newTask);
					            CreateResponse createResponse3 = restApi.create(createRequest3); 
					            if (createResponse.wasSuccessful()) {
					            	
					            	System.out.println(String.format("Created %s", createResponse3.getObject().get("_ref").getAsString()));          
					            	
						            //Read task
						            String taskRef = Ref.getRelativeRef(createResponse3.getObject().get("_ref").getAsString());
						            System.out.println(String.format("\nReading task %s...",taskRef));
					            }
				            }
				            
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
            //Release all resources
            restApi.close();
        }   

	} 
	
}