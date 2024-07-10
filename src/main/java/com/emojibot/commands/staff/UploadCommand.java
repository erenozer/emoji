package com.emojibot.commands.staff;

import com.emojibot.Bot;
import com.emojibot.commands.utils.Command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.AttachmentProxy;
import net.dv8tion.jda.api.utils.FileProxy;

import java.io.InputStream;

public class UploadCommand extends Command {

    public UploadCommand(Bot bot) {
        super(bot);
        this.name = "upload";
        this.description = "Upload an emoji to your server";

        // Define options
        this.args.add(new OptionData(OptionType.ATTACHMENT, "image", "The image file for the emoji", true));
        this.args.add(new OptionData(OptionType.STRING, "name", "The name for the new emoji", true));
        
        this.permission = Permission.MANAGE_GUILD_EXPRESSIONS;
        this.botPermission = Permission.MANAGE_GUILD_EXPRESSIONS;
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        // Get the file and name options
        var fileOption = event.getOption("image").getAsAttachment();
        var emojiName = event.getOption("name").getAsString();

        if (fileOption == null || emojiName == null || emojiName.isEmpty()) {
            event.getHook().sendMessage("Please provide both an image and a name for the emoji.").setEphemeral(true).queue();
            return;
        }

        if (!fileOption.isImage()) {
            event.getHook().sendMessage("The file provided is not an image. Please upload a valid image file.").setEphemeral(true).queue();
            return;
        }

        // Upload the emoji
        fileOption.getProxy().download().thenAccept(inputStream -> {
            try {
                Icon icon = Icon.from(inputStream);
                event.getGuild().createEmoji(emojiName, icon).reason("Responsible " + event.getUser())
                    .queue(
                        emoji -> event.getHook().sendMessage("Emoji " + emoji.getAsMention() + " has been uploaded successfully!").queue(),
                        error -> event.getHook().sendMessage("Failed to upload the emoji.").setEphemeral(true).queue()
                    );
            } catch (Exception e) {
                event.getHook().sendMessage("Failed to process the image file.").setEphemeral(true).queue();
            }
        }).exceptionally(error -> {
            event.getHook().sendMessage("Failed to retrieve the image file.").setEphemeral(true).queue();
            return null;
        });
    }
}