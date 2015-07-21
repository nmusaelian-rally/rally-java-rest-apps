import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import java.net.URI;


public class GetAllowedValues {

    public static void main(String[] args) throws Exception {

        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123"; 
        String applicationName = "NickM:GetAllowedValues";
        //String workspaceRef = "/workspace/123"; //default workspace
        String workspaceRef = "/workspace/345";//non-default workspace

        RallyRestApi restApi = null;
        try {
            String typeName = "Defect";
            restApi = new RallyRestApi(new URI(host),apiKey);
            restApi.setApplicationName(applicationName);
            QueryRequest typedefRequest = new QueryRequest("TypeDefinition");
            typedefRequest.setFetch(new Fetch("Attributes,ElementName"));
            typedefRequest.setQueryFilter(new QueryFilter("TypePath", "=", typeName));
            typedefRequest.setWorkspace(workspaceRef);
            QueryResponse typedefResponse = restApi.query(typedefRequest);
            for (int i=0; i<typedefResponse.getTotalResultCount();i++){
                JsonObject typedefObj = typedefResponse.getResults().get(i).getAsJsonObject();
                System.out.println("ElementName: " + typedefObj.get("ElementName") + " Attributes: " + typedefObj.get("Attributes"));
                JsonObject attributesCollection = typedefObj.get("Attributes").getAsJsonObject();
                QueryRequest attributesRequest = new QueryRequest(attributesCollection);
                attributesRequest.setFetch(new Fetch("AllowedValues","AttributeType","Name","Custom"));
                attributesRequest.setLimit(1000);
                QueryResponse attributesResponse = restApi.query(attributesRequest);
                for (int j=0; j<attributesResponse.getTotalResultCount();j++) {
                    JsonObject attributeObj = attributesResponse.getResults().get(j).getAsJsonObject();
                    if(attributeObj.get("AttributeType").getAsString().equals("STATE")){
                        System.out.println(attributeObj.get("Name") + " " + attributeObj.get("AttributeType") + " Custom: " + attributeObj.get("Custom"));
                        JsonObject allowedValuesCollection = attributeObj.get("AllowedValues").getAsJsonObject();
                        QueryRequest valuesRequest = new QueryRequest(allowedValuesCollection);
                        valuesRequest.setFetch(new Fetch("StringValue"));
                        valuesRequest.setWorkspace(workspaceRef); //if workspace is not set on this request, it is scoped to the user's default workspace. Setting workspace on typedefRequest has not effect here
                        QueryResponse valuesResponse = restApi.query(valuesRequest);
                        for (int k=0; k<valuesResponse.getTotalResultCount();k++) {
                            JsonObject allowedValueObj = valuesResponse.getResults().get(k).getAsJsonObject();
                            System.out.println(allowedValueObj.get("StringValue"));
                        }
                    }
                }
            }
        } finally {
            if (restApi != null) {
                restApi.close();
            }
        }
    }
}