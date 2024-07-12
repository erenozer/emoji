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

        // Define options
        this.args.add(new OptionData(OptionType.STRING, "name", "The name for the new emoji", true));
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
        var emojiName = event.getOption("name").getAsString();

        // Check if either file, emoji, or URL is provided
        if ((fileOption == null && emojiOption == null && urlOption == null) || emojiName == null || emojiName.isEmpty()) {
            event.getHook().sendMessage(BotConfig.noEmoji() + " Please provide an emoji file, **OR** an existing custom emoji, **OR** a direct emoji link to upload.").queue();
            return;
        }

        if (fileOption != null) {
            // Check if the file is an image
            var file = fileOption.getAsAttachment();
            if (!file.isImage()) {
                event.getHook().sendMessage(BotConfig.noEmoji() + " The file provided is not an image. Please upload a valid image/gif file.").queue();
                return;
            }

            // Upload emoji from file
            file.getProxy().download().thenAccept(inputStream -> {
                try {
                    uploadEmoji(event, emojiName, inputStream);
                } catch (IOException e) {
                    // Usually this one is triggered when something is wrong with the upload
                    event.getHook().sendMessage(BotConfig.noEmoji() + " Upload failed, please make sure that:\n- Server has a free slot to upload an emoji\n- File is not bigger than 256kb in size").queue();
                }
            }).exceptionally(error -> {
                event.getHook().sendMessage(BotConfig.noEmoji() + " Upload failed, please make sure that:\n- Server has a free slot to upload an emoji\n- File is not bigger than 256kb in size").queue();
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
                event.getHook().sendMessage(BotConfig.noEmoji() + " Upload failed, please make sure that:\n- Server has a free slot to upload an emoji").queue();
            }

        } else if (urlOption != null) {
            // Upload emoji from direct URL
            String emojiUrl = urlOption.getAsString();
            try (InputStream inputStream = new URL(emojiUrl).openStream()) {
                uploadEmoji(event, emojiName, inputStream);
            } catch (Exception e) {
                event.getHook().sendMessage(BotConfig.noEmoji() + " Upload failed, please make sure that:\n- Server has a free slot to upload an emoji").queue();
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
                emoji -> event.getHook().sendMessage(BotConfig.yesEmoji() + " Emoji " + emoji.getAsMention() + " has been uploaded successfully!").queue(),
                error -> event.getHook().sendMessage(BotConfig.noEmoji() + " Upload failed, please make sure that:\n- Server has a free slot to upload an emoji\n- File is not bigger than 256kb in size").queue()
            );
    }
}