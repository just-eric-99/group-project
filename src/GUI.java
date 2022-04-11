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
        if (toAddressInput.getText().isEmpty()){
            appendLog("Address not provided.");
            return;
        }

        if (amountInput.getText().isEmpty()){
            appendLog("Amount not provided.");
            return;
        }

        double amount = Double.parseDouble(amountInput.getText());
        if (main.minerWallet.getBalance() < amount) {
            appendLog("Insufficient amount.");
        }
        Transaction tx = main.minerWallet.pay(toAddressInput.getText(), amount);

        if(tx != null) {
            main.socketList.forEach(socket -> {
                try {
                    main.sendTransaction(tx, socket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            appendLog("Transaction processed successfully.");
        }  else {
            appendLog("Transaction cannot be processed. Please try again.");
        }
    }

    public void mineButton(ActionEvent actionEvent) {
        main.mine();
    }

    public void appendLog(String str) {
        System.out.println(str);
        log.appendText(str + "\n");
    }

    public void appendHistory(String str) {
        System.out.println(str);
        history.appendText(str + "\n");
    }


    public void updateBalanceInput(String balance) {
        balanceInput.setText(balance);
    }
}
