package me.winflix.vitalcore.events;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.winflix.vitalcore.interfaces.ConfirmationHandler;
import me.winflix.vitalcore.utils.TitleManager;
import me.winflix.vitalcore.utils.Utils;

public class ConfirmationConversation {

    private final Player sender;
    private final Player receiver;
    private final JavaPlugin plugin;
    private final int conversationDurationSeconds = 60;
    private final FileConfiguration messagesConfig;
    private final ConfirmationHandler confirmationHandler;

    public ConfirmationConversation(Player sender, Player receiver, JavaPlugin plugin,
            FileConfiguration messagesConfig, ConfirmationHandler confirmationHandler) {
        this.sender = sender;
        this.receiver = receiver;
        this.plugin = plugin;
        this.messagesConfig = messagesConfig;
        this.confirmationHandler = confirmationHandler;
    }

    public void start() {
        ConfirmationPrompt confirmationPrompt = new ConfirmationPrompt(messagesConfig, confirmationHandler);
        confirmationPrompt.startConversation(sender, receiver, plugin, conversationDurationSeconds);
    }
}

class ConfirmationPrompt extends ValidatingPrompt {

    private final String acceptPrompt;
    private final String rejectPrompt;
    private final String inviteTitle;
    private final String inviteMessage;
    private final ConfirmationHandler confirmationHandler;

    public ConfirmationPrompt(FileConfiguration messagesConfig, ConfirmationHandler confirmationHandler) {
        this.inviteTitle = messagesConfig.getString("inviteTitle");
        this.inviteMessage = messagesConfig.getString("inviteMessage");
        this.acceptPrompt = messagesConfig.getString("acceptPrompt");
        this.rejectPrompt = messagesConfig.getString("rejectPrompt");
        this.confirmationHandler = confirmationHandler;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        Player receiver = (Player) context.getForWhom();
        TitleManager.sendTitle(receiver, inviteTitle, "&e¡Decide tu respuesta!", 10, 100, 20);
        return Utils.useColors(inviteMessage.replace("{sender}", context.getSessionData("sender").toString())
                .replace("{acceptPrompt}", acceptPrompt)
                .replace("{rejectPrompt}", rejectPrompt));
    }

    @Override
    protected boolean isInputValid(ConversationContext context, String input) {
        return input.equalsIgnoreCase(acceptPrompt) || input.equalsIgnoreCase(rejectPrompt);
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, String input) {
        boolean confirmed = input.equalsIgnoreCase(acceptPrompt);
        context.setSessionData("confirmed", confirmed);
        return Prompt.END_OF_CONVERSATION;
    }

    public void startConversation(Player sender, Player receiver, JavaPlugin plugin, int duration) {
        Conversation conversation = new ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(this)
                .withLocalEcho(false)
                .withTimeout(duration)
                .buildConversation(receiver);

        conversation.addConversationAbandonedListener(this::handleConversationEnd);
        conversation.getContext().setSessionData("sender", sender.getName());

        conversation.begin();
    }

    private void handleConversationEnd(ConversationAbandonedEvent event) {
        handleConfirmation(event.getContext());
    }

    private void handleConfirmation(ConversationContext context) {
        Player sender = Bukkit.getPlayer((String) context.getSessionData("sender"));
        Player receiver = (Player) context.getForWhom();

        Object confirmedObj = context.getSessionData("confirmed");
        boolean confirmed = confirmedObj != null && (boolean) confirmedObj;

        confirmationHandler.handleConfirmation(sender, receiver, confirmed);
    }
}
