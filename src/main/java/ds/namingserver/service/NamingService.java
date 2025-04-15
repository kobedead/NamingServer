package ds.namingserver.service;

import ds.namingserver.Config.NSConf;
import ds.namingserver.CustomMap.LocalJsonMap;
import ds.namingserver.Multicast.MulticastListener;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class NamingService {

    private LocalJsonMap<Integer, String> map;

    public final String MAP_PATH = "src/main/resources/map.json";

    private final ExecutorService multicastExecutor;
    private MulticastListener multicastListener;

    public NamingService()
    {
        map = new LocalJsonMap<>(MAP_PATH);
        this.multicastListener = new MulticastListener(this);
        this.multicastExecutor = Executors.newSingleThreadExecutor();

    }

    @PostConstruct
    public void startMulticastListener() {
        multicastExecutor.submit(multicastListener::run);
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
     * @param nodeName name of the node to fetch the IP from
     * @return IP-address
     */
    public String getNodeIpFromName(String nodeName) {
        int hash = mapHash(nodeName);
        String value = map.get(hash);
        if (value != null) {
            return value;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Node" + nodeName + "(name) was not found");
        }
    }

    /**
     * Returns the value associated with the node id
     * @param nodeId name of the node to fetch the IP from
     * @return IP-address
     */
    public String getNodeIpFromId(int nodeId) {
        String value = map.get(nodeId);
        if (value != null) {
            return value;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Node" + nodeId + "(ID) was not found");
        }
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
    public void deleteNodeByName(String nodeName) {
        int hashedName = mapHash(nodeName);
        if (map.containsKey(hashedName)) {
            map.remove(hashedName);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The node you are trying to remove does not exist");
        }
    }

    /**
     * Removes node and update JSON if it is found in the map
     * @param nodeIp ip of the node that will be removed
     */
    public void deleteNodeByIp(String nodeIp) {
        if (map.containsValue(nodeIp)) {
            map.removeValue(nodeIp);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The node you are trying to remove does not exist");
        }
    }



    public ResponseEntity<Resource> getFile(String filename)  {

        String ip = getNodeFromName(filename);

        final String uri = "http://"+ip+":"+NSConf.NAMINGNODE_PORT+"/node/file/"+filename;

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Resource> response = restTemplate.exchange(
                uri, HttpMethod.GET, null, Resource.class);

        //return result;
        return response;
        }

    public ResponseEntity<String> sendFile(MultipartFile file)  {


        String ip = getNodeFromName(file.getOriginalFilename());

        final String uri = "http://"+ip+":"+NSConf.NAMINGNODE_PORT+"/node/file";

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

        System.out.println("Begin debug");

        System.out.println(filename);
        System.out.println(hashOfName);
        System.out.println("hashes in map : ");
        map.keySet().forEach(System.out::println);


        Integer closest = null;
        int minDifference = Integer.MAX_VALUE;

        for (int num : map.keySet()) {
            if (num < hashOfName) { // Ensure we only consider smaller values
                int difference = hashOfName - num; // Difference should be positive
                if (difference < minDifference) {
                    closest = num;
                    minDifference = difference;
                }
            }
        }

        // If no smaller key found, get the largest key
        if (closest == null) {
            closest = Collections.max(map.keySet());
        }


        System.out.println("Found hash : " + closest);
        System.out.println("Found Ip : " +  map.get(closest));

        System.out.println("End debug");


        return map.get(closest);
    }


    public Map<Integer, String> getMap() {
        return map;
    }

    public void setMap(LocalJsonMap<Integer, String> map) {
        this.map = map;
    }


    public void processIncomingMulticast(String requestingNodeIp , String name) {

        System.out.println("Got the incoming multicast" + requestingNodeIp);

        //calc hash
        int hashName = mapHash(name);

        //add node to map
        map.put(hashName , requestingNodeIp);

        //return number of nodes on network (in map)
        int numberOfNodes = map.size();

        System.out.println("number of nodes : " + numberOfNodes);
        System.out.println("test");

        final String uri = "http://"+ requestingNodeIp +":"+ NSConf.NAMINGNODE_PORT +"/node/size";

        System.out.println("Call to " + uri);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<Integer> requestEntity = new HttpEntity<>(numberOfNodes, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
            System.out.println("send");
            // Print response
            System.out.println("Response: " + response.getBody());
        } catch (Exception e) {
            System.out.println("Error processing incoming multicast" + e.getMessage());
        }

    }

    public Map<Integer, String> getNextAndPrevious(String ip) {
        Integer hash = map.entrySet().stream()
                .filter(entry -> Objects.equals(entry.getValue(), ip))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (hash == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The node does not exist");
        }

        int diff1 = Integer.MIN_VALUE;
        int diff2 = Integer.MAX_VALUE;
        Map<Integer, String> nextAndPrevMap = new HashMap<>();

        int previousHash = -10;
        String previousIp = "";
        int nextHash = -10;
        String nextIp = "";


        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            // Received hash is equal to one found in the map
             if (entry.getKey() > hash) { // Received hash is lower than one iterated over in map
                if (hash - entry.getKey() > diff1) {
                    diff1 = entry.getKey() - hash;
                    previousHash = entry.getKey();
                    previousIp = entry.getValue();
                }
            }

             if (entry.getKey() < hash) { // Received hash is greater than one iterated over in map
                if (hash - entry.getKey() < diff2) {
                    diff2 = entry.getKey() - hash;
                    nextHash = entry.getKey();
                    nextIp = entry.getValue();
                }
            }
        }

        Map<Integer, String> copiedMap = new HashMap<>(nextAndPrevMap);
        copiedMap.remove(hash);

        // There is no lower previousHash
        if (previousHash == -10) {
            // Take the max hash
            previousHash = copiedMap.keySet().stream().max(Integer::compareTo).orElseThrow();
            previousIp = copiedMap.get(previousHash);
        }

        // There is no greater nextHash
        if (nextHash == -10) {
            // Take the min hash
            nextHash = copiedMap.keySet().stream().min(Integer::compareTo).orElseThrow();
            nextIp = copiedMap.get(nextHash);
        }

        nextAndPrevMap.put(previousHash, previousIp);
        nextAndPrevMap.put(nextHash, nextIp);
        return nextAndPrevMap;
    }
}
