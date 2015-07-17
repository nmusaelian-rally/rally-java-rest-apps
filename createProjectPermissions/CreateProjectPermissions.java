import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.CreateRequest;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.CreateResponse;
import com.rallydev.rest.util.Ref;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class CreateProjectPermissions {
    public static void main(String[] args) throws URISyntaxException, IOException {
        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123";
        String applicationName = "NickM-CreateProjectPermissions";


        RallyRestApi restApi = new RallyRestApi(new URI(host),apiKey);
        restApi.setApplicationName(applicationName);
        try {
            restApi = new RallyRestApi(new URI(host),apiKey);
            QueryRequest request = new QueryRequest("HierarchicalRequirement");

            restApi.setApplicationName(applicationName);
            JsonObject newPP = new JsonObject();
            newPP.addProperty("Project", "/project/123456");  
            newPP.addProperty("Role", "Editor");
            newPP.addProperty("User", "/user/777"); 
            CreateRequest createRequest = new CreateRequest("ProjectPermission", newPP);
            CreateResponse createResponse = restApi.create(createRequest);
                if (createResponse.wasSuccessful()) {
                    String ppRef = Ref.getRelativeRef(createResponse.getObject().get("_ref").getAsString());
                    System.out.println(String.format("Created %s", ppRef));
                } else {
                    String[] createErrors;
                    createErrors = createResponse.getErrors();
                    System.out.println("Error occurred creating a changeset: ");
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