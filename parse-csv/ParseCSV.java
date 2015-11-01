import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collection;


public class ParseCSV {
    public static void main(String[] args) throws URISyntaxException, IOException {
        String host = "https://rally1.rallydev.com";
        String username = "user@co.com";
        String password = "secret";
        String applicationName = "example";

        RallyRestApi restApi = new RallyRestApi(
                new URI(host),
                username,
                password);
        restApi.setApplicationName(applicationName);
        System.out.println(restApi.getWsapiVersion());
        String csvFile = "Preswitch_owner_data_adp2.csv";
        BufferedReader br = null;
        Collection<Map<String,String>> maps = new HashSet<Map<String,String>>();

        try {
            String line;
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                System.out.println("raw data from csv......." + readCSVtoArrayList(line) + "\n");
                ArrayList<String> data = readCSVtoArrayList(line);
                HashMap map = new HashMap();
                String artifactName = "";
                String artifactType = "";
                for(int i=0; i<data.size(); i++) {
                    switch(i){
                        case 2: map.put("UserEmail", data.get(i));
                            System.out.println("UserEmail: " + data.get(i));
                            break;
                        case 3: map.put("ProjectName", data.get(i));
                            System.out.println("ProjectName: " + data.get(i));
                            break;
                        case 7: map.put("ArtifactName", data.get(i));
                            artifactName = data.get(i);
                            System.out.println("ArtifactName: " + artifactName);
                            break;
                        case 8: map.put("ArtifactType", data.get(i));
                            artifactType = data.get(i);
                            System.out.println("ArtifactType: " + artifactType);
                            break;
                    }
                }
                String state = getRallyData(restApi, artifactName,artifactType);
                System.out.println("state: " + state);
                maps.add(map);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException myException) {
                myException.printStackTrace();
            }
        }
    }
    public static ArrayList<String> readCSVtoArrayList(String s) {
        ArrayList<String> result = new ArrayList<String>();

        if (s != null) {
            String[] splitData = s.split("\\s*,\\s*");
            for (int i = 0; i < splitData.length; i++) {
                if (!(splitData[i] == null) || !(splitData[i].length() == 0)) {
                    result.add(splitData[i].trim());
                }
            }
        }

        return result;
    }

    public static String getRallyData(RallyRestApi rally, String artifactName, String artifactType) throws URISyntaxException, IOException{
        String type = "";
        String field = "";
        String state = "";
        String workspaceRef = "/workspace/123";
        if(artifactType.equals("USER STORY")){
            type = "HierarchicalRequirement";
            field = "ScheduleState";
        }
        else if(artifactType.equals("TASK")){
            type = "Task";
            field = "State";
        }
        else if(artifactType.equals("DEFECT")){
            type = "Defect";
            field = "ScheduleState";
        }
        QueryRequest  request = new QueryRequest(type);
        request.setFetch(new Fetch(field));
        request.setWorkspace(workspaceRef);
        request.setQueryFilter(new QueryFilter("Name", "=", artifactName));

        QueryResponse response = rally.query(request);
        if(response.getTotalResultCount() > 0){
            JsonObject obj = response.getResults().get(0).getAsJsonObject();
            state = obj.get(field).getAsString();
        }
        return state;
    }
}