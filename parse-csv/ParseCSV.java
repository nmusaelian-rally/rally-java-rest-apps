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
import java.io.FileWriter;


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
        String csvFile = "withoutDupes.csv";
        String csvExport = "export2.csv";
        BufferedReader br = null;
        Collection<Map<String,String>> maps = new HashSet<Map<String,String>>();

        try {
            String line;
            br = new BufferedReader(new FileReader(csvFile));
            String header = br.readLine(); //leave behind
            while ((line = br.readLine()) != null) {
                System.out.println("\n raw data from csv......." + readCSVtoArrayList(line) + "\n");
                ArrayList<String> data = readCSVtoArrayList(line);
                if(data.size()>=9){
                    HashMap map = new HashMap();
                    String userEmail = data.get(2);
                    String projectName = data.get(3);
                    String artifactName = data.get(7);
                    String artifactType = data.get(data.size()-1); //in case of overflow
                    String state = getArtifactState(restApi, artifactName,artifactType);
                    map.put("UserEmail", userEmail);
                    map.put("ProjectName", projectName);
                    map.put("ArtifactName", artifactName);
                    map.put("ArtifactType", artifactType);
                    map.put("State", state);

                    maps.add(map);
                    FileWriter writer = new FileWriter(csvExport);
                    for (Map<String, String> m : maps) {
                        for (Map.Entry<String, String> entry : m.entrySet()) {
                            writer.append(entry.getValue());
                            writer.append(',');
                        }
                        writer.append('\n');
                    }
                    writer.flush();
                    writer.close();
                }
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

    public static String getArtifactState(RallyRestApi rally, String artifactName, String artifactType) throws URISyntaxException, IOException{
        String type = "";
        String field = "";
        String state = "";
        String workspaceRef = "/workspace/12345";
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