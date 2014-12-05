import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.request.UpdateRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.response.UpdateResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;

public class BulkUpdateTagsOnDefects {

	public static void main(String[] args) throws URISyntaxException, IOException {
		

		   String host = "https://rally1.rallydev.com";
	       String apiKey = "_abc123";
	       String workspaceRef = "/workspace/12352608129";
	       String applicationName = "bulkUpdateTagsOnDefects";
	        
		
	       RallyRestApi restApi = new RallyRestApi(new URI(host),apiKey);
           restApi.setApplicationName(applicationName);   

        try {
        	String tagToRemove = "tag1";
        	String tagToAdd = "new tag";
        	
        	//find all defects with "tag1" with ScheduleState < Completed
        	QueryRequest defectRequest = new QueryRequest("Defect");
        	defectRequest.setWorkspace(workspaceRef);
        	defectRequest.setFetch(new Fetch(new String[] {"Name", "FormattedID", "Tags"}));
        	defectRequest.setLimit(1000);
	        
        	defectRequest.setQueryFilter((new QueryFilter("Tags.Name", "contains", tagToRemove)).and(new QueryFilter("ScheduleState", "<", "Completed")));
	        
	        QueryResponse defectQueryResponse = restApi.query(defectRequest);
	        System.out.println("Size: " + defectQueryResponse.getTotalResultCount());
	        
	        /*
	        QueryRequest  oldTagRequest = new QueryRequest("Tag");
        	oldTagRequest.setWorkspace(workspaceRef);
        	oldTagRequest.setQueryFilter(new QueryFilter("Name", "=", tagToRemove));
        	QueryResponse oldTagQueryResponse = restApi.query(oldTagRequest);
            if(oldTagQueryResponse.getTotalResultCount() == 0){
            	System.out.println("Cannot find tag: " + tagToRemove);
                return;
            }
            JsonObject oldTagJsonObject = oldTagQueryResponse.getResults().get(0).getAsJsonObject();
 	        String oldTagRef = oldTagJsonObject.get("_ref").getAsString();
 	        System.out.println(oldTagRef);
	        */
        	QueryRequest  newTagRequest = new QueryRequest("Tag");
        	newTagRequest.setWorkspace(workspaceRef);
        	newTagRequest.setQueryFilter(new QueryFilter("Name", "=", tagToAdd));
        	QueryResponse newTagQueryResponse = restApi.query(newTagRequest);
            if(newTagQueryResponse.getTotalResultCount() == 0){
            	System.out.println("Cannot find tag: " + tagToAdd);
                return;
            }
            JsonObject newTagJsonObject = newTagQueryResponse.getResults().get(0).getAsJsonObject();
 	        String newTagRef = newTagJsonObject.get("_ref").getAsString();
 	        System.out.println(newTagRef);
            
            if (defectQueryResponse.getTotalResultCount() == 0) {
             System.out.println("Cannot find defects tagged : " + tagToRemove);
             return;
            }
            else{
            	 for (int i=0; i<defectQueryResponse.getResults().size();i++){
            		 JsonObject defectJsonObject = defectQueryResponse.getResults().get(i).getAsJsonObject();
            		 String defectRef = defectJsonObject.get("_ref").getAsString();
            		 System.out.println("Name: " + defectJsonObject.get("Name") + " FormattedID: " + defectJsonObject.get("FormattedID"));
            		 int numberOfTags = defectJsonObject.getAsJsonObject("Tags").get("Count").getAsInt();
            		 QueryRequest tagCollectionRequest = new QueryRequest(defectJsonObject.getAsJsonObject("Tags"));
            		 tagCollectionRequest.setFetch(new Fetch("Name","ObjectID"));
            		 JsonArray tags = restApi.query(tagCollectionRequest).getResults();
            		 for (int j=0;j<numberOfTags;j++){
             	        System.out.println("Tag Name: " + tags.get(j).getAsJsonObject().get("Name"));
             	    }
            		 tags.add(newTagJsonObject);
            		 JsonObject defectUpdate = new JsonObject();
            		 defectUpdate.add("Tags", tags);
            		 UpdateRequest updateDefectRequest = new UpdateRequest(defectRef,defectUpdate);
            		 UpdateResponse updateDefectResponse = restApi.update(updateDefectRequest);
            		 if (updateDefectResponse.wasSuccessful()) {
         				System.out.println("Successfully updated : " + defectJsonObject.get("FormattedID") + " Tags after update: ");
         				QueryRequest tagCollectionRequest2 = new QueryRequest(defectJsonObject.getAsJsonObject("Tags"));
         				tagCollectionRequest2.setFetch(new Fetch("Name","ObjectID"));
         	    	    JsonArray tagsAfterUpdate = restApi.query(tagCollectionRequest2).getResults();
         	    	    int numberOfTagsAfterUpdate = restApi.query(tagCollectionRequest2).getResults().size();
         	    	    for (int j=0;j<numberOfTagsAfterUpdate;j++){
         	    	        System.out.println("Tag Name: " + tagsAfterUpdate.get(j).getAsJsonObject().get("Name"));
         	    	    }
         	        }
            	 }
            }
	        
        } finally {
            restApi.close();
        }   

	} 
}