package com.emojibot.commands.staff;

import java.util.Objects;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.commands.utils.EmojiCommand;
import com.emojibot.commands.utils.EmojiInput;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class DeleteCommand extends EmojiCommand {

    public DeleteCommand(Bot bot) {
        super(bot);
        this.name = "delete";
        this.description = "Delete an emoji from your server, either by emoji or name";
        this.cooldownDuration = 4;

        // Define options
        this.args.add(new OptionData(OptionType.STRING, "emoji", "Emoji itself or emoji's name to be deleted", true));

        this.permission = Permission.MANAGE_GUILD_EXPRESSIONS;
        this.botPermission = Permission.MANAGE_GUILD_EXPRESSIONS;
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue(); 
        String emojiInput = Objects.requireNonNull(event.getOption("emoji")).getAsString();
        String emojiName = EmojiInput.extractEmojiName(emojiInput);

        var guild = event.getGuild();
        RichCustomEmoji emote = null;

        // Get the emoji using the name extracted from the input
        emote = guild.getEmojisByName(emojiName, false).stream().findFirst().orElse(null);

        // If emoji is not found
        if (emote == null) {
            event.getHook().sendMessage(String.format("%s The emoji with name \"%s\" does not exist in this server.", BotConfig.noEmoji(), emojiName)).queue();
            return;
        }

        // Delete the emoji
        emote.delete().reason("Responsible " + event.getUser()).queue(
                success -> event.getHook().sendMessage(String.format("%s Emoji \"%s\" has been deleted.", BotConfig.yesEmoji(), emojiName)).queue(),
                error -> event.getHook().sendMessage(String.format("%s Failed to delete the emoji.", BotConfig.noEmoji())).queue()
        );
    }
}