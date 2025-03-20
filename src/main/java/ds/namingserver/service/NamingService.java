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

import static java.lang.Math.abs;

@Service
public class NamingService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String FILE_PATH = "map.json";
    private Map<Integer, String> map = new HashMap<>();

    /// Hashing function to hash incoming names (based on given hashing algorithm)
    public int mapHash(String name) {
        int JAVA_HASH_MAX = Integer.MAX_VALUE;
        int JAVA_HASH_MIN = Integer.MIN_VALUE;
        int NEW_MAX = 32768;

        int hashCode = name.hashCode();
        return (int) (((long) (hashCode - JAVA_HASH_MIN) * NEW_MAX) / ((long) JAVA_HASH_MAX - JAVA_HASH_MIN));
    }

    /// Function to add new node (validate if node already exists)
    public void addNode(String nodeName, String ip) {
        int hashedName = mapHash(nodeName);
        if (map.containsKey(hashedName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This node already exists");
        } else {
            map.put(mapHash(nodeName), ip);
            updateJSON();
        }
    }

    ///  Function to remove node (validate if node does not exist)
    public void removeNode(String nodeName) {
        int hashedName = mapHash(nodeName);
        if (map.containsKey(hashedName)) {
            map.remove(hashedName);
            updateJSON();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The node you are trying to remove does not exist");
        }
    }

    /// Function to write map to JSON
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
