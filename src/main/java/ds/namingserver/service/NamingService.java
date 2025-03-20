package ds.namingserver.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
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
