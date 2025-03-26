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
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NamingServiceTest {

    @Autowired
    private NamingService namingService;

    private static final String JSON_FILE_PATH = "src/main/resources/map.json";
    private static final String JSON_FILE_PATH_BACKUP = "src/main/resources/map_backup.json";


    @BeforeEach
    void setUp() throws IOException {
        // Backup the original JSON file
        Files.copy(Paths.get(JSON_FILE_PATH), Paths.get(JSON_FILE_PATH_BACKUP), StandardCopyOption.REPLACE_EXISTING);

        namingService.setMap(new LocalJsonMap<>(JSON_FILE_PATH_BACKUP));

        namingService.addNode("node_11111", "192.168.1.1");
        namingService.addNode("node_22222", "192.168.1.2");
        namingService.addNode("node_33333", "192.168.1.3");
        //namingService.updateJSONFromMap(testData);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Restore the original JSON file
        //Files.copy(Paths.get(JSON_FILE_PATH_BACKUP), Paths.get(JSON_FILE_PATH), StandardCopyOption.REPLACE_EXISTING);

        // Delete the backup file
        Files.deleteIfExists(Paths.get(JSON_FILE_PATH_BACKUP));
    }

    @Test
    void getNode() {
        // Happy path
        assertEquals("192.168.1.1", namingService.getNode("node_11111"));

        // Alternative path
        // Get node that does not exist
        assertThrows(ResponseStatusException.class, () -> namingService.getNode("non_existant_node"));
    }

    @Test
    void addNode() {
        // Happy path
        namingService.addNode("node_44444", "192.168.1.4");
        assertEquals("192.168.1.4", namingService.getNode("node_44444"));

        // Alternative path
        // Add an already existing node
        assertThrows(ResponseStatusException.class, () -> namingService.addNode("node_11111", "any_ip"));
    }

    @Test
    void deleteNode() {
        // Happy path
        namingService.deleteNode("node_11111");

        // Alternative path
        // Remove a node that does not exist
        assertThrows(ResponseStatusException.class, () -> namingService.deleteNode("non-existant-node"));
    }
}
