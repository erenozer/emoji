package com.emojibot.commands.staff;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.utils.EmojiInput;
import com.emojibot.utils.command.EmojiCommand;
import com.emojibot.utils.language.Localization;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class FastCommand extends EmojiCommand {

    public FastCommand(Bot bot) {
        super(bot);
        this.name = "fast";
        this.description = "Fast upload an emoji to your server";
        this.cooldownDuration = 4;

        this.localizedNames.put(DiscordLocale.TURKISH, "hızlı-yükle"); 
        this.localizedDescriptions.put(DiscordLocale.TURKISH, "Sunucunuza hızlı bir şekilde emoji yüklemenizi sağlar");


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

        Localization localization = Localization.getLocalization(event.getUser().getId());

        // Check if either file, emoji, or URL is provided
        if ((fileOption == null && emojiOption == null && urlOption == null)) {
            event.getHook().sendMessage(String.format(localization.getMsg("fast_command", "invalid_input"), BotConfig.noEmoji())).queue();
            return;
        }

        // If more than one option is provided, prioritize file > emoji > URL instead of throwing an error 

        long emojiCount = event.getGuild().getEmojiCache().size();
        String emojiName = "emoji_" + (emojiCount+1);

        if (fileOption != null) {
            // Check if the file is an image
            var file = fileOption.getAsAttachment();
            if (!file.isImage()) {
                event.getHook().sendMessage(String.format(localization.getMsg("fast_command", "invalid_file"), BotConfig.noEmoji())).queue();
                return;
            }

            // Upload emoji from file
            file.getProxy().download().thenAccept(inputStream -> {
                try {
                    uploadEmoji(event, emojiName, inputStream, localization);
                } catch (IOException e) {
                    // Usually this one is triggered when something is wrong with the upload
                    event.getHook().sendMessage(String.format(localization.getMsg("fast_command", "upload_failed"), BotConfig.noEmoji())).queue();
                }
            }).exceptionally(error -> {
                event.getHook().sendMessage(String.format(localization.getMsg("fast_command", "upload_failed"), BotConfig.noEmoji())).queue();
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
                event.getHook().sendMessage(String.format(localization.getMsg("fast_command", "upload_failed_only_slot"), BotConfig.noEmoji())).queue();
            }

        } else if (urlOption != null) {
            // Upload emoji from direct URL
            String emojiUrl = urlOption.getAsString();
            try (InputStream inputStream = new URL(emojiUrl).openStream()) {
                uploadEmoji(event, emojiName, inputStream, localization);
            } catch (Exception e) {
                event.getHook().sendMessage(String.format(localization.getMsg("fast_command", "upload_failed_only_slot"), BotConfig.noEmoji())).queue();
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
                emoji -> event.getHook().sendMessage(String.format(localization.getMsg("fast_command", "success"), BotConfig.yesEmoji(), emoji.getAsMention(), emojiName)).queue(),
                error -> event.getHook().sendMessage(String.format(localization.getMsg("fast_command", "upload_failed"), BotConfig.noEmoji())).queue()
            );
    }
}