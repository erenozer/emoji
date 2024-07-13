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
        event.deferReply().queue(); 

        var emojiOption = event.getOption("emoji");
        var nameOption = event.getOption("name");

        // Check if either emoji or name is provided
        if (emojiOption == null && nameOption == null) {
            event.getHook().sendMessage(BotConfig.noEmoji() + " Please provide an emoji directly **OR** name of it to delete.").setEphemeral(true).queue();
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
            }
        }

        // If name option is provided or emoji was not found using emoji option
        if (emote == null && nameOption != null) {
            String emojiName = EmojiInput.normalize(nameOption.getAsString());
            emote = guild.getEmojisByName(emojiName, false).stream().findFirst().orElse(null);
        }

        // Emoji not found using both methods
        if (emote == null) {
            event.getHook().sendMessage(BotConfig.noEmoji() + " The emoji provided does not exist in this server.").queue();
            return;
        }

        String emoteName = emote.getName();

        // Delete the emoji
        emote.delete().reason("Responsible " + event.getUser()).queue(
                success -> event.getHook().sendMessage(BotConfig.yesEmoji() + " Emoji \"" + emoteName + "\" has been deleted.").queue(),
                error -> event.getHook().sendMessage(BotConfig.noEmoji() + " Failed to delete the emoji.").queue()
        );
    }
}