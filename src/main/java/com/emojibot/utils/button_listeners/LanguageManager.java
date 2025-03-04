package com.emojibot.utils.button_listeners;

import java.time.Duration;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;

import com.emojibot.BotConfig;
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


public class LanguageManager {
    private static final ConcurrentHashMap<String, Timer> sessionTimers = new ConcurrentHashMap<>();

    /**
     * Returns the language of the user from the database as a string, default to "en" if not found
     * @return the language string like "en" or "tr"
     * @param userId id of the user to get language of
     */
    public static String getUserLanguageString(String userId) {
        // Get the user preferences collection
        MongoCollection<Document> collection = MongoManager.getUserPrefCollection();

        if(collection == null)
            return "en";

        Document filter = new Document("user_id", userId);

        // .find() returns an iterator, get the first of that.
        var result = collection.find(filter).first();

        if(result != null) {
            // Check the approved field's value, default to false
            var language = result.getString("language");
            if(language == null) {
                language = "en";
            }

            return language;
        }

        // User not found in the database
        return "en";
    }

    /**
     * Returns the language of the user from the database as a Locale object, default to "en" if not found
     * @param userId id of the user to get language of
     * @return Locale object of the user's language
     */
    public static Locale getUserLanguageLocal(String userId) {
        return getLocaleFromLanguageCode(getUserLanguageString(userId));
    }

    public static boolean isUserLanguageSet(String userId) {
        return MongoManager.getUserPrefCollection().find(new Document("user_id", userId)).first() != null;
    }

    /**
     * Converts String to a Locale object
     * @param languageCode language code like "en" or "tr" from the database
     * @return Locale object of the language code, default to English if not found
     */
    public static Locale getLocaleFromLanguageCode(String languageCode) {
        switch (languageCode) {
            case "tr":
                return Locale.forLanguageTag("tr");
            case "en":
            default:
                return Locale.forLanguageTag("en");
        }
    }

    /**
     * Sets the language of the user in the database
     * @param userId
     * @param newLanguage
     * @return
     */
    public static boolean setUserLanguage(String userId, String newLanguage) {
        // Get the user preferences collection
        MongoCollection<Document> collection = MongoManager.getUserPrefCollection();

        if(collection == null) {
            return false;
        }

        // Create a filter to find the document from user_id field that matches the userId we are looking for
        Document filter = new Document("user_id", userId);

        // Update the language for the user even if the language is the same

        // Create a document with set operator to update the language
        Document update = new Document("$set", new Document("language", newLanguage));

        // Enable upsert to insert the document if it doesn't exist
        UpdateOptions options = new UpdateOptions().upsert(true);

        // Update or insert the document
        collection.updateOne(filter, update, options);

        return true;
    }

    /**
     * Handles the button interaction with user validation, does not actually update the language from the database
     * a seperate method setUserLanguage must be called before this method to update the language
     * @param event
     * @param isSuccess
     * @param language
     */
    public static void handleClick(ButtonInteractionEvent event, boolean isSuccess, String language) {
        String sessionId = event.getComponentId().split(":")[0] + ":" +event.getComponentId().split(":")[1] ;

        // Cancel the timer for this session
        Timer timer = sessionTimers.remove(sessionId);
        if (timer != null) {
            timer.cancel();
        }
        try {
            if(isSuccess) {
            if(language.equals("en")) {
                    event.editMessage(String.format("%s Your language is set to English.\n\nCommand names will change according to your Discord client's language.", BotConfig.yesEmoji(), event.getUser().getEffectiveName())).setComponents().queue();
                } else if(language.equals("tr")) {
                    event.editMessage(String.format("%s Diliniz Türkçe olarak ayarlandı.\n\nKomut isimleri Discord dilinizle uyumlu olarak değişecektir.", BotConfig.yesEmoji(), event.getUser().getEffectiveName())).setComponents().queue();
                } else {
                    event.editMessage(String.format("%s There was an error with your request. Please try again.", BotConfig.noEmoji())).setComponents().queue();
                }
            } else {
                event.editMessage(String.format("%s There was an error with your request.", BotConfig.noEmoji())).setComponents().queue();
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

    /**
     * Ask the user to select a language with buttons
     * @param hook InteractionHook object to send the message
     */
    public static void askUserForLanguage(InteractionHook hook) {
        String userId = hook.getInteraction().getUser().getId();
        
        // Session ID will contain uuid:userId
        String sessionId = ButtonListener.createUniqueId(userId);
    
        String englishButtonId = sessionId + ":language:en";
        String turkishButtonId = sessionId + ":language:tr";
    
        Button englishButton = Button.of(ButtonStyle.PRIMARY, englishButtonId, "English", Emoji.fromUnicode("🇺🇸"));
        Button turkishButton = Button.of(ButtonStyle.PRIMARY, turkishButtonId, "Türkçe", Emoji.fromUnicode("🇹🇷"));
    
        // Send the message and capture the Message object to edit the latest message, not the previous message
        // which can be sent due to user not having a supported language, (see CommandManager->onSlashCommandInteraction)
        hook.sendMessage("**Language Selection - Dil Seçimi**\n\n🇺🇸 - Select your language for Emoji bot below.\n\n🇹🇷 - Emoji botun dilini aşağıdan seçebilirsin.\n")
            .setComponents(ActionRow.of(englishButton, turkishButton))
            .queue(message -> {
                // Schedule expiration of the buttons
                Timer timer = new Timer();
                sessionTimers.put(sessionId, timer); // Store the timer in the map
    
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        MessageEmbed expiredEmbed = new EmbedBuilder()
                            .addField("Buttons Expired - Butonların Süresi Doldu", "Run the command again to set your language  \nKomutu tekrar kullanarak dilinizi ayarlayabilirsiniz", true)
                            .setColor(BotConfig.getGeneralEmbedColor())
                            .build();
    
                        try {
                            // Remove the buttons
                            message.editMessageEmbeds(expiredEmbed).setComponents().queue(null, throwable -> ButtonListener.handleQueueError(throwable, "Could not edit message: message not found or deleted"));
                        } catch (ErrorResponseException e) {
                            if (e.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE) {
                                System.out.println("The message was deleted or not found, cannot update.");
                            } else {
                                e.printStackTrace();
                            }
                        }
                    }
                }, Duration.ofSeconds(30).toMillis());
            });
    }

}