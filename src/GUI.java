import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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

    public void payButton(ActionEvent actionEvent) {
        main.minerWallet.pay(toAddressInput.toString(), Double.parseDouble(amountInput.toString()));
    }

    public void mineButton(ActionEvent actionEvent) {
        main.mine();
    }

    public void appendLog(String blockString) {
        System.out.println(blockString);
        log.appendText(blockString + "\n");
    }

}
