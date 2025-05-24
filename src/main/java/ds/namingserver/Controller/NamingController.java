package ds.namingserver.Controller;

import ds.namingserver.Model.AddNodeDTO;
import ds.namingserver.service.NamingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/namingserver")
public class NamingController {

    Logger logger = Logger.getLogger(NamingController.class.getName());


    private final NamingService namingservice;

    @Autowired
    public NamingController(NamingService namingservice) {
        this.namingservice = namingservice;
    }



    /**
     * Get ip of node from name
     *
     * @param name name of the node you want to receive ip from
     * @return The ip of the node
     */
    @GetMapping("/node/by-name/{name}")
    public String getNodeIP(@PathVariable String name) {
        String ip = namingservice.getNodeIpFromName(name);
        logger.info("Called get Ip for node : " + name + "(name) with ip : " + ip );
        return ip;
    }



    /**
     * remove Node from server
     *
     * @param id of Node
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/node/by-id/{id}")
    public ResponseEntity<String> removeNodeById(@PathVariable Integer id) {
        namingservice.deleteNodeById(id);
        logger.info("Node removed from Server, ID = "+ id);
        return ResponseEntity.ok("Node removed successfully");
    }



    @GetMapping("/node/nextAndPrevious/{id}")
    public ResponseEntity<Map<Integer, String>> getNextAndPrevious(@PathVariable Integer id) {
        Map<Integer, String> nextAndPreviousMap = namingservice.getNextAndPrevious(id);
        return new ResponseEntity<>(nextAndPreviousMap, HttpStatus.OK);
    }



    /**
     * Gets filename and sends ip back of node it belongs to
     *
     * @param filename file
     * @return the file
     */
    @GetMapping("/node/by-filename/{filename}")
    public ResponseEntity getIpOfNodeFromFile(@PathVariable("filename") String filename) {
        String IPofNode = namingservice.getNodeFromFileName(filename , null);
        ResponseEntity<String> ip = new ResponseEntity<>(IPofNode , HttpStatus.OK);

        return ip;
    }


    /**
     * Gets filename and sends ip back of node it belongs to
     *
     * @param filename file
     * @param ownerIp ip of owner
     * @return the file
     */
    @GetMapping("/node/by-filename")
    public ResponseEntity getIpOfNodeFromFileWithOwner(@RequestParam String filename, @RequestParam String ownerIp) {
        String IPofNode = namingservice.getNodeFromFileName(filename , ownerIp);
        ResponseEntity<String> ip = new ResponseEntity<>(IPofNode , HttpStatus.OK);

        return ip;
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
     * @param  file file to upload
     * @return the file
     */
    @PostMapping("/file")
    public ResponseEntity uploadFile(@RequestBody MultipartFile file ) throws IOException {
        return namingservice.sendFile(file);
    }






















}