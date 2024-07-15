package com.emojibot.commands.staff;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.commands.utils.Command;
import com.emojibot.commands.utils.EmojiInput;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class UploadCommand extends Command {

    public UploadCommand(Bot bot) {
        super(bot);
        this.name = "upload";
        this.description = "Upload an emoji to your server";
        this.cooldownDuration = 4;

        // Define options
        this.args.add(new OptionData(OptionType.STRING, "name", "The name for the new emoji - Select one of the arguments (press TAB) after naming your emoji", true));
        this.args.add(new OptionData(OptionType.ATTACHMENT, "file", "The image file for the emoji", false));
        this.args.add(new OptionData(OptionType.STRING, "emoji", "Existing custom emoji to upload", false));
        this.args.add(new OptionData(OptionType.STRING, "link", "Direct URL of the emoji image", false));

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

        if(emojiName.length() > 32 || emojiName.length() < 2) {
            event.getHook().sendMessage(String.format("%s Emoji name must be longer than 2, shorter than 32 characters.", BotConfig.noEmoji())).queue();
            return;
        }

        // Check if either file, emoji, or URL is provided
        if ((fileOption == null && emojiOption == null && urlOption == null) || emojiName == null || emojiName.isEmpty()) {
            event.getHook().sendMessage(String.format("%s Please provide an emoji file, **or** an existing custom emoji, **or** a direct emoji link to upload.", BotConfig.noEmoji())).queue();
            return;
        }

        // If more than one option is provided, prioritize file > emoji > URL instead of throwing an error 

        if (fileOption != null) {
            // Check if the file is an image
            var file = fileOption.getAsAttachment();
            if (!file.isImage()) {
                event.getHook().sendMessage(String.format("%s The file provided is not an image. Please upload a valid image/gif file.", BotConfig.noEmoji())).queue();
                return;
            }

            // Upload emoji from file
            file.getProxy().download().thenAccept(inputStream -> {
                try {
                    uploadEmoji(event, emojiName, inputStream);
                } catch (IOException e) {
                    // Usually this one is triggered when something is wrong with the upload
                    event.getHook().sendMessage(String.format("%s Upload failed, please make sure that:\n- Server has a free slot to upload an emoji\n- File is not bigger than 256kb in size", BotConfig.noEmoji())).queue();
                }
            }).exceptionally(error -> {
                event.getHook().sendMessage(String.format("%s Upload failed, please make sure that:\n- Server has a free slot to upload an emoji\n- File is not bigger than 256kb in size", BotConfig.noEmoji())).queue();
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
                uploadEmoji(event, emojiName, inputStream);
            } catch (Exception e) {
                event.getHook().sendMessage(String.format("%s Upload failed, please make sure that:\n- Server has a free slot to upload an emoji", BotConfig.noEmoji())).queue();
            }

        } else if (urlOption != null) {
            // Upload emoji from direct URL
            String emojiUrl = urlOption.getAsString();
            try (InputStream inputStream = new URL(emojiUrl).openStream()) {
                uploadEmoji(event, emojiName, inputStream);
            } catch (Exception e) {
                event.getHook().sendMessage(String.format("%s Upload failed, please make sure that:\n- Server has a free slot to upload an emoji", BotConfig.noEmoji())).queue();
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
    private void uploadEmoji(SlashCommandInteractionEvent event, String emojiName, InputStream inputStream) throws IOException {
        Icon icon = Icon.from(inputStream);
        event.getGuild().createEmoji(emojiName, icon).reason("Responsible " + event.getUser())
            .queue(
                emoji -> event.getHook().sendMessage(String.format("%s Emoji %s has been uploaded successfully!", BotConfig.yesEmoji(), emoji.getAsMention())).queue(),
                error -> event.getHook().sendMessage(String.format("%s Upload failed, please make sure that:\n- Server has a free slot to upload an emoji\n- File is not bigger than 256kb in size", BotConfig.noEmoji())).queue()
            );
    }
}