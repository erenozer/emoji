package com.emojibot.commands.staff;

import java.util.Objects;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.utils.Localization;
import com.emojibot.utils.command.EmojiCommand;
import com.emojibot.utils.command.EmojiInput;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class RenameCommand extends EmojiCommand {

    public RenameCommand(Bot bot) {
        super(bot);
        this.name = "rename";
        this.description = "Change the name of an existing emoji in your server";
        this.cooldownDuration = 4;

        this.localizedNames.put(DiscordLocale.TURKISH, "adlandır");
        this.localizedDescriptions.put(DiscordLocale.TURKISH, "Sunucunuzdaki mevcut bir emojinin adını değiştirin");

        // Define options
        this.args.add(new OptionData(OptionType.STRING, "emoji", "Emoji itself or emoji's name to be renamed", true, false)
        .setNameLocalization(DiscordLocale.TURKISH, "emoji")
        .setDescriptionLocalization(DiscordLocale.TURKISH, "Yeniden adlandırılacak emojinin kendisi ya da ismi"));

        this.args.add(new OptionData(OptionType.STRING, "new-name", "New name for the emoji", true, false)
        .setNameLocalization(DiscordLocale.TURKISH, "yeni-ad")
        .setDescriptionLocalization(DiscordLocale.TURKISH, "Emojinin yeni adı"));


        this.permission = Permission.MANAGE_GUILD_EXPRESSIONS;
        this.botPermission = Permission.MANAGE_GUILD_EXPRESSIONS;
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue(); 

        String emojiInput = Objects.requireNonNull(event.getOption("emoji").getAsString());
        String nameInput = Objects.requireNonNull(event.getOption("new-name").getAsString());

        Localization localization = Localization.getLocalization(event.getUser().getId());

        if(nameInput.length() > 32 || nameInput.length() < 2) {
            event.getHook().sendMessage(String.format(localization.getMsg("rename_command", "name_length_error"), BotConfig.noEmoji())).queue();
            return;
        }

        String emojiName = EmojiInput.extractEmojiName(emojiInput);

        var guild = event.getGuild();

        // Get the emoji using the name extracted from the input
        RichCustomEmoji emote = guild.getEmojisByName(emojiName, false).stream().findFirst().orElse(null);

        // If emoji is not found
        if (emote == null) {
            event.getHook().sendMessage(String.format(localization.getMsg("rename_command", "emoji_not_found"), BotConfig.noEmoji(), emojiName)).queue();
            return;
        }

        String newName = EmojiInput.removeInvalidEmojiCharacters(nameInput);

        // Rename the emoji
        emote.getManager().setName(newName).reason(event.getUser().toString()).queue(
                success -> event.getHook().sendMessage(String.format(localization.getMsg("rename_command", "success"), BotConfig.yesEmoji(), emojiName, newName)).queue(),
                error -> event.getHook().sendMessage(String.format(localization.getMsg("rename_command", "failed"), BotConfig.noEmoji())).queue()
        );
    }
}