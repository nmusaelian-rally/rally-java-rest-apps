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

public class CreateDefectAddZipAttachment{
    public static void main(String[] args) throws URISyntaxException, IOException {


        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123"; 
        String projectRef = "/project/12352608219";
        String applicationName = "RestExample_createDefectAttachZip";


        RallyRestApi restApi = new RallyRestApi(new URI(host),apiKey);
        restApi.setApplicationName(applicationName);

        //Read User
        QueryRequest userRequest = new QueryRequest("User");
        userRequest.setQueryFilter(new QueryFilter("UserName", "=", "nick@gmt-8.com"));
        QueryResponse userQueryResponse = restApi.query(userRequest);
        JsonArray userQueryResults = userQueryResponse.getResults();
        JsonElement userQueryElement = userQueryResults.get(0);
        JsonObject userQueryObject = userQueryElement.getAsJsonObject();
        String userRef = userQueryObject.get("_ref").getAsString();



        try {
            for (int i=0; i<1; i++) {

                //Create a defect
                System.out.println("Creating a defect...");
                JsonObject newDefect = new JsonObject();
                newDefect.addProperty("Name", "bug12345");
                newDefect.addProperty("Project", projectRef);


                CreateRequest createRequest = new CreateRequest("defect", newDefect);
                CreateResponse createResponse = restApi.create(createRequest);
                if (createResponse.wasSuccessful()) {

                    System.out.println(String.format("Created %s", createResponse.getObject().get("_ref").getAsString()));

                    //Read defect
                    String ref = Ref.getRelativeRef(createResponse.getObject().get("_ref").getAsString());
                    System.out.println(String.format("\nReading Defect %s...", ref));

                    String imageFilePath = "/Users/myuser/Desktop/"; //on mac
                    String imageFileName = "test.zip";
                    String fullImageFile = imageFilePath + imageFileName;
                    String imageBase64String;
                    long attachmentSize;

                    // Open file
                    RandomAccessFile myImageFileHandle = new RandomAccessFile(fullImageFile, "r");

                    try {
                        long longLength = myImageFileHandle.length();
                        long maxLength = 5000000;
                        if (longLength >= maxLength) throw new IOException("File size >= 5 MB Upper limit for Rally.");
                        int fileLength = (int) longLength;

                        // Read file and return data
                        byte[] fileBytes = new byte[fileLength];
                        myImageFileHandle.readFully(fileBytes);
                        imageBase64String = Base64.encodeBase64String(fileBytes);
                        attachmentSize = fileLength;

                        // First create AttachmentContent from image string
                        JsonObject myAttachmentContent = new JsonObject();
                        myAttachmentContent.addProperty("Content", imageBase64String);
                        CreateRequest attachmentContentCreateRequest = new CreateRequest("AttachmentContent", myAttachmentContent);
                        CreateResponse attachmentContentResponse = restApi.create(attachmentContentCreateRequest);
                        String myAttachmentContentRef = attachmentContentResponse.getObject().get("_ref").getAsString();
                        System.out.println("Attachment Content created: " + myAttachmentContentRef);

                        // Now create the Attachment itself
                        JsonObject myAttachment = new JsonObject();
                        myAttachment.addProperty("Artifact", ref);
                        myAttachment.addProperty("Content", myAttachmentContentRef);
                        myAttachment.addProperty("Name", "test.zip");
                        myAttachment.addProperty("Description", "Attachment From REST");
                        myAttachment.addProperty("ContentType","application/octet-stream");
                        myAttachment.addProperty("Size", attachmentSize);
                        myAttachment.addProperty("User", userRef);

                        CreateRequest attachmentCreateRequest = new CreateRequest("Attachment", myAttachment);
                        CreateResponse attachmentResponse = restApi.create(attachmentCreateRequest);
                        String myAttachmentRef = attachmentResponse.getObject().get("_ref").getAsString();
                        System.out.println("Attachment  created: " + myAttachmentRef);

                        if (attachmentResponse.wasSuccessful()) {
                            System.out.println("Successfully created Attachment");
                        } else {
                            String[] attachmentContentErrors;
                            attachmentContentErrors = attachmentResponse.getErrors();
                            System.out.println("Error occurred creating Attachment: ");
                            for (int j=0; j<attachmentContentErrors.length;j++) {
                                System.out.println(attachmentContentErrors[j]);
                            }
                        }
                    }catch (Exception e) {
                        System.out.println("Exception occurred while attempting to create Content and/or Attachment: ");
                        e.printStackTrace();
                    }

                } else {
                    String[] createErrors;
                    createErrors = createResponse.getErrors();
                    System.out.println("Error occurred creating a defect: ");
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