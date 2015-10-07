import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.util.Fetch;

import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.request.UpdateRequest;
import com.rallydev.rest.response.UpdateResponse;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.QueryFilter;
import java.util.Date;


public class PiTypes{
    public static void main(String[] args) throws URISyntaxException, IOException {


        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123"; //use your ApiKey
        String applicationName = "Nick:PiTypes";


        RallyRestApi restApi = new RallyRestApi(new URI(host),apiKey);
        restApi.setApplicationName(applicationName);
        String workspaceRef = "/workspace/12345";

        try {
            QueryRequest typedefRequest = new QueryRequest("TypeDefinition");
            typedefRequest.setFetch(new Fetch("ElementName","Ordinal"));
            typedefRequest.setOrder("Ordinal DESC");
            typedefRequest.setQueryFilter(new QueryFilter("Parent.Name", "=", "Portfolio Item"));
            typedefRequest.setWorkspace(workspaceRef);
            QueryResponse typedefResponse = restApi.query(typedefRequest);
            for (int i=0; i<typedefResponse.getTotalResultCount();i++){
                JsonObject typedefObj = typedefResponse.getResults().get(i).getAsJsonObject();
                if(typedefObj.get("Ordinal").getAsInt() > -1){
                    System.out.println("ElementName: " + typedefObj.get("ElementName") + ", " +
                            "Ordinal: " + typedefObj.get("Ordinal"));
                }
            }
        } finally {
            restApi.close();
        }
    }
}