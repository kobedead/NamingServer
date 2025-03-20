package ds.namingserver.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
class NamingServiceTest {
    @Autowired
    private NamingService namingService;

    @Test
    public void testNamingService() {
        namingService.addNode("testNode1", "192.168.1.1");
        namingService.addNode("node", "192.168.1.2");
        namingService.addNode("adzadazd", "192.168.1.3");
    }


    @Test
    public void testReadNodes() {
        Map<Integer, String> map = namingService.getMapFromJSON();
        System.out.println("Keys");
        map.keySet().forEach(System.out::println);
        System.out.println("Values");
        map.values().forEach(System.out::println);
    }
}