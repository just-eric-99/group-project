import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.SerializationUtils;

public class GUI {
    public TextField amountInput;
    public TextField toAddressInput;
    public TextField balanceInput;
    public TextField myAddressInput;
    public TextArea log;
    public TextArea history;
    private Main main;

    public void initialize(String myAddress, double balance, Main main) {
        this.myAddressInput.setText(myAddress);
        this.balanceInput.setText(balance + "");
        this.main = main;
    }

    public void payButton(ActionEvent actionEvent) throws Exception {
        Transaction tx = main.minerWallet.pay(toAddressInput.getText(), Double.parseDouble(amountInput.getText()));
        for (int i = 3000; i < 3000 + main.portRange; i++) {
            main.broadcast(SerializationUtils.serialize(tx), i);
        }
    }

    public void mineButton(ActionEvent actionEvent) {
        main.mine();
    }

    public void appendLog(String blockString) {
        System.out.println(blockString);
        log.appendText(blockString + "\n");
    }

    public void updateBalanceInput(String balance) {
        balanceInput.setText(balance);
    }

}
