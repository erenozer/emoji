package com.emojibot.utils.button_listeners;

import com.emojibot.Bot;
import com.emojibot.commands.emoji.ListCommand;
import com.emojibot.commands.other.ShardCommand;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ButtonListener extends ListenerAdapter {
    private final Map<String, Consumer<ButtonInteractionEvent>> buttonHandlers = new HashMap<>();

    /**
     * Initialize all possible button actions
     * @param bot
     */
    public ButtonListener(Bot bot) {
        // List command next/previous page buttons
        registerButtonHandler("list:previous", event -> {
            ListCommand.handleClick(event, false);
        });
        registerButtonHandler("list:next", event -> {
            ListCommand.handleClick(event, true);
        });

        // Shard command next/previous page buttons
        registerButtonHandler("shard:previous", event -> {
            ShardCommand.handleClick(event, false);
        });
        registerButtonHandler("shard:next", event -> {
            ShardCommand.handleClick(event, true);
        });

        // Usage terms accept/decline buttons
        registerButtonHandler("accept_terms", event -> {
            boolean result = UsageTerms.setUserStatus(event.getUser().getId(), true);

            UsageTerms.handleClick(event, result, true);
        });
        registerButtonHandler("decline_terms", event -> {
            boolean result = UsageTerms.setUserStatus(event.getUser().getId(), false);

            UsageTerms.handleClick(event, result, false);
        });

        // Language selection buttons
        registerButtonHandler("language:en", event -> {
            boolean result = LanguageManager.setUserLanguage(event.getUser().getId(), "en");
            
            LanguageManager.handleClick(event, result, "en");
        });
        registerButtonHandler("language:tr", event -> {
            boolean result = LanguageManager.setUserLanguage(event.getUser().getId(), "tr");

            LanguageManager.handleClick(event, result, "tr");
        });

        // Hide command buttons
        registerButtonHandler("hide:enable", event -> {
            boolean result = HideManager.setServerHiddenStatus(event.getGuild().getId(), true);

            HideManager.handleClick(event, result, true);
        });
        registerButtonHandler("hide:disable", event -> {
            boolean result = HideManager.setServerHiddenStatus(event.getGuild().getId(), false);

            HideManager.handleClick(event, result, false);
        });

        // Premium admin buttons for server premium status
        registerButtonHandler("premium:disable", event -> {
            String serverId = event.getButton().getId().split(":")[4];

            boolean result = PremiumManager.setServerPremium(serverId, false);
            
            PremiumManager.handleClick(event, result, false);
        });
        registerButtonHandler("premium:enable", event -> {
            String serverId = event.getButton().getId().split(":")[4];

            boolean result = PremiumManager.setServerPremium(serverId, true);

            PremiumManager.handleClick(event, result, true);
        });

    }

    /**
     * Maps the created button handlers into a map with ID -> method
     * @param buttonId 
     * @param handler
     */
    public void registerButtonHandler(String buttonId, Consumer<ButtonInteractionEvent> handler) {
        buttonHandlers.put(buttonId, handler);
    }

    /**
     * Handles the button interaction with user validation
     * ! While using this logic, make sure to save session IDs in the command and validate them 
     * @param event 
     */
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] contents = event.getComponentId().split(":");
        int length = contents.length;

        // Usage terms button have length 3
        // Other buttons have length 4
        // Premium buttons have length 5
        if(!(length >= 3 && length <= 5)) {
            throw new IllegalArgumentException("Button ID is not valid!");
        }

        System.out.println("Button pressed: " + event.getComponentId());

        if(!contents[1].equals(event.getUser().getId())) {
            // Ignore other users' button presses
            return;
        }

        // Different syntaxes, handle them too
        // Usage warning buttons do not include a command name
        // Premium buttons use a syntax session:userid:premium:disable:serverId
        String actionName = (length == 4 || length == 5) ? String.format("%s:%s", contents[2], contents[3]) : contents[2];

        System.out.println(actionName);
        buttonHandlers.get(actionName).accept(event);
    }

    /**
     * Returns a unique ID with the format uuid:userId
     * @param userId user Id to create a unique session id from
     * @return String in the format uuid:userId
     */
    public static String createUniqueId(String userId) {
        StringBuilder id = new StringBuilder();
        String uuid = UUID.randomUUID().toString();

        id.append(uuid);    
        id.append(":");
        id.append(userId);

        return id.toString();
    }

    /**
     * Handles the queue error and prints the log message if the message was deleted or not found
     * This usually happens when the message with buttons gets deleted by someone before the buttons are expired
     * @param throwable
     * @param logMessage
     */
    public static void handleQueueError(Throwable throwable, String logMessage) {
        if (throwable instanceof ErrorResponseException) {
            ErrorResponseException e = (ErrorResponseException) throwable;
            if (e.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE) {
                // The message was deleted or not found, not printing stack trace for this
                //System.out.println(logMessage);
            } else {
                e.printStackTrace();
            }
        } else {
            throwable.printStackTrace();
        }
    }
}