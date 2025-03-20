package ds.namingserver.Controller;

import ds.namingserver.Model.AddNodeDTO;
import ds.namingserver.service.NamingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
        //String ip = namingservice.getNodeIP();
        //logger.info("Called get Ip for node : " + name + "with ip : " + ip );
        //return ip;
        return null;
    }


    /**
     * Add Node to server
     *
     * @param addNodeDTO DTO with name and ip of node
     * @return the ResponseEntity with status 200 (OK)
     */
    @PutMapping("/node")
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
        namingservice.removeNode(name);
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
    public ResponseEntity downloadFile(@PathVariable("filename") String filename) throws FileNotFoundException {

        // Get file instance from the service
        File file= namingservice.getFile(filename);

        // Creating a new InputStreamResource object
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        // Creating a new instance of HttpHeaders Object
        HttpHeaders headers = new HttpHeaders();

        // Setting up values for contentType and headerValue
        String contentType = "application/octet-stream";
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);

    }






}