package ds.namingserver.CustomMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.source.tree.Tree;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class LocalJsonMap<K extends Comparable<K>, V> extends TreeMap<K, V> {

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
            objectMapper.writeValue(file, this); // 'this' is now a sorted TreeMap
            System.out.println("Sorted map updated and saved to map.json successfully! Map size is now " + size());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error writing to JSON", e);
        }
    }

    /**
     * Get all key value pairs contained in the Map.JSON file.
     * @return all of the key value pairs from the JSON "database"
     */
    public TreeMap<K, V> getMapFromJSON() {
        File file = new File(FILE_PATH);  // Assuming FILE_PATH is defined
        try {
            // Check if the file exists and has content
            if (file.exists() && file.length() > 0) {
                // Read the content from the file and map it to a TreeMap
                return (TreeMap<K, V>) objectMapper.readValue(file, new TypeReference<TreeMap<Integer, String>>() {});
            } else {
                // Return an empty TreeMap if the file is empty or doesn't exist
                return new TreeMap<>();
            }
        } catch (IOException e) {
            // Handle any IO exceptions (e.g., file read issues)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading JSON file", e);
        }
    }

    public V getNextByKey(K key) {
        K nextKey = this.higherKey(key);
        return nextKey != null ? this.get(nextKey) : null;
    }

    public V getPreviousByKey(K key) {
        K previousKey = this.lowerKey(key);
        return previousKey != null ? this.get(previousKey) : null;
    }

    public V getNextWithWrap(K key) {
        K nextKey = this.higherKey(key);
        if (nextKey != null) {
            return this.get(nextKey);
        } else {
            // Wrap around to the first (smallest) key
            if (!isEmpty()) {
                return this.get(this.firstKey());
            } else {
                return null; // Or throw an exception if the map is empty
            }
        }
    }

    public V getPreviousWithWrap(K key) {
        K previousKey = this.lowerKey(key);
        if (previousKey != null) {
            return this.get(previousKey);
        } else {
            // Wrap around to the last (largest) key
            System.out.println("Wrap around performed");
            if (!isEmpty()) {
                return this.get(this.lastKey());
            } else {
                System.out.println("List is empty");
                return null; // Or throw an exception if the map is empty
            }
        }
    }


    public K getNextKeyWithWrap(K key) {
        K nextKey = this.higherKey(key);
        if (nextKey != null) {
            return nextKey;
        } else {
            // Wrap around to the first (smallest) key
            if (!isEmpty()) {
                return this.firstKey();
            } else {
                return null; // Or throw an exception if the map is empty
            }
        }
    }

    public K getPreviousKeyWithWrap(K key) {
        K previousKey = this.lowerKey(key);
        if (previousKey != null) {
            return previousKey;
        } else {
            // Wrap around to the last (largest) key
            if (!isEmpty()) {
                return this.lastKey();
            } else {
                return null; // Or throw an exception if the map is empty
            }
        }
    }

}
