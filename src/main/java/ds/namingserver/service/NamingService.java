package ds.namingserver.service;

import com.fasterxml.jackson.core.type.TypeReference;
import ds.namingserver.CustomMap.LocalJsonMap;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class NamingService {

    private Map<Integer, String> map;

    public NamingService(){
        map = new LocalJsonMap<>( "map.json");
    }


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
            map.put(hashedName, ip);
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
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The node you are trying to remove does not exist");
        }
    }



    public ResponseEntity<Resource> getFile(String filename)  {

        String ip = getNodeFromName(filename);

        final String uri = "http://"+ip+":8082/node/file/"+filename;

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Resource> response = restTemplate.exchange(
                uri, HttpMethod.GET, null, Resource.class);

        //return result;
        return response;
        }

    public ResponseEntity<String> sendFile(MultipartFile file)  {


        String ip = getNodeFromName(file.getName());

        final String uri = "http://"+ip+":8082/node/file/";

        // Create headers for multipart form-data
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Create a MultiValueMap to hold the file data
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());  // Use the file's resource directly

        // Create HttpEntity with body and headers
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        // Create a RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        // Send the HTTP POST request
        ResponseEntity<String> response = restTemplate.exchange(
                uri, HttpMethod.POST, entity, String.class);

        return response;

    }




    public String getNodeFromName(String filename){

        int hashOfName = mapHash(filename);


        int closest = Collections.max(new ArrayList<>(map.keySet()))  ;
        int minDifference = Math.abs(hashOfName - closest);


        for (int num : map.keySet()) {
            if (num < hashOfName) {

                int difference = Math.abs(hashOfName - num);
                if (difference < minDifference) {
                    closest = num;
                    minDifference = difference;
                }
            }
        }

        return map.get(closest);
    }


    public Map<Integer, String> getMap() {
        return map;
    }

    public void setMap(Map<Integer, String> map) {
        this.map = map;
    }
}
