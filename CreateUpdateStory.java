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

public class CreateUpdateStory {

	public static void main(String[] args) throws URISyntaxException, IOException {
		

	       String host = "https://rally1.rallydev.com";
	       String apiKey = "_abc123";
	       String projectRef = "/project/12352608219";
	       String applicationName = "RestExample_createUpdateStory";
	        
		
		RallyRestApi restApi = new RallyRestApi(new URI(host),apiKey);
		restApi.setApplicationName(applicationName);   
        
 

        try {
        	for (int i=0; i<1; i++) { 
	            System.out.println("Creating a story...");
	            JsonObject newStory = new JsonObject();
	            newStory.addProperty("Name", "some story 999");
	            newStory.addProperty("Project", projectRef); 

	             
	            CreateRequest createRequest = new CreateRequest("hierarchicalrequirement", newStory);
	            CreateResponse createResponse = restApi.create(createRequest);  
	            if (createResponse.wasSuccessful()) {
	            	
	            	System.out.println(String.format("Created %s", createResponse.getObject().get("_ref").getAsString()));          
	            	
		            //Read story
		            String storyRef = Ref.getRelativeRef(createResponse.getObject().get("_ref").getAsString());
		            System.out.println(String.format("\nReading Story %s...", storyRef));
		            //Update story
		            JsonObject storyUpdate = new JsonObject();
			        storyUpdate.addProperty("Description", "this is description of story");
			        storyUpdate.addProperty("Iteration", "/iteration/12345");
			        UpdateRequest updateStoryRequest = new UpdateRequest(storyRef,storyUpdate);
			        UpdateResponse updateResponse = restApi.update(updateStoryRequest);
			        if (updateResponse.wasSuccessful()) {
						System.out.println("Successfully updated story: " + newStory.get("Name") +
								"Description: " + storyUpdate.get("Description"));
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