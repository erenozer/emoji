package com.emojibot.utils;

import java.awt.Color;
import java.time.Duration;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;

import com.emojibot.BotConfig;
import com.emojibot.events.ButtonListener;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.ErrorResponse;


public class PremiumManager {
    private static final ConcurrentHashMap<String, Timer> sessionTimers = new ConcurrentHashMap<>();

    /**
     * Returns the language of the user from the database as a string, default to "en" if not found
     * @return the language string like "en" or "tr"
     * @param userId id of the user to get language of
     */
    public static boolean getPremiumStatus(String server_id) {
        // Get the usage terms collection
        MongoCollection<Document> collection = MongoManager.getServersCollection();

        if(collection == null)
            return false;

        Document filter = new Document("server_id", server_id);

        var result = collection.find(filter).first();

        if(result != null) {
            // Check the premium field's value for the server, default to false
            var premiumEnabled = result.getBoolean("premium", false);

            return premiumEnabled;
        }

        // User not found in the database
        return false;
    }

    private static String getServerPremiumStatusString(String server_id) {
        return getPremiumStatus(server_id) ? "enabled" : "disabled";
    }

    /**
     * Update server premium status with the given boolean as an argument
     * @param serverId
     * @param newStatus
     * @return
     */
    public static boolean setServerPremium(String serverId, boolean newStatus) {
        // Get the usage terms collection
        MongoCollection<Document> collection = MongoManager.getServersCollection();

        if(collection == null) {
            return false;
        }

        Document filter = new Document("server_id", serverId);

        Document update = new Document("$set", new Document("premium", newStatus));

        // Enable upsert to insert the document if it doesn't exist
        UpdateOptions options = new UpdateOptions().upsert(true);

        // Update or insert the document
        collection.updateOne(filter, update, options);

        return true;
    }

    /**
     * Does not actually update the database, just handles the click/message,
     * use setServerPremium to update the database with the new status
     * @param event
     * @param isSuccess
     * @param newStatus
     */
    public static void handleClick(ButtonInteractionEvent event, boolean isSuccess, boolean newStatus) {
        String sessionId = event.getComponentId().split(":")[0] + ":" + event.getComponentId().split(":")[1];

        // Cancel the timer for this session
        Timer timer = sessionTimers.remove(sessionId);
        if (timer != null) {
            timer.cancel();
        }

        try {
            if (isSuccess) {
                if (newStatus) {
                    event.editMessage(String.format("%s Premium has been **enabled** for that server.", BotConfig.yesEmoji()))
                        .setComponents().queue(null, throwable -> ButtonListener.handleQueueError(throwable, "Could not edit message: message not found or deleted"));
                } else {
                    event.editMessage(String.format("%s Premium has **disabled** for that server.", BotConfig.yesEmoji()))
                        .setComponents().queue(null, throwable -> ButtonListener.handleQueueError(throwable, "Could not edit message: message not found or deleted"));
                }
            } else {
                event.editMessage(String.format("%s There was an error with your request.", BotConfig.noEmoji()))
                    .setComponents().queue(null, throwable -> ButtonListener.handleQueueError(throwable, "Could not edit message: message not found or deleted"));
            }
        } catch (ErrorResponseException e) {
            if (e.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE) {
                // The message was deleted or not found
                System.out.println("The message was deleted or not found, cannot update.");
            } else {
                e.printStackTrace();
            }
        }
    }

    public static void askForConfirmation(InteractionHook hook, String serverId) {
        String userId = hook.getInteraction().getUser().getId();

        // Session ID will contain uuid:userId
        String sessionId = ButtonListener.createUniqueId(userId);

        String cancelButtonId = sessionId + ":premium:disable";
        String confirmButtonId = sessionId + ":premium:enable";

        Button cancelButton = Button.of(ButtonStyle.DANGER, cancelButtonId, "Disable Premium", Emoji.fromFormatted(BotConfig.noEmoji()));
        Button confirmButton = Button.of(ButtonStyle.SUCCESS, confirmButtonId, "Enable Premium", Emoji.fromFormatted(BotConfig.yesEmoji()));

        String serverPremiumStatus = getServerPremiumStatusString(serverId);

        hook.sendMessage(String.format(":star: **Premium Management**\n\nServer with the ID **%s** currently has premium **%s**. \nUse buttons to disable/enable premium for this server.", serverId, serverPremiumStatus))
            .setComponents(ActionRow.of(cancelButton, confirmButton))
            .queue();

        // Schedule expiration of the buttons
        Timer timer = new Timer();

        // Store the timer in the map
        sessionTimers.put(sessionId, timer);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                MessageEmbed expiredEmbed = new EmbedBuilder()
                    .addField("Buttons Expired", "You can run the command again", true)
                    .setColor(Color.YELLOW)
                    .build();

                try {
                    // Remove the buttons
                    hook.editOriginalEmbeds(expiredEmbed).setComponents().queue(null, throwable -> ButtonListener.handleQueueError(throwable, "Could not edit message: message not found or deleted"));
                } catch (ErrorResponseException e) {
                    if (e.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE) {
                        System.out.println("The message was deleted or not found, cannot update.");
                    } else {
                        e.printStackTrace();
                    }
                }
            }
        }, Duration.ofSeconds(30).toMillis());
    }

}