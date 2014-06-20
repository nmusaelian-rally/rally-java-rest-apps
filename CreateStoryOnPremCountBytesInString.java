import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.CreateRequest;
import com.rallydev.rest.response.CreateResponse;
import com.rallydev.rest.util.Ref;


public class CreateStoryOnPremCountBytesInString {

	public static void main(String[] args) throws URISyntaxException, IOException {
		

	       String host = "http://10.32.16.77";   //had to use http to bypass Exception : javax.net.ssl.SSLPeerUnverifiedException: peer not authenticated
	       String username = "user@co.com";
	       String password = "secret";
	        String wsapiVersion = "v2.0";
	        String projectRef = "/project/1234";
	        String applicationName = "Example-createStory";
	        
		
        RallyRestApi restApi = new RallyRestApi(
        		new URI(host),
        		username,
        		password);
        restApi.setWsapiVersion(wsapiVersion);
        
        
        String storyName = //"................................................................................."; //your string here
        restApi.setApplicationName(applicationName);   
        byte[] utf8Bytes = storyName.getBytes("UTF-8");
        System.out.println(utf8Bytes.length); // prints "253"
        
        byte[] utf16Bytes= storyName.getBytes("UTF-16");
        System.out.println(utf16Bytes.length); // prints "328"

        byte[] utf32Bytes = storyName.getBytes("UTF-32");
        System.out.println(utf32Bytes.length); // prints "652"

        byte[] isoBytes = storyName.getBytes("ISO-8859-1");
        System.out.println(isoBytes.length); // prints "163"

        byte[] winBytes = storyName.getBytes("CP1252");
        System.out.println(winBytes.length); // prints "163"
        
        try {
            
	            System.out.println("Creating a story...");
	            JsonObject newStory = new JsonObject();
	            
	            newStory.addProperty("Name", storyName);
	            newStory.addProperty("Project", projectRef); 

	             
	            CreateRequest createRequest = new CreateRequest("hierarchicalrequirement", newStory);
	            //System.out.println(createRequest.getBody());
	            CreateResponse createResponse = restApi.create(createRequest);  
	            if (createResponse.wasSuccessful()) {
	            	
	            	System.out.println(String.format("Created %s", createResponse.getObject().get("_ref").getAsString()));          
	            	String[] warningList;
	            	warningList = createResponse.getWarnings();
	            	for (int w = 0; w < warningList.length; w++){
	            		System.out.println(warningList[w]);
	            	}
		            //Read epic story
		            String storyRef = Ref.getRelativeRef(createResponse.getObject().get("_ref").getAsString());
		            System.out.println(String.format("\nReading epic story %s...",storyRef));
		        }
		        else {
	            	String[] createErrors;
	            	createErrors = createResponse.getErrors();
	        		System.out.println("Error!");
	            	for (int i=0; i<createErrors.length;i++) {
	            		System.out.println(createErrors[i]);
	            	}
	            }
        } finally {
            //Release all resources
            restApi.close();
        }   

	} 
	
}