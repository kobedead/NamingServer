package ds.namingserver.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class NamingService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String FILE_PATH = "map.json";
    private Map<Integer, String> map = new HashMap<>();

    /**
     * Hashing function to hash incoming names (based on given hashing algorithm)
     * @param text name of the node or file to be hashed
     * @return hashed integer value
     */
    public int mapHash(String text) {
        int hashCode = text.hashCode();
        int max = Integer.MAX_VALUE;
        int min = Integer.MIN_VALUE;
        
        // Ensure the hashCode is always positive
        int adjustedHash = Math.abs(hashCode);
        
        // Mapping hashCode from (Integer.MIN_VALUE, Integer.MAX_VALUE) to (0, 32768)
        return (int) (((long) adjustedHash * 32768) / ((long) max - min));
    }

    /**
     * Returns the value associated with the node name
     * @param nodename name of the node to fetch the IP from
     * @return IP-address
     */
    public String getNode(String nodename) {
        int hash = mapHash(nodename);
        return map.get(hash);
    }

    /**
     * Add node to the map and update the JSON if node does not exist
     * @param nodeName name of the node to be added
     * @param ip ip address of the node to be added
     */
    public void addNode(String nodeName, String ip) {
        int hashedName = mapHash(nodeName);
        System.out.println(hashedName);
        if (map.containsKey(hashedName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This node already exists");
        } else {
            map.put(mapHash(nodeName), ip);
            updateJSON();
        }
    }

    /**
     * Removes node and update JSON if it is found in the map
     * @param nodeName name of the node that will be removed
     */
    public void deleteNode(String nodeName) {
        int hashedName = mapHash(nodeName);
        if (map.containsKey(hashedName)) {
            map.remove(hashedName);
            updateJSON();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The node you are trying to remove does not exist");
        }
    }

    /**
     * Update the JSON file with the current objects that are stored in memory in map
     */
    public void updateJSON() {
        File file = new File(FILE_PATH);
        try {
            // Read existing data from JSON file if it exists and is not empty
            Map<Integer, String> existingData = getMapFromJSON(); // Use the getMapFromJSON method

            // Merge existing data with the new data in memory
            existingData.putAll(map);

            // Write updated data back to the file
            objectMapper.writeValue(file, existingData);
            System.out.println("Map updated and saved to map.json successfully!");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error writing to JSON", e);
        }
    }

    public ResponseEntity<Resource> getFile(String filename) throws IOException {

        String ip = getNodeFromName(filename);

        final String uri = "http://localhost:8082/node/file/"+filename;

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Resource> response = restTemplate.exchange(
                uri, HttpMethod.GET, null, Resource.class);

        //return result;
        return response;
        }

    public ResponseEntity<Resource> sendFile(String filename) throws IOException {

        // Checking whether the file requested for download exists or not
        //String fileUploadpath = System.getProperty("user.dir") +"/Uploads";
        //String[] filenames = this.getFiles();
        //boolean contains = Arrays.asList(filenames).contains(filename);
        //if(!contains) {
        //    return new ResponseEntity("FIle Not Found",HttpStatus.NOT_FOUND);
        //}

        // Setting up the filepath
        String filePath = "D:/schoolshit/6_DS/Lab3_node/NamingNote/yea";

        // Creating new file instance
        File file= new File(filePath);
        // Creating a new InputStreamResource object
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        // Creating a new instance of HttpHeaders Object
        HttpHeaders headers = new HttpHeaders();

        // Setting up values for contentType and headerValue
        String contentType = "application/octet-stream";
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);

    }






    public String getNodeFromName(String filename){

        int hashOfName = mapHash(filename);
        Map<Integer, String> nodeMap = getMapFromJSON();

        int closest = nodeMap.keySet().iterator().next();
        int minDifference = Math.abs(hashOfName - closest);


        for (int num : nodeMap.keySet()) {
            int difference = Math.abs(hashOfName - num);
            if (difference < minDifference) {
                closest = num;
                minDifference = difference;
            }
        }
        return nodeMap.get(closest);
    }







    /**
     * Get all key value pairs contained in the Map.JSON file.
     * @return all of the key value pairs from the JSON "database"
     */
    public Map<Integer, String> getMapFromJSON() {
        File file = new File(FILE_PATH);  // Assuming FILE_PATH is defined as "map.json"
        try {
            // Check if the file exists and has content
            if (file.exists() && file.length() > 0) {
                // Read the content from the file and map it to a Map<Integer, String>
                return objectMapper.readValue(file, new TypeReference<Map<Integer, String>>() {});
            } else {
                // Return an empty map if the file is empty or doesn't exist
                return new HashMap<>();
            }
        } catch (IOException e) {
            // Handle any IO exceptions (e.g., file read issues)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading JSON file", e);
        }
    }


}
