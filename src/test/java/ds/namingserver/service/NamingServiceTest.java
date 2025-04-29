package ds.namingserver.service;


import ds.namingserver.CustomMap.LocalJsonMap;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static ds.namingserver.Config.NSConf.MAP_PATH;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = NamingService.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)  // Allows non-static @AfterAll
class NamingServiceTest {

    @Autowired
    private NamingService namingService;

    private static final String JSON_FILE_PATH_BACKUP = "src/main/resources/map_backup.json";


    @BeforeEach
    void setUp() throws IOException {
        // Backup the original JSON file
        Files.copy(Paths.get(MAP_PATH), Paths.get(JSON_FILE_PATH_BACKUP), StandardCopyOption.REPLACE_EXISTING);

        namingService.setMap(new LocalJsonMap<>(JSON_FILE_PATH_BACKUP));

        namingService.addNode("node_11111", "192.168.1.1");
        namingService.addNode("node_22222", "192.168.1.2");
        namingService.addNode("node_33333", "192.168.1.3");
        //namingService.updateJSONFromMap(testData);
    }



    @AfterEach
    void tearDown() throws IOException {
        // Delete the backup file
        Files.deleteIfExists(Paths.get(JSON_FILE_PATH_BACKUP));
    }

    @Test
    void getNodeIpFromName() {
        // Happy path
        assertEquals("192.168.1.1", namingService.getNodeIpFromName("node_11111"));

        // Alternative path
        // Get node that does not exist
        assertThrows(ResponseStatusException.class, () -> namingService.getNodeIpFromName("non_existant_node"));
    }

    @Test
    void addNode() {
        // Happy path
        namingService.addNode("node_44444", "192.168.1.4");
        assertEquals("192.168.1.4", namingService.getNodeIpFromName("node_44444"));

        // Alternative path
        // Add an already existing node
        assertThrows(ResponseStatusException.class, () -> namingService.addNode("node_11111", "any_ip"));
    }

    @Test
    void deleteNodeByName() {
        // Happy path
        namingService.deleteNodeByName("node_11111");

        // Alternative path
        // Remove a node that does not exist
        assertThrows(ResponseStatusException.class, () -> namingService.deleteNodeByName("non-existant-node"));
    }

    @AfterAll
    void restore(){
        namingService.setMap(new LocalJsonMap<>(MAP_PATH));
    }
}
