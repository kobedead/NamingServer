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

    public int mapHash(String text) {
        int hashCode = text.hashCode();
        int max = Integer.MAX_VALUE;
        int min = Integer.MIN_VALUE;
        
        // Ensure the hashCode is always positive
        int adjustedHash = Math.abs(hashCode);
        
        // Mapping hashCode from (Integer.MIN_VALUE, Integer.MAX_VALUE) to (0, 32768)
        return (int) (((long) adjustedHash * 32768) / ((long) max - min));
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
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File("map.json"), map);
            System.out.println("Map saved to map.json successfully!");
        } catch (IOException e) {
            //
        }
    }
}
