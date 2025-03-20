package ds.namingserver.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;


@RestController
@RequestMapping("/bank")
public class NamingController {

    private final NamingService namingService;

    Logger logger = Logger.getLogger(BankController.class.getName());

    @Autowired
    public BankController(BankService bankService) {
        this.bankService = bankService;
    }


    /**
     * Get balance from account
     *
     * @param email     the email of the person accessing the account
     * @param accountID the id of the bank account the person wants to access
     * @return the ResponseEntity with status 200 (OK) and with body of the list of products
     */
    @GetMapping("/{email}/account/{accountID}")
    public Double getBalance(@PathVariable String email, @PathVariable Long accountID) {
        Double balance = bankService.getBalance(email, accountID);
        logger.info("get" + balance);
        return balance;

    }


    /**
     * Add money to account
     *
     * @param email     the email of the person accessing the account
     * @param accountID the id of the bank account the person wants to access
     * @param money     money to add to the account balance
     * @return the ResponseEntity with status 200 (OK) and with body of the product, or with status 404 (Not Found) if the product does not exist
     */
    @PutMapping("/{email}/account/{accountID}/addBalance")
    public ResponseEntity<String> addBalance(@PathVariable String email, @PathVariable Long accountID, @RequestBody Double money) {
        Double updatedBalance = bankService.addBalance(email, accountID, money);
        logger.info("put(add)" + updatedBalance);

        return ResponseEntity.ok("money added succesfully");
    }

    /**
     * remove money to account
     *
     * @param email     the email of the person accessing the account
     * @param accountID the id of the bank account the person wants to access
     * @param money     money to remove from the account balance
     * @return the ResponseEntity with status 200 (OK) and with body of the product, or with status 404 (Not Found) if the product does not exist
     */
    @PutMapping("/{email}/account/{accountID}/removeBalance")
    public ResponseEntity<String> removeBalance(@PathVariable String email, @PathVariable Long accountID, @RequestBody Double money) {
        Double updatedBalance = bankService.removeBalance(email, accountID, money);
        logger.info("put(remove)" + updatedBalance);

        return ResponseEntity.ok("money removed succesfully");
    }


    @DeleteMapping("/{email}/account/{accountID}")
    public ResponseEntity<String> deleteBalance(@PathVariable String email, @PathVariable Long accountID) {
        Double updatedBalance = bankService.deleteBalance(email, accountID);
        logger.info("delete" + updatedBalance);

        return ResponseEntity.ok("money deleted succesfully");
    }


}
