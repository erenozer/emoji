package com.emojibot.commands.staff;

import java.util.Objects;

import org.w3c.dom.NameList;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.commands.utils.Command;
import com.emojibot.commands.utils.EmojiInput;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class RenameCommand extends Command {

    public RenameCommand(Bot bot) {
        super(bot);
        this.name = "rename";
        this.description = "Change the name of an existing emoji in your server";
        this.cooldownDuration = 4;

        // Define options
        this.args.add(new OptionData(OptionType.STRING, "emoji", "Emoji itself or emoji's name to be renamed", true, false));
        this.args.add(new OptionData(OptionType.STRING, "new-name", "New name for the emoji", true, false));

        this.permission = Permission.MANAGE_GUILD_EXPRESSIONS;
        this.botPermission = Permission.MANAGE_GUILD_EXPRESSIONS;
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue(); 

        String emojiInput = Objects.requireNonNull(event.getOption("emoji").getAsString());
        String nameInput = Objects.requireNonNull(event.getOption("new-name").getAsString());

        if(nameInput.length() > 32 || nameInput.length() < 2) {
            event.getHook().sendMessage(String.format("%s Emoji name must be longer than 2, shorter than 32 characters.", BotConfig.noEmoji())).queue();
            return;
        }

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
        String newName = EmojiInput.removeInvalidEmojiCharacters(nameInput);
        // Rename the emoji
        emote.getManager().setName(newName).reason("Responsible " + event.getUser()).queue(
                success -> event.getHook().sendMessage(String.format("%s Emoji \"%s\" has been renamed to \"%s\".", BotConfig.yesEmoji(), emojiName, newName)).queue(),
                error -> event.getHook().sendMessage(String.format("%s I couldn't rename the emoji, make sure the name only contains English letters.", BotConfig.noEmoji())).queue()
        );
    }
}