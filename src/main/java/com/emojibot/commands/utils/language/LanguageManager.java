package com.emojibot.commands.utils.language;

import java.time.Duration;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;

import com.emojibot.BotConfig;
import com.emojibot.commands.utils.MongoManager;
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


public class LanguageManager {
    private static final ConcurrentHashMap<String, Timer> sessionTimers = new ConcurrentHashMap<>();

    /**
     * Returns the language of the user from the database as a string, default to "en" if not found
     * @return the language string like "en" or "tr"
     * @param userId id of the user to get language of
     */
    public static String getUserLanguageString(String userId) {
        // Get the usage terms collection
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
     * Update user approval status with the given boolean as an argument
     * @param userId ID of the user to update status
     * @param newStatus new approval status of the user in the database
     */
    public static boolean setUserLanguage(String userId, String newLanguage) {
        // Get the usage terms collection
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

        if(isSuccess) {
           if(language.equals("en")) {
                event.editMessage(String.format("%s Your language is set to English.", BotConfig.yesEmoji(), event.getUser().getEffectiveName())).setComponents().queue();
              } else if(language.equals("tr")) {
                event.editMessage(String.format("%s Diliniz Türkçe olarak ayarlandı.\n\n:warning: Bot komutlarının isimleri Discord'un kısıtlamaları sebebiyle İngilizce kalmaya devam edecektir, komutların içerikleri ve mesajları ise Türkçe olacaktır.\n**Komut isimlerinin Türkçe açıklamalarını görmek için /help yazabilirsiniz.**", BotConfig.yesEmoji(), event.getUser().getEffectiveName())).setComponents().queue();
              } else {
                event.editMessage(String.format("%s There was an error with your request. Please try again.", BotConfig.noEmoji())).setComponents().queue();
           }
        } else {
            event.editMessage(String.format("%s There was an error with your request.", BotConfig.noEmoji())).setComponents().queue();
        }
    }

    /**
     * This command will NOT check the user status, it will only show the terms and buttons to accept/decline
     * Make sure to check the user status before running the method
     * @param userId userId to approve 
     */
    public static void askUserForLanguage(InteractionHook hook) {
        String userId = hook.getInteraction().getUser().getId();
    
        // Session ID will contain uuid:userId
        String sessionId = ButtonListener.createUniqueId(userId);

        String englishButtonId = sessionId + ":language:en";
        String turkishButtonId = sessionId + ":language:tr";

        Button englishButton = Button.of(ButtonStyle.PRIMARY, englishButtonId, "English", Emoji.fromUnicode("🇺🇸"));
        Button turkishButton = Button.of(ButtonStyle.PRIMARY, turkishButtonId, "Türkçe", Emoji.fromUnicode("🇹🇷"));

        hook.sendMessage("**Language Selection - Dil Seçimi**\n\n🇺🇸 - Select your language for Emoji bot below.\n\n🇹🇷 - Emoji botun dilini aşağıdan seçebilirsin.\n")
        .setComponents(ActionRow.of(englishButton, turkishButton)).queue();

        // Schedule expiration of the buttons
        Timer timer = new Timer();

        sessionTimers.put(sessionId, timer); // Store the timer in the map

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                MessageEmbed expiredEmbed = new EmbedBuilder()
                .addField("Buttons Expired", "You can run the command again using /language", true)
                .setColor(BotConfig.getGeneralEmbedColor())
                .build();
    

                // Remove the buttons
                hook.editOriginalEmbeds(expiredEmbed).setComponents().queue(); 
            }
        }, Duration.ofSeconds(30).toMillis());
    }


}