package me.winflix.vitalcore.general.utils;

import me.winflix.vitalcore.general.interfaces.ClickableAction;

public class ClickableMessage {
    String message;
    String command;
    String hoverMessage;
    ClickableAction action;

    public ClickableMessage(final String message, final String command, final String hoverMessage,
            final ClickableAction action) {
        this.message = message;
        this.command = command;
        this.action = action;
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

    public ClickableAction getAction() {
        return action;
    }
}
