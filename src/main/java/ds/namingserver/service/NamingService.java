package ds.namingserver.service;

import ds.namingserver.Config.NSConf;
import ds.namingserver.CustomMap.LocalJsonMap;
import ds.namingserver.Multicast.MulticastListener;
import ds.namingserver.Utilities.NodeDTO;
import ds.namingserver.Utilities.Utilities;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ds.namingserver.Config.NSConf.MAP_PATH;

@Service
public class NamingService {

    private LocalJsonMap<Integer, String> map;


    private final ExecutorService multicastExecutor;
    private final MulticastListener multicastListener;

    public NamingService()
    {
        map = new LocalJsonMap<>(MAP_PATH);
        this.multicastListener = new MulticastListener(this);
        this.multicastExecutor = Executors.newSingleThreadExecutor();

    }

    @PostConstruct
    public void startMulticastListener() {
        pingMap();
        multicastExecutor.submit(multicastListener::run);
    }

    /**
     * Iterate over whole map and ping every entry to check if its on network.
     * If node not in network, delete from map.
     */
    private void pingMap() {

        Iterator<Map.Entry<Integer, String>> iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Integer, String> entry = iterator.next();
            Integer i = entry.getKey();
            String ip = entry.getValue();

            if (Objects.equals(ip, "")) {
                iterator.remove();
                continue;
            }

            String url = "http://" + ip + ":" + NSConf.NAMINGNODE_PORT + "/node/ping";
            RestTemplate restTemplate = new RestTemplate();

            try {
                String response = restTemplate.getForObject(url, String.class);
                System.out.println("Found node " + i + " active on network, response: " + response);
            } catch (Exception e) {
                iterator.remove();
                System.out.println("Node: " + i + " not on network, DELETING...");
            }
        }
        map.updateJSON();
    }



    /**
     * Returns the value associated with the node name
     * @param nodeName name of the node to fetch the IP from
     * @return IP-address
     */
    public String getNodeIpFromName(String nodeName) {
        int hash = Utilities.mapHash(nodeName);
        String value = map.get(hash);
        if (value != null) {
            return value;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Node" + nodeName + "(name) was not found");
        }
    }


    /**
     * Add node to the map and update the JSON if node does not exist
     * @param nodeName name of the node to be added
     * @param ip ip address of the node to be added
     */
    public void addNode(String nodeName, String ip) {
        int hashedName = Utilities.mapHash(nodeName);
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
        int hashedName = Utilities.mapHash(nodeName);
        if (map.containsKey(hashedName)) {
            map.remove(hashedName);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The node you are trying to remove does not exist");
        }
    }



    /**
     * Removes node and update JSON if it is found in the map
     * @param nodeId ip of the node that will be removed
     */
    public void deleteNodeById(int nodeId) {
        if (map.containsKey(nodeId)) {
            map.remove(nodeId);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The node you are trying to remove does not exist");
        }
    }


    /**
     * Method getFile
     * Gets a file from the right node and returns as ResponseEntity
     *
     *
     * @param filename the file to search for
     * @param ipOfRequester the ip of the requester
     * @return ResponseEntity with file in
     */
    public ResponseEntity<Resource> getFile(String filename, String ipOfRequester)  {

        String ip = getNodeFromFileName(filename , null);

        String mapping = "/node/file/with-requesterIP?filename=" + filename + "&requesterIP=" + ipOfRequester;

        final String uri = "http://"+ip+":"+NSConf.NAMINGNODE_PORT+ mapping;

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Resource> response = restTemplate.exchange(
                uri, HttpMethod.GET, null, Resource.class);

        //return result;
        return response;
    }


    /**
     * Method sendFile
     * Send a given file to the right node
     *
     * @param file file to send to node
     * @param ipOfRequester ip of the requester
     * @return ResponseEntity with status of the received (responses)
     */
    public ResponseEntity<String> sendFile(MultipartFile file, String ipOfRequester)  {

        //could do something with this ip -> check if legit...

        String ip = getNodeFromFileName(file.getOriginalFilename() , null);

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

    /**
     * Method getNodeFromFileName
     * Returns the id of the node that corresponds to the filename
     * Follows the specs given in the exercise
     *
     * @param filename filename to find node for
     * @param ipOfOwner ip of the owner of the file
     * @return id of node where the file belongs
     */
    public String getNodeFromFileName(String filename , String ipOfOwner){



        int hashOfFile = Utilities.mapHash(filename);

        String ipOfFoundNode = map.getPreviousWithWrap(hashOfFile);

        if (ipOfFoundNode != null) {
            //this is a bit slow but should do the trick -> future work is to convert everything to using Node classes
            if (Objects.equals(ipOfFoundNode, ipOfOwner)) {
                System.out.println("Found node (" + ipOfFoundNode + ") is the owner, finding previous for redundancy.");
                Integer ownerKey = null;

                // Find the key (node ID) associated with the owner's IP -> bad part
                for (Map.Entry<Integer, String> entry : map.entrySet()) {
                    if (Objects.equals(entry.getValue(), ipOfOwner)) {
                        ownerKey = entry.getKey();
                        break;
                    }
                }

                if (ownerKey != null) {
                    ipOfFoundNode = map.getPreviousWithWrap(ownerKey);
                    System.out.println("Redundant node found: " + ipOfFoundNode);
                } else {
                    System.out.println("Could not find owner's key in the map for redundancy.");
                    // Fallback to the initially found node (the owner) or handle differently
                }
            }
        }

        System.out.println("Node requested for Filename : " + filename + "     node found : " + ipOfFoundNode);

        return ipOfFoundNode;
    }


    public Map<Integer, String> getMap() {
        return map;
    }

    public void setMap(LocalJsonMap<Integer, String> map) {
        this.map = map;
    }

    /**
     * Method to handle the incoming multicast from nodes.
     * Put node in map if not already. And send back the number of nodes on network
     * @param requestingNodeIp Ip of node that sent the multicast.
     * @param name Name of the node that sent the multicast.
     */
    public void processIncomingMulticast(String requestingNodeIp , String name) {

        System.out.println("Got the incoming multicast" + requestingNodeIp);

        //calc hash
        int hashName = Utilities.mapHash(name);

        int numberOfNodes ;

        //add node to map, if node already in map return -1
        if(map.containsKey(hashName)) {
            numberOfNodes = -1;
            map.remove(hashName); //to be certain
            System.out.println("Node with hash already in map");
        }
        else {
            map.put(hashName, requestingNodeIp);
            numberOfNodes = map.size();
        }

        System.out.println("number of nodes : " + numberOfNodes);

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
    /**
     * Method getNextAndPrevious
     * Get the next and previous node from given node id (in map)
     *
     * @param id id of node to look for neighbors
     * @return next and previous node in map.
     */
    public Map<Integer, String> getNextAndPrevious(Integer id) {

        Map<Integer, String> nextAndPrevMap = new HashMap<>();

        int nextKey = map.getNextKeyWithWrap(id);
        int previousKey = map.getPreviousKeyWithWrap(id);

        if (nextKey == previousKey){
            nextAndPrevMap.put(nextKey,map.get(nextKey));
        }
        else {
            nextAndPrevMap.put(nextKey, map.get(nextKey));
            nextAndPrevMap.put(previousKey, map.get(previousKey));
        }

        System.out.println("Next and previous : " + nextAndPrevMap +  "   , asked of node : " + id);
        System.out.println("Full map is : " + map.toString());


        return nextAndPrevMap;

    }


    public NodeDTO getNext(int id ){
        int nextKey = map.getNextKeyWithWrap(id);
        System.out.println("Next from the map is asked from : " + id + " Supplied next : " + nextKey );

        return new NodeDTO(nextKey , map.get(nextKey));

    }

    public NodeDTO getPrevious(int id ){
        int previousKey = map.getPreviousKeyWithWrap(id);
        System.out.println("Previous from the map is asked from : " + id + " Supplied next : " + previousKey );
        return new NodeDTO(previousKey , map.get(previousKey));

    }








    public int getNumberOfNodes() {
        if (!map.isEmpty()) {
            return map.size();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "NamingServer map size is empty");
        }

    }
}
