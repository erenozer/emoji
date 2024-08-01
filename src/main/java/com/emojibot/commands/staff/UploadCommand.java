package com.emojibot.commands.staff;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.utils.Localization;
import com.emojibot.utils.command.EmojiCommand;
import com.emojibot.utils.command.EmojiInput;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class UploadCommand extends EmojiCommand {

    public UploadCommand(Bot bot) {
        super(bot);
        this.name = "upload";
        this.description = "Upload an emoji to your server";
        this.cooldownDuration = 4;

        this.localizedNames.put(DiscordLocale.TURKISH, "yükle");
        this.localizedDescriptions.put(DiscordLocale.TURKISH, "Sunucunuza bir emoji yüklemenizi sağlar");

        // Define options
        this.args.add(new OptionData(OptionType.STRING, "name", "The name for the new emoji - Select one of the arguments (press TAB) after naming your emoji", true)
        .setNameLocalization(DiscordLocale.TURKISH, "isim")
        .setDescriptionLocalization(DiscordLocale.TURKISH, "Yeni emojinin ismi - Yazdıktan sonra yükleme seçeneklerinden birini seçin (TAB tuşuna basarak)"));
        
        this.args.add(new OptionData(OptionType.ATTACHMENT, "file", "The image file for the emoji", false)
        .setNameLocalization(DiscordLocale.TURKISH, "dosya")
        .setDescriptionLocalization(DiscordLocale.TURKISH, "Emoji için resim dosyası"));
        
        this.args.add(new OptionData(OptionType.STRING, "emoji", "Existing custom emoji to upload", false)
        .setNameLocalization(DiscordLocale.TURKISH, "emoji")
        .setDescriptionLocalization(DiscordLocale.TURKISH, "Yüklemek istediğiniz emojinin kendisi"));

        this.args.add(new OptionData(OptionType.STRING, "link", "Direct URL of the emoji image", false)
        .setNameLocalization(DiscordLocale.TURKISH, "bağlantı")
        .setDescriptionLocalization(DiscordLocale.TURKISH, "Emoji dosyasının bağlantısı"));

        this.permission = Permission.MANAGE_GUILD_EXPRESSIONS;
        this.botPermission = Permission.MANAGE_GUILD_EXPRESSIONS;
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        var fileOption = event.getOption("file");
        var emojiOption = event.getOption("emoji");
        var urlOption = event.getOption("link");
        var emojiName = EmojiInput.removeInvalidEmojiCharacters(event.getOption("name").getAsString());

        Localization localization = Localization.getLocalization(event.getUser().getId());

        if(emojiName.length() > 32 || emojiName.length() < 2) {
            event.getHook().sendMessage(String.format(localization.getMsg("upload_command", "name_length_error"), BotConfig.noEmoji())).queue();
            return;
        }

        // Check if either file, emoji, or URL is provided
        if ((fileOption == null && emojiOption == null && urlOption == null) || emojiName == null || emojiName.isEmpty()) {
            event.getHook().sendMessage(String.format(localization.getMsg("upload_command", "invalid_input"), BotConfig.noEmoji())).queue();
            return;
        }

        // If more than one option is provided, prioritize file > emoji > URL instead of throwing an error 

        if (fileOption != null) {
            // Check if the file is an image
            var file = fileOption.getAsAttachment();
            if (!file.isImage()) {
                event.getHook().sendMessage(String.format(localization.getMsg("upload_command", "invalid_file"), BotConfig.noEmoji())).queue();
                return;
            }

            // Upload emoji from file
            file.getProxy().download().thenAccept(inputStream -> {
                try {
                    uploadEmoji(event, emojiName, inputStream, localization);
                } catch (IOException e) {
                    // Usually this one is triggered when something is wrong with the upload
                    event.getHook().sendMessage(String.format(localization.getMsg("upload_command", "upload_failed"), BotConfig.noEmoji())).queue();
                }
            }).exceptionally(error -> {
                event.getHook().sendMessage(String.format(localization.getMsg("upload_command", "upload_failed"), BotConfig.noEmoji())).queue();
                return null;
            });

        } else if (emojiOption != null) {
            // Extract emoji ID and URL from existing custom emoji
            String emojiInput = emojiOption.getAsString();
            String emojiId = EmojiInput.extractEmojiId(emojiInput);
            String emojiUrl = emojiInput.contains("<a:") ?
                    String.format("https://cdn.discordapp.com/emojis/%s.gif?&quality=lossless", emojiId) :
                    String.format("https://cdn.discordapp.com/emojis/%s.png?quality=lossless", emojiId);

            // Upload emoji from URL
            try (InputStream inputStream = new URL(emojiUrl).openStream()) {
                uploadEmoji(event, emojiName, inputStream, localization);
            } catch (Exception e) {
                event.getHook().sendMessage(String.format(localization.getMsg("upload_command", "upload_failed_only_slot"), BotConfig.noEmoji())).queue();
            }

        } else if (urlOption != null) {
            // Upload emoji from direct URL
            String emojiUrl = urlOption.getAsString();
            try (InputStream inputStream = new URL(emojiUrl).openStream()) {
                uploadEmoji(event, emojiName, inputStream, localization);
            } catch (Exception e) {
                event.getHook().sendMessage(String.format(localization.getMsg("upload_command", "upload_failed_only_slot"), BotConfig.noEmoji())).queue();
            }
        }
    }

    /**
     * Uploads an emoji to the server, throws an exception if the upload fails
     * @param event the slash command event
     * @param emojiName the name of the emoji
     * @param inputStream the input stream of the image
     * @throws IOException if the upload fails
     */
    private void uploadEmoji(SlashCommandInteractionEvent event, String emojiName, InputStream inputStream, Localization localization) throws IOException {
        Icon icon = Icon.from(inputStream);
        event.getGuild().createEmoji(emojiName, icon).reason(event.getUser().toString())
            .queue(
                emoji -> event.getHook().sendMessage(String.format(localization.getMsg("upload_command", "success"), BotConfig.yesEmoji(), emoji.getAsMention())).queue(),
                error -> event.getHook().sendMessage(String.format(localization.getMsg("upload_command", "failed"), BotConfig.noEmoji())).queue()
            );
    }
}