import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.CreateRequest;
import com.rallydev.rest.request.GetRequest;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.CreateResponse;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.QueryFilter;
import com.rallydev.rest.util.Ref;

public class CreatePI{
    public static void main(String[] args) throws URISyntaxException, IOException {


        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123"; 
        String projectRef = "/project/456"; 
        String applicationName = "Nick:CreatePI";


        RallyRestApi restApi = new RallyRestApi(new URI(host),apiKey);
        restApi.setApplicationName(applicationName);


        try {
                System.out.println("Creating a PI/Initiative...");
                JsonObject newInitiative = new JsonObject();
                newInitiative.addProperty("Name", "Jupiter Lander Initiative");
                newInitiative.addProperty("Project", projectRef);

                CreateRequest createRequest = new CreateRequest("portfolioitem/initiative", newInitiative);
                CreateResponse createResponse = restApi.create(createRequest);
                if (createResponse.wasSuccessful()) {

                    System.out.println(String.format("Created %s %s", createResponse.getObject().get("FormattedID"), createResponse.getObject().get("_ref").getAsString()));

                    String initiativeRef = Ref.getRelativeRef(createResponse.getObject().get("_ref").getAsString());
                    System.out.println(String.format("\nReading Initiative %s...", initiativeRef));
                    System.out.println("Creating a PI/Feature...");

                    JsonObject newFeature = new JsonObject();
                    newFeature.addProperty("Name", "Jupiter Flyby Feature");
                    newFeature.addProperty("Project", projectRef);
                    newFeature.addProperty("Parent", initiativeRef);

                    CreateRequest createRequest2 = new CreateRequest("portfolioitem/feature", newFeature);
                    CreateResponse createResponse2 = restApi.create(createRequest2);
                    if (createResponse.wasSuccessful()) {

                        System.out.println(String.format("Created %s %s", createResponse2.getObject().get("FormattedID"), createResponse2.getObject().get("_ref").getAsString()));
                    }

                    } else {
                    String[] createErrors;
                    createErrors = createResponse.getErrors();
                    System.out.println("Error occurred creating a defect: ");
                    for (int k=0; k<createErrors.length;k++) {
                        System.out.println(createErrors[k]);
                    }
                }
        } finally {
            restApi.close();
        }

    }
}