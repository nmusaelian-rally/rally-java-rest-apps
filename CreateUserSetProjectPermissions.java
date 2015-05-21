import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.request.CreateRequest;
import com.rallydev.rest.response.CreateResponse;
import com.rallydev.rest.util.Ref;
import com.rallydev.rest.util.Fetch;

public class CreateUserSetProjectPermissions {

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {

        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123"; //subadmin
        String applicationName = "Nick:create user and projectpermissions";
        RallyRestApi restApi = new RallyRestApi(new URI(host), apiKey);
        restApi.setApplicationName(applicationName);
        String workspaceRef = "/workspace/12345"; 

        try {
            JsonObject newUser = new JsonObject();
            newUser.addProperty("UserName", "user777@co.com");
            newUser.addProperty("EmailAddress", "user@co.com");

            CreateRequest createUserRequest = new CreateRequest("user", newUser);
            CreateResponse createUserResponse = restApi.create(createUserRequest);

            if (createUserResponse.wasSuccessful()) {
                //read user
                String userRef = Ref.getRelativeRef(createUserResponse.getObject().get("_ref").getAsString());
                System.out.println(String.format("Created %s", createUserResponse.getObject().get("_refObjectName").getAsString()));
                //get all projects in the workspace
                QueryRequest  projectRequest = new QueryRequest("Project");
                projectRequest.setFetch(new Fetch("ObjectID", "Name"));
                projectRequest.setLimit(10000);
                projectRequest.setWorkspace(workspaceRef);
                QueryResponse projectResponse = restApi.query(projectRequest);
                int resultCount = projectResponse.getTotalResultCount();
                System.out.println(resultCount);
                if(resultCount >0){
                    for (int i=0;i<resultCount;i++){
                        JsonObject projectObject = projectResponse.getResults().get(i).getAsJsonObject();
                        String projectRef = projectObject.get("_ref").getAsString();
                        System.out.println(projectObject.get("ObjectID") + " " + projectObject.get("Name"));
                        //create projectpermission object
                        JsonObject newPP = new JsonObject();
                        newPP.addProperty("User", userRef);
                        newPP.addProperty("Project", projectRef);
                        newPP.addProperty("Role","Editor");
                        CreateRequest createProjectPermissionRequest = new CreateRequest("projectpermission", newPP);
                        CreateResponse createProjectPermissionResponse = restApi.create(createProjectPermissionRequest);
                        if (createUserResponse.wasSuccessful()) {
                            String projectPermissionRef = Ref.getRelativeRef(createProjectPermissionResponse.getObject().get("_ref").getAsString());
                            System.out.println(String.format("Created %s", createProjectPermissionResponse.getObject().get("_ref").getAsString()));
                        }
                    }
                }
                else{
                    System.out.println("No results");
                }

            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            restApi.close();
        }
    }
}