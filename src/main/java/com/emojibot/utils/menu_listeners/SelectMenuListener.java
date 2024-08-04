package com.emojibot.utils.menu_listeners;

import com.emojibot.Bot;
import com.emojibot.commands.staff.RandomUploadCommand;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class SelectMenuListener extends ListenerAdapter {
    private final Map<String, Consumer<StringSelectInteractionEvent>> selectMenuHandlers = new HashMap<>();

    /**
     * Initialize all possible select menu actions
     * @param bot
     */
    public SelectMenuListener(Bot bot) {
        // List command next/previous page buttons
        registerSelectHandler("random_emojis", event -> {
            RandomUploadCommand.handleSelect(event);
        });

        // Register other handlers as needed
    }

    /**
     * Maps the created select menu handlers into a map with ID -> method
     * @param selectId 
     * @param handler
     */
    public void registerSelectHandler(String selectId, Consumer<StringSelectInteractionEvent> handler) {
        selectMenuHandlers.put(selectId, handler);
    }

    /**
     * Handles the select menu interaction with user validation
     * @param event 
     */
    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        String[] contents = event.getComponentId().split(":");
        int length = contents.length;

        if (length != 3) {
            throw new IllegalArgumentException("Select menu ID is not valid!");
        }

        System.out.println("Select menu used: " + event.getComponentId());

        if (!contents[1].equals(event.getUser().getId())) {
            // Ignore other users' select menu interactions
            return;
        }

        String actionName = contents[2];

        Consumer<StringSelectInteractionEvent> handler = selectMenuHandlers.get(actionName);
        if (handler != null) {
            handler.accept(event);
        }
    }

    /**
     * Returns a unique ID with the format uuid:userId
     * @param userId user Id to create a unique session id from
     * @return String in the format uuid:userId
     */
    public static String createUniqueId(String userId) {
        return UUID.randomUUID().toString() + ":" + userId;
    }

    /**
     * Handles the queue error and prints the log message if the message was deleted or not found
     * This usually happens when the message with select menus gets deleted by someone before the select menus expire
     * @param throwable
     * @param logMessage
     */
    public static void handleQueueError(Throwable throwable, String logMessage) {
        if (throwable instanceof ErrorResponseException) {
            ErrorResponseException e = (ErrorResponseException) throwable;
            if (e.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE) {
                // The message was deleted or not found, not printing stack trace for this
                System.out.println(logMessage);
            } else {
                e.printStackTrace();
            }
        } else {
            throwable.printStackTrace();
        }
    }
}