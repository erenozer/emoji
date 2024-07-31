package com.emojibot.utils.button_listeners;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;

import com.emojibot.BotConfig;
import com.emojibot.utils.Localization;
import com.emojibot.utils.MongoManager;
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

public class HideManager {
    private static final ConcurrentHashMap<String, Timer> sessionTimers = new ConcurrentHashMap<>();

     public static void handleClick(ButtonInteractionEvent event, boolean isSuccess, boolean option) {
        String sessionId = event.getComponentId().split(":")[0] + ":" +event.getComponentId().split(":")[1] ;

        // Cancel the timer for this session
        Timer timer = sessionTimers.remove(sessionId);
        if (timer != null) {
            timer.cancel();
        }

        Localization localization = Localization.getLocalization(event.getUser().getId());

        String status = option ? localization.getMsg("hide_command", "hidden") : localization.getMsg("hide_command", "not_hidden");
        try {
            if(isSuccess) {
                if(option) {
                    event.editMessage(String.format(localization.getMsg("hide_command", "success"), BotConfig.yesEmoji(), status)).setComponents().queue();
                } else {
                    event.editMessage(String.format(localization.getMsg("hide_command", "success"), BotConfig.yesEmoji(), status)).setComponents().queue();
                }
            } else {
                event.editMessage(String.format(localization.getMsg("hide_command", "error"), BotConfig.noEmoji())).setComponents().queue();
            }
        } catch (ErrorResponseException e) {
            if (e.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE) {
                // The message was deleted or not found
                //System.out.println("The message was deleted or not found, cannot update.");
            } else {
                e.printStackTrace();
            }
        }
    }


    /**
     * Set the server's hidden status to true or false
     * @param server_id
     * @param newStatus
     * @return
     */
    public static boolean setServerHiddenStatus(String server_id, boolean newStatus) {
        // Get the servers collection
        MongoCollection<Document> collection = MongoManager.getServersCollection();

        if(collection == null) {
            return false;
        }

        Document filter = new Document("server_id", server_id);

        Document update = new Document("$set", new Document("hidden", newStatus));

        // Enable upsert to insert the document if it doesn't exist
        UpdateOptions options = new UpdateOptions().upsert(true);

        // Update or insert the document
        collection.updateOne(filter, update, options);

        return true;
    }

    /**
     * Get the hidden status of a server
     * @param server_id
     * @return
     */
    public static boolean getServerHiddenStatus(String server_id) {
        // Get the servers collection
        MongoCollection<Document> collection = MongoManager.getServersCollection();

        if(collection == null)
            return false;

        Document filter = new Document("server_id", server_id);

        var result = collection.find(filter).first();

        if(result != null) {
            // Check the premium field's value for the server, default to false
            var hidden = result.getBoolean("hidden", false);

            return hidden;
        }

        // Server not found in the database
        return false;
    }

    public static void askForHideStatus(InteractionHook hook, String serverId) {
        String userId = hook.getInteraction().getUser().getId();

        Localization localization = Localization.getLocalization(userId);

        // Session ID will contain uuid:userId
        String sessionId = ButtonListener.createUniqueId(userId);

        String cancelButtonId = sessionId + ":hide:disable";
        String confirmButtonId = sessionId + ":hide:enable";

        Button cancelButton = Button.of(ButtonStyle.PRIMARY, cancelButtonId, localization.getMsg("hide_command", "disable"), Emoji.fromFormatted(BotConfig.noEmoji()));
        Button confirmButton = Button.of(ButtonStyle.PRIMARY, confirmButtonId, localization.getMsg("hide_command", "enable"), Emoji.fromFormatted(BotConfig.yesEmoji()));

        String serverPremiumStatus = getServerHiddenStatus(serverId) ? localization.getMsg("hide_command", "hidden") : localization.getMsg("hide_command", "not_hidden");

        hook.sendMessage(String.format(localization.getMsg("hide_command", "no_option"), BotConfig.infoEmoji(), serverPremiumStatus))
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
                    .setTitle(localization.getMsg("hide_command", "button_expired"))
                    .setColor(BotConfig.getGeneralEmbedColor())
                    .build();

                try {
                    // Remove the buttons
                    hook.editOriginalEmbeds(expiredEmbed).setComponents().queue(null, throwable -> ButtonListener.handleQueueError(throwable, "Could not edit message: message not found or deleted"));
                } catch (ErrorResponseException e) {
                    if (e.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE) {
                        //System.out.println("The message was deleted or not found, cannot update.");
                    } else {
                        e.printStackTrace();
                    }
                }
            }
        }, Duration.ofSeconds(30).toMillis());
    }
}
