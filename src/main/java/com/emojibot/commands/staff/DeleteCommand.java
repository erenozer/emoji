package com.emojibot.commands.staff;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.commands.utils.Command;
import com.emojibot.commands.utils.EmojiInput;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class DeleteCommand extends Command {

    public DeleteCommand(Bot bot) {
        super(bot);
        this.name = "delete";
        this.description = "Delete an emoji from your server, select either by emoji or name";

        // Define options
        this.args.add(new OptionData(OptionType.STRING, "emoji", "Emoji to be deleted", false));
        this.args.add(new OptionData(OptionType.STRING, "name", "Name of the emoji to be deleted", false));

        this.permission = Permission.MANAGE_GUILD_EXPRESSIONS;
        this.botPermission = Permission.MANAGE_GUILD_EXPRESSIONS;
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue(); 

        var emojiOption = event.getOption("emoji");
        var nameOption = event.getOption("name");

        // Check if either emoji or name is provided
        if (emojiOption == null && nameOption == null) {
            event.getHook().sendMessage(BotConfig.noEmoji() + " Please provide an emoji **OR** the name of the emoji to delete.").setEphemeral(true).queue();
            return;
        }

        var guild = event.getGuild();
        RichCustomEmoji emote = null;

        // If emoji option is provided
        if (emojiOption != null) {
            String emojiInput = emojiOption.getAsString();
            String emojiId = EmojiInput.extractEmojiId(emojiInput);
            if (emojiId != null) { // Custom emoji format
                emote = guild.getEmojiById(emojiId);
                System.out.println("Emoji ID: " + emojiId);
            }
        }

        // If name option is provided or emoji was not found using emoji option
        if (emote == null && nameOption != null) {
            String emojiName = EmojiInput.normalize(nameOption.getAsString());
            emote = guild.getEmojisByName(emojiName, false).stream().findFirst().orElse(null);
            System.out.println("name ile");
        }

        // Check if the emoji exists
        if (emote == null) {
            event.getHook().sendMessage(BotConfig.noEmoji() + " The emoji provided does not exist in this server.").setEphemeral(true).queue();
            return;
        }

        // Delete the emoji
        emote.delete().queue(
                success -> event.getHook().sendMessage(BotConfig.yesEmoji() + " Emoji has been deleted.").setEphemeral(true).queue(),
                error -> event.getHook().sendMessage(BotConfig.noEmoji() + " Failed to delete the emoji.").setEphemeral(true).queue()
        );
    }
}