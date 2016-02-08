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
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.response.CreateResponse;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.QueryFilter;

public class CreateTestResultAddAttachment{
    public static void main(String[] args) throws URISyntaxException, IOException {
        String host = "https://rally1.rallydev.com";
        String apiKey = "_abc123"; //nick@chicago
        String workspaceRef = "/workspace/12352608129";
        String applicationName = "Nick:createTCR-addAttachment";

        RallyRestApi restApi = new RallyRestApi(new URI(host),apiKey);
        restApi.setApplicationName(applicationName);

        //Read User
        QueryRequest userRequest = new QueryRequest("User");
        userRequest.setFetch(new Fetch("UserName", "Subscription", "DisplayName", "SubscriptionAdmin"));
        userRequest.setQueryFilter(new QueryFilter("UserName", "=", "nick@denver.com"));
        QueryResponse userQueryResponse = restApi.query(userRequest);
        JsonArray userQueryResults = userQueryResponse.getResults();
        JsonElement userQueryElement = userQueryResults.get(0);
        JsonObject userQueryObject = userQueryElement.getAsJsonObject();
        String userRef = userQueryObject.get("_ref").getAsString();
        System.out.println(userRef);

        // Query for Test Case to which we want to add results
        QueryRequest testCaseRequest = new QueryRequest("TestCase");
        testCaseRequest.setFetch(new Fetch("FormattedID","Name"));
        testCaseRequest.setWorkspace(workspaceRef);
        testCaseRequest.setQueryFilter(new QueryFilter("FormattedID", "=", "TC1180"));
        QueryResponse testCaseQueryResponse = restApi.query(testCaseRequest);
        JsonObject testCaseJsonObject = testCaseQueryResponse.getResults().get(0).getAsJsonObject();
        String testCaseRef = testCaseQueryResponse.getResults().get(0).getAsJsonObject().get("_ref").getAsString();

        try {
            //Add a Test Case Result
            System.out.println(testCaseRef);
            System.out.println("Creating Test Case Result...");
            JsonObject newTestCaseResult = new JsonObject();
            newTestCaseResult.addProperty("Verdict", "Pass");
            newTestCaseResult.addProperty("Date", "2016-02-08T00:00:00.000Z");
            newTestCaseResult.addProperty("Notes", "Some Scheduled Test");
            newTestCaseResult.addProperty("Build", "4.0");
            newTestCaseResult.addProperty("Tester", userRef);
            newTestCaseResult.addProperty("TestCase", testCaseRef);
            /*
            Error if workspace is not specified below while the user that makes a request (whose ApiKey is used in the code) has a default workspace different from the destination workspace
            Could not set value for Test Case: Cannot connect object to value, Test Case value is in a different workspace. [object workspace OID=1011574887, value workspace OID=12352608129
             */
            newTestCaseResult.addProperty("Workspace", workspaceRef);
            CreateRequest createRequest = new CreateRequest("testcaseresult", newTestCaseResult);
            CreateResponse createResponse = restApi.create(createRequest);
            if (createResponse.wasSuccessful()) {
                addAttachment(restApi, createResponse.getObject().get("_ref").getAsString(), userRef, workspaceRef);
            }
            else {
                String[] createErrors;
                createErrors = createResponse.getErrors();
                System.out.println("Error occurred creating Test Case Result: ");
                for (int i = 0; i < createErrors.length; i++) {
                    System.out.println(createErrors[i]);
                }
            }

        } finally {
            //Release all resources
            restApi.close();
        }

    }
    private static void addAttachment(RallyRestApi restApi, String testCaseResultRef, String userRef, String workspaceRef) throws URISyntaxException,IOException {
        System.out.println("Created " + testCaseResultRef);

        String imageFilePath = "/Users/nmusaelian/Desktop/";
        String imageFileName = "goodnight-moon_3.jpg";
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
            //java.lang.NullPointerException if workspace parameter is not set as below
            attachmentContentCreateRequest.addParam("workspace", workspaceRef);
            CreateResponse attachmentContentResponse = restApi.create(attachmentContentCreateRequest);
            String myAttachmentContentRef = attachmentContentResponse.getObject().get("_ref").getAsString();
            System.out.println("Attachment Content created: " + myAttachmentContentRef);

            // Now create the Attachment itself
            JsonObject myAttachment = new JsonObject();
            myAttachment.addProperty("TestCaseResult", testCaseResultRef);
            myAttachment.addProperty("Content", myAttachmentContentRef);
            myAttachment.addProperty("Name", "AttachmentFromREST.png");
            myAttachment.addProperty("Description", "Attachment From REST");
            myAttachment.addProperty("ContentType","image/png");
            myAttachment.addProperty("Size", attachmentSize);
            myAttachment.addProperty("User", userRef);

            CreateRequest attachmentCreateRequest = new CreateRequest("Attachment", myAttachment);
            //java.lang.NullPointerException if workspace parameter is not set as below
            attachmentCreateRequest.addParam("workspace", workspaceRef);
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

    }
}