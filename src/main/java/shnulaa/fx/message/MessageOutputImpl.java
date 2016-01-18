package shnulaa.fx.message;

import javafx.scene.control.TextArea;
import shnulaa.fx.constant.Constant;

@SuppressWarnings("restriction")
public class MessageOutputImpl {
    private TextArea textArea;

    public MessageOutputImpl(TextArea textArea) {
        this.textArea = textArea;
    }

    public void output(String message) {
        this.textArea.appendText(message + Constant.BR);
    }

}
