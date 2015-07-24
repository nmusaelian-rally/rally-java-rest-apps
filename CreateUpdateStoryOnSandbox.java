/*
ApiKey is preferred method of authentication on rally1.rallydev.com
but is not supported on sandbox.rallydev.com

Use basic authentication (username/password) on sandbox
NOTE: DO NOT APPEND SECURITY TOKEN TO POST REQUESTS
There is no need to hit security/authorize endpoint and append a token to POST requests
when using Rally API Toolkit for Java.
The toolkit does it under the hood.
 */

import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.CreateRequest;
import com.rallydev.rest.request.UpdateRequest;
import com.rallydev.rest.response.CreateResponse;
import com.rallydev.rest.response.UpdateResponse;
import com.rallydev.rest.util.Ref;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import com.rallydev.rest.client.HttpClient;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.scheme.Scheme;


public class CreateUpdateStoryOnSandbox {

    public static void main(String[] args) throws URISyntaxException, IOException {


        String host = "https://sandbox.rallydev.com";
        String username = "user@co.com";
        String password = "secret";
        String projectRef = "/project/12345";
        String applicationName = "NickM:CreateUpdateStoryOnSandbox";


        RallyRestApi restApi = new RallyRestApi(new URI(host),username, password);
        //OPTIONAL: if you have proxy:
        restApi.setProxy(new URI("http://myserver.company.com:1234"), "proxyusername", "proxypassword");
        
        restApi.setApplicationName(applicationName);
        /*
        OPTIONAL:
        This example also shows how to bypass
        Exception in thread "main" javax.net.ssl. SSLPeerUnverifiedException: peer not authenticated
        See https://rallycommunity.rallydev.com/answers?id=kA014000000PKJY for details
         */
        HttpClient client = restApi.getClient();
        try {
            SSLSocketFactory sf = new SSLSocketFactory(new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] certificate, String authType)
                        throws CertificateException {
                    //trust all certs
                    return true;
                }
            }, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, sf));
        //end of the block to bypass javax.net.ssl. SSLPeerUnverifiedException

            System.out.println("Creating a story...");
            JsonObject newStory = new JsonObject();
            newStory.addProperty("Name", "some story 999");
            newStory.addProperty("Project", projectRef);


            CreateRequest createRequest = new CreateRequest("hierarchicalrequirement", newStory);
            CreateResponse createResponse = restApi.create(createRequest);
            if (createResponse.wasSuccessful()) {

                System.out.println(String.format("Created %s", createResponse.getObject().get("_ref").getAsString()));

                //Read story
                String storyRef = Ref.getRelativeRef(createResponse.getObject().get("_ref").getAsString());
                System.out.println(String.format("\nReading Story %s...", storyRef));
                //Update story
                JsonObject storyUpdate = new JsonObject();
                storyUpdate.addProperty("Description", "this is description of story");
                storyUpdate.addProperty("Iteration", "/iteration/34715726689");
                UpdateRequest updateStoryRequest = new UpdateRequest(storyRef,storyUpdate);
                UpdateResponse updateResponse = restApi.update(updateStoryRequest);
                if (updateResponse.wasSuccessful()) {
                    System.out.println("Successfully updated story: " + newStory.get("Name") +
                            "Description: " + storyUpdate.get("Description"));
                }

            } else {
                String[] createErrors;
                createErrors = createResponse.getErrors();
                System.out.println("Error");
                for (int i=0; i<createErrors.length;i++) {
                    System.out.println(createErrors[i]);
                }
            }
        } catch (Exception e) {
        System.out.println(e);

        } finally {
            //Release all resources
            restApi.close();
        }
    }
}