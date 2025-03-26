package ds.namingserver.Controller;

import ds.namingserver.Model.AddNodeDTO;
import ds.namingserver.service.NamingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

@RestController
@RequestMapping("/namingserver")
public class NamingController {

    private final NamingService namingservice;

    Logger logger = Logger.getLogger(NamingController.class.getName());

    @Autowired
    public NamingController(NamingService namingservice) {
        this.namingservice = namingservice;
    }


    /**
     * Get ip of node form name
     *
     * @param name name of the node you want to receive ip from
     * @return The ip of the node
     */
    @GetMapping("/node/{name}")
    public String getNodeIP(@PathVariable String name) {
        String ip = namingservice.getNode(name);
        logger.info("Called get Ip for node : " + name + "with ip : " + ip );
        return ip;
    }


    /**
     * Add Node to server
     *
     * @param addNodeDTO DTO with name and ip of node
     * @return the ResponseEntity with status 200 (OK)
     */
    @PostMapping("/node")
    public ResponseEntity<String> addNode(@RequestBody AddNodeDTO addNodeDTO) {
        namingservice.addNode(addNodeDTO.getName() , addNodeDTO.getIp());
        logger.info("Node added to Server, Name = "+ addNodeDTO.getName() + "Ip = "+addNodeDTO.getIp() );
        return ResponseEntity.ok("Node added to Server");
    }

    /**
     * remove Node from server
     *
     * @param name name of Node
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/node/{name}")
    public ResponseEntity<String> removeNode(@PathVariable String name) {
        namingservice.deleteNode(name);
        logger.info("Node removed from Server, Name = "+ name);
        return ResponseEntity.ok("Node removed successfully");
    }


    /**
     * Get file from correct node from name of file
     *
     * @param filename name of the file
     * @return the file
     */
    @GetMapping("/file/{filename}")
    public ResponseEntity downloadFile(@PathVariable("filename") String filename) throws IOException {

        // Get file instance from the service
        ResponseEntity<Resource> resource= namingservice.getFile(filename);
        return resource;

    }


    /**
     * send file to correct node from name of file
     *
     * @param filename name of the file
     * @return the file
     */
    @PostMapping("/file/")
    public ResponseEntity uploadFile(@PathVariable("filename") String filename) throws IOException {

        //needs a file as input probabily

        return namingservice.sendFile(filename);

    }






}