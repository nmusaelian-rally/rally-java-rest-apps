import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.CreateRequest;
import com.rallydev.rest.request.DeleteRequest;
import com.rallydev.rest.request.GetRequest;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.request.UpdateRequest;
import com.rallydev.rest.response.CreateResponse;
import com.rallydev.rest.response.DeleteResponse;
import com.rallydev.rest.response.GetResponse;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.response.UpdateResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import com.rallydev.rest.util.Ref;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.codec.binary.Base64;
public class AttachmentDownloadFromStory {

	public static void main(String[] args) throws URISyntaxException, IOException {

		// Create and configure a new instance of RallyRestApi
		// Connection parameters
		String rallyURL = "https://rally1.rallydev.com";
		String wsapiVersion = "v2.0";
		String applicationName = "RestExample_DownloadAttachment";
		String apiKey = "_sdsuJNAiTN8Bj88VbiOi5Q0U9Qor2f02MHmXlpJJLJa";

		RallyRestApi restApi = new RallyRestApi(
			new URI(rallyURL),
			apiKey
		);
		restApi.setWsapiVersion(wsapiVersion);
		restApi.setApplicationName(applicationName);       

		// Workspace and Project Settings
		String myWorkspace = "My Workspace";
		String myProject = "My Project";

		// FormattedID of Existing Test Case to Query
		String existStoryFormattedID = "US43";       

		// Get reference to Workspace of interest
		QueryRequest workspaceRequest = new QueryRequest("Workspace");
		workspaceRequest.setFetch(new Fetch(new String[] {"Name", "Owner", "Projects"}));
		workspaceRequest.setQueryFilter(new QueryFilter("Name", "=", myWorkspace));
		QueryResponse workspaceQueryResponse = restApi.query(workspaceRequest);
		String workspaceRef = workspaceQueryResponse.getResults().get(0).getAsJsonObject().get("_ref").toString();

		// Get reference to Project of interest
		QueryRequest projectRequest = new QueryRequest("Project");
		projectRequest.setFetch(new Fetch(new String[] {"Name", "Owner", "Projects"}));
		projectRequest.setQueryFilter(new QueryFilter("Name", "=", myProject));
		QueryResponse projectQueryResponse = restApi.query(projectRequest);
		String projectRef = projectQueryResponse.getResults().get(0).getAsJsonObject().get("_ref").toString();      

		// Query for existing User Story
		System.out.println("Querying for User Story: " + existStoryFormattedID);

		QueryRequest  existUserStoryRequest = new QueryRequest("HierarchicalRequirement");
		existUserStoryRequest.setFetch(new Fetch(new String [] {"FormattedID","Name","Attachments"}));
		existUserStoryRequest.setQueryFilter(new QueryFilter("FormattedID", "=", existStoryFormattedID));
		QueryResponse userStoryQueryResponse = restApi.query(existUserStoryRequest);
		JsonObject existUserStoryJsonObject = userStoryQueryResponse.getResults().get(0).getAsJsonObject();
		String existUserStoryRef = userStoryQueryResponse.getResults().get(0).getAsJsonObject().get("_ref").toString();
		
		// Query for attachments using attachments collection _ref		
		JsonObject attachmentsJsonObject = existUserStoryJsonObject.getAsJsonObject("Attachments");
        QueryRequest attachmentsRequest = new QueryRequest(attachmentsJsonObject);
        attachmentsRequest.setFetch(new Fetch(new String[] {"Content", "Content-Type","Name"}));	
        JsonArray attachmentsArray = restApi.query(attachmentsRequest).getResults();

		// Take first attachment
		JsonObject attachmentObject = attachmentsArray.get(0).getAsJsonObject();
		String attachmentRef = attachmentObject.get("_ref").getAsString();
		JsonObject attachmentContentReference = attachmentObject.getAsJsonObject("Content");
	    // Grab attachment name
	    String attachmentName = attachmentObject.get("Name").getAsString();

		// Read attachmentContent from Ref
		System.out.println("Reading First Attachment: " + attachmentRef);
		
		String attachmentContentStringRef = attachmentContentReference.get("_ref").getAsString();
		System.out.println("AttachmentContent Ref: " + attachmentContentStringRef);

		GetRequest attachmentContentRequest = new GetRequest(attachmentContentStringRef);
		attachmentContentRequest.setFetch(new Fetch(new String[] {"Content"}));
		GetResponse attachmentContentResponse = restApi.get(attachmentContentRequest);

		// AttachmentContent object
	    // Read Content String of AttachmentContent
	    String attachmentContentBase64String = attachmentContentResponse.getObject().get("Content").getAsString();

   

		// Decode base64 string into bytes
		byte[] imageBytes = Base64.decodeBase64(attachmentContentBase64String);

		// Image output
		String imageFilePath = "/Users/markwilliams/Desktop/";
		String fullImageFile = imageFilePath + attachmentName;

		// Write output file  
		System.out.println("Writing attachment to file: " + attachmentName);
		try {        

		OutputStream imageOutputStream = new FileOutputStream(fullImageFile);
		imageOutputStream.write(imageBytes);
		imageOutputStream.flush();
		imageOutputStream.close();

		} catch (Exception e) {
			System.out.println("Exception occurred while write image file ");
			e.printStackTrace();            
		}

		finally {
			//Release all resources
			restApi.close();
		}                
	}
}