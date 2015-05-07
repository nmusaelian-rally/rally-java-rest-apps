import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.client.HttpClient;
import com.rallydev.rest.request.GetRequest;
import com.rallydev.rest.request.UpdateRequest;
import com.rallydev.rest.response.GetResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import com.rallydev.rest.response.UpdateResponse;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.scheme.Scheme;


public class OnPremUpdateStory {

    public static void main(String[] args) throws URISyntaxException, IOException {
        String host = "https://xx.xx.xx.xx";
        String username = "user@company.com";
        String password = "secret";
        String applicationName = "Test updating Custom Txt fields and Descripiton On-Prem";
        RallyRestApi restApi = new RallyRestApi(new URI(host),username, password);
        restApi.setApplicationName(applicationName);
        //restApi.setWsapiVersion("1.43");
        HttpClient client = restApi.getClient();
        //Due to self-signed certificate, trust all:
        try {
            SSLSocketFactory sf = new SSLSocketFactory(new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] certificate, String authType)
                        throws CertificateException {
                    //trust all certs
                    return true;
                }
            }, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, sf));

            String workspaceRef = "/workspace/5103"; //YOUR VALID WORKSPACE OID HERE
            GetRequest getRequest = new GetRequest(workspaceRef);
            GetResponse getResponse = restApi.get(getRequest);
            System.out.println(getResponse.getObject());
            //************************UPDATE STORY**********************************************
            String ref_mod = "/hierarchicalRequirement/225154";
            JsonObject modified = new JsonObject();
            modified.addProperty("MoreNotes", "textfieldone value");
            modified.addProperty("AdditionalNotes", "textfieldtwo value");
            modified.addProperty("Description", "Description Test");

            UpdateRequest updateStoryRequest = new UpdateRequest(ref_mod,modified);
            UpdateResponse updateResponse = restApi.update(updateStoryRequest);
            if (updateResponse.wasSuccessful()) {
                System.out.println("Successfully updated story:" + ref_mod);
                String[] warningList;
                warningList = updateResponse.getWarnings();
                for (int i=0;i<warningList.length;i++) {
                    System.out.println(warningList[i]);
                }
            } else {
                System.out.println("Error occurred attempting to update story: " + ref_mod);
                String[] errorList;
                errorList = updateResponse.getErrors();
                for (int i=0;i<errorList.length;i++) {
                    System.out.println(errorList[i]);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            restApi.close();
        }
    }
}