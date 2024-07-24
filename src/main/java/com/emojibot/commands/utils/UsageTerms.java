package com.emojibot.commands.utils;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Color;
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
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public class UsageTerms {
    private static final ConcurrentHashMap<String, Timer> sessionTimers = new ConcurrentHashMap<>();

    /**
     * @return 0 if the user has not accepted the warning/not in the database, 1 if the user has accepted the warning, -1 if database collection is not found (something is wrong)
     * @param userId id of the user to check approval
     */
    public static int checkUserStatus(String userId) {
        // Get the usage terms collection
        MongoCollection<Document> collection = MongoManager.getUsageTermsCollection();

        if(collection == null)
            return -1;

        Document filter = new Document("user_id", userId);

        // .find() returns an iterator, get the first of that.
        var result = collection.find(filter).first();

        if(result != null) {
            // Check the approved field's value, default to false
            var isApproved = result.getBoolean("approved", false); 

            if(isApproved) {
                return 1;
            } else {
                return 0;
            }
        }

        // User not found in the database
        return 0;
    }

    /**
     * Update user approval status with the given boolean as an argument
     * @param userId ID of the user to update status
     * @param newStatus new approval status of the user in the database
     */
    public static boolean setUserStatus(String userId, boolean newStatus) {
        // Get the usage terms collection
        MongoCollection<Document> collection = MongoManager.getUsageTermsCollection();

        if(collection == null) {
            return false;
        }

        // Create a filter to find the document from user_id field that matches the userId we are looking for
        Document filter = new Document("user_id", userId);

        if(checkUserStatus(userId) != 0) {
            // User has already accepted the terms, do not process the request
            return false;
        }

        // Create a document with set operator to update the approved field
        Document update = new Document("$set", new Document("approved", newStatus));

        // Enable upsert to insert the document if it doesn't exist
        UpdateOptions options = new UpdateOptions().upsert(true);

        // Update or insert the document
        collection.updateOne(filter, update, options);

        return true;
    }

    public static void handleClick(ButtonInteractionEvent event, boolean isSuccess, boolean option) {
        String sessionId = event.getComponentId().split(":")[0] + ":" +event.getComponentId().split(":")[1] ;

        // Cancel the timer for this session
        Timer timer = sessionTimers.remove(sessionId);
        if (timer != null) {
            timer.cancel();
        }

        if(isSuccess) {
            if(option) {
                event.editMessage(String.format("%s You have acknowledged the disclaimer, you can now use the command.", BotConfig.yesEmoji())).setComponents().queue();
            } else {
                event.editMessage(String.format("%s You have declined the disclaimer, you cannot use this command.", BotConfig.infoEmoji())).setComponents().queue();
            }
        } else {
            event.editMessage(String.format("%s There was an error with your request. This may be because you have already accepted this before. You can try the command again.", BotConfig.noEmoji())).setComponents().queue();
        }
    }

    /**
     * This command will NOT check the user status, it will only show the terms and buttons to accept/decline
     * Make sure to check the user status before running the method
     * @param userId userId to approve 
     */
    public static void validateTerms(InteractionHook hook) {
        String userId = hook.getInteraction().getUser().getId();
    
        // Session ID will contain uuid:userId
        String sessionId = ButtonListener.createUniqueId(userId);

        String declineButtonId = sessionId + ":decline_terms";
        String acceptButtonId = sessionId + ":accept_terms";

        Button declineButton = Button.of(ButtonStyle.DANGER, declineButtonId, "Decline", Emoji.fromFormatted(BotConfig.noEmoji()));
        Button acceptButton = Button.of(ButtonStyle.SUCCESS, acceptButtonId, "Accept", Emoji.fromFormatted(BotConfig.yesEmoji()));

        hook.sendMessage(String.format("%s **Disclaimer**\n\nEmojis sent from this command and some other commands of Emoji Bot may cause epileptic seizures for some users.\nBy accepting, you acknowledge that you have been informed of this and accept the terms of usage for all commands of this bot.\n\nUse the buttons below to accept or decline.", BotConfig.infoEmoji()))
        .setComponents(ActionRow.of(declineButton, acceptButton)).queue();

        // Schedule expiration of the buttons
        Timer timer = new Timer();

        sessionTimers.put(sessionId, timer); // Store the timer in the map

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                MessageEmbed expiredEmbed = new EmbedBuilder()
                .addField("Buttons Expired", "You can run the command again to accept or decline", true)
                .setColor(Color.RED)
                .build();
    

                // Remove the buttons
                hook.editOriginalEmbeds(expiredEmbed).setComponents().queue(); 
            }
        }, Duration.ofSeconds(30).toMillis());
    }
}