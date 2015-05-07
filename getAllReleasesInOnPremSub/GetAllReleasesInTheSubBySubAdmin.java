import java.util.ArrayList;
import com.google.gson.JsonArray;
import java.util.List;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.client.HttpClient;
import com.rallydev.rest.request.GetRequest;
import com.rallydev.rest.response.GetResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.scheme.Scheme;


public class GetAllReleasesInTheSubBySubAdmin {

    public static void main(String[] args) throws URISyntaxException, IOException {

        String host = "https://xx.xx.xx.xx";
        String username = "admin@co.com";
        String password = "secret";
        String applicationName = "Get Releases On-Prem";
        RallyRestApi restApi = new RallyRestApi(new URI(host),username, password);
        restApi.setApplicationName(applicationName);
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

            GetRequest getRequest = new GetRequest("/user");
            GetResponse getResponse = restApi.get(getRequest);
            JsonObject currentUser = getResponse.getObject();
            String currentUserRef = currentUser.get("_ref").getAsString();
            String currentUserName = currentUser.get("_refObjectName").getAsString();
            System.out.println("current user " + " " + currentUserName + " " + currentUserRef);

            List<String> workspaces = new ArrayList<String>();
            GetRequest getSubscriptionRequest = new GetRequest("/subscription");
            getSubscriptionRequest.setFetch(new Fetch("Workspaces"));
            GetResponse getSubscriptionResponse = restApi.get(getSubscriptionRequest);
            JsonObject subJsonObject = getSubscriptionResponse.getObject();
            String subRef = subJsonObject.get("_ref").getAsString();
            System.out.println("subscription " + " " + subRef);
            int numberOfWorkspaces = subJsonObject.getAsJsonObject("Workspaces").get("Count").getAsInt();
            System.out.println("number of workspaces in the sub " + " " + numberOfWorkspaces);
            if(numberOfWorkspaces > 0) {
                QueryRequest workspaceRequest = new QueryRequest(subJsonObject.getAsJsonObject("Workspaces"));
                workspaceRequest.setFetch(new Fetch("Name","ObjectID","State"));
                JsonArray workspacesInSub = restApi.query(workspaceRequest).getResults();

                for (int i=0;i<numberOfWorkspaces;i++){
                    System.out.println("Name: " + workspacesInSub.get(i).getAsJsonObject().get("Name")
                            + " OID: " + workspacesInSub.get(i).getAsJsonObject().get("ObjectID").getAsString()
                            + " State:" + workspacesInSub.get(i).getAsJsonObject().get("State").getAsString());
                    workspaces.add(workspacesInSub.get(i).getAsJsonObject().get("ObjectID").getAsString());

                }
            }

            System.out.println("workspaces: " + workspaces);

            class MyRunnable implements Runnable {
                private String oid;

                public MyRunnable(String oid) {
                    this.oid = oid;

                }

                public void run() {
                    try{
                        getRally(this.oid);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            Thread [] threads = new Thread[numberOfWorkspaces];
            for (int i = 0; i < threads.length; i++)
            {
                threads[i] = new Thread(new MyRunnable(workspaces.get(i)));
                threads[i].start();
                threads[i].join();
            }
            if (restApi != null) {
                restApi.close();
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            restApi.close();
        }
    }
    private static void getRally(String oid) throws Exception
    {

        String host = "https://xx.xx.xx.xx";
        String username = "admin@co.com";
        String password = "secret";
        String applicationName = "Get Releases On-Prem";
        RallyRestApi restApi = new RallyRestApi(new URI(host),username, password);
        restApi.setApplicationName(applicationName);
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

            String workspaceRef = "/workspace/" + oid;
            QueryRequest  releaseRequest = new QueryRequest("Release");
            releaseRequest.setFetch(new Fetch("Name","ObjectID","ReleaseStartDate","ReleaseDate", "Project","RevisionHistory","Revisions"));
            releaseRequest.setWorkspace(workspaceRef);
            QueryResponse releaseQueryResponse = restApi.query(releaseRequest);
            int numberOfReleases = releaseQueryResponse.getTotalResultCount();
            System.out.println("\n########## workspace : " + oid +  " ###########");
            System.out.println("number of releases in workspace : " + oid +  " : "+ numberOfReleases);
            if(numberOfReleases >0)
            {
                for (int i=0;i<numberOfReleases;i++) {
                    JsonObject releaseJsonObject = releaseQueryResponse.getResults().get(i).getAsJsonObject();
                    System.out.println(releaseJsonObject.get("Name") + " : " + releaseJsonObject.get("ObjectID") + " in project " + releaseJsonObject.get("Project").getAsJsonObject().get("Name"));
                    String rsd = releaseJsonObject.get("ReleaseStartDate").getAsString();
                    String rd = releaseJsonObject.get("ReleaseDate").getAsString();
                    System.out.println("ReleaseStartDate: " + rsd + " ReleaseDate: " + rd);
                    System.out.println("-------------------");
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            restApi.close();
        }
    }
}