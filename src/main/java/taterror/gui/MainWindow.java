package taterror.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;

import taterror.TATerror;

/**
 * FXML controller for the main chat window ({@code view/MainWindow.fxml}): the
 * scrollable dialog history, the text input field, and the Send button. Wires
 * user input to a {@link TATerror} instance and renders each exchange as a pair
 * of {@link DialogBox} bubbles.
 */
public class MainWindow {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;

    private TATerror taTerror;

    private Image userImage = new Image(this.getClass().getResourceAsStream("/images/DaUser.png"));
    private Image taTerrorImage = new Image(this.getClass().getResourceAsStream("/images/DaDuke.png"));

    /**
     * FXML lifecycle callback: binds the scroll position to the dialog container's
     * height so the view auto-scrolls to the newest message as the conversation
     * grows.
     */
    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
    }

    /**
     * Supplies the chatbot instance this window talks to, and shows its intro
     * greeting as the first dialog bubble. Must be called once, right after this
     * controller is loaded from FXML, before the user can send anything.
     *
     * @param t the chatbot instance to route all future user input through
     */
    public void setTaTerror(TATerror t) {
        taTerror = t;
        dialogContainer.getChildren().add(
                DialogBox.getTaTerrorDialog("Oh. It's you. I'm TA Terror. Try not to waste my time.", taTerrorImage)
        );
    }

    /**
     * Fired when the user presses Enter in the input field or clicks Send. Reads
     * the current input, gets the chatbot's reply, and appends both as a new pair
     * of dialog bubbles - unless the input is blank, in which case nothing
     * happens.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        if (input.trim().isEmpty()) {
            return;
        }
        String response = taTerror.getResponse(input);
        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(input, userImage),
                DialogBox.getTaTerrorDialog(response, taTerrorImage)
        );
        userInput.clear();
    }
}
