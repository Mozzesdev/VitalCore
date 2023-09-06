package me.winflix.vitalcore.general.utils;

public class ClickableMessage {
    String message;
    String command;
    String hoverMessage;

    public ClickableMessage(final String message, final String command, final String hoverMessage) {
        this.message = message;
        this.command = command;
        this.hoverMessage = hoverMessage;
    }

    public String getMessage() {
        return message;
    }

    public String getHoverMessage() {
        return hoverMessage;
    }

    public String getCommand() {
        return command;
    }

}
