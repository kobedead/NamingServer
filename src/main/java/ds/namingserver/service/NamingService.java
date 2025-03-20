package ds.namingserver.service;

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
    Map<Integer, String> map = new HashMap<>();

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

    /// Function to write map to JSON
    public void updateJSON() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File("map.json"), map);
            System.out.println("Map saved to map.json successfully!");
        } catch (IOException e) {
            //
        }
    }



}
