package ds.namingserver.CustomMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LocalJsonMap<K, V> extends HashMap<K, V> {


    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String FILE_PATH;

    public LocalJsonMap(String filepath) {
        this.FILE_PATH = filepath;
        Map<K, V> loadedMap = getMapFromJSON();
        super.putAll(loadedMap);
    }

    @Override
    public V put(K key, V value) {
        V result = super.put(key, value);
        updateJSON();
        return result;
    }


    @Override
    public V remove(Object key) {
        V result = super.remove(key);
        updateJSON();
        return result;
    }

    public void removeValue(Object value) {
        super.values().remove(value);
        updateJSON();
    }



    /**
     * Update the JSON file with the current objects that are stored in memory in map
     */
    public void updateJSON() {
        File file = new File(FILE_PATH);
        try {
            // Write updated data back to the file
            objectMapper.writeValue(file, this);
            System.out.println("Map updated and saved to map.json successfully!");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error writing to JSON", e);
        }
    }




    /**
     * Get all key value pairs contained in the Map.JSON file.
     * @return all of the key value pairs from the JSON "database"
     */
    public  Map<K, V> getMapFromJSON() {
        File file = new File(FILE_PATH);  // Assuming FILE_PATH is defined as "map.json"
        try {
            // Check if the file exists and has content
            if (file.exists() && file.length() > 0) {
                // Read the content from the file and map it to a Map<Integer, String>
                return (Map<K, V>) objectMapper.readValue(file, new TypeReference<Map<Integer, String>>() {});
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
