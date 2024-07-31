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

public class DeleteCommand extends EmojiCommand {

    public DeleteCommand(Bot bot) {
        super(bot);
        this.name = "delete";
        this.description = "Delete an emoji from your server, either by emoji or name";
        this.cooldownDuration = 4;

        this.localizedNames.put(DiscordLocale.TURKISH, "sil");
        this.localizedDescriptions.put(DiscordLocale.TURKISH, "Sunucunuzdan bir emoji siler, emoji ya da ismi ile");

        
        OptionData option = new OptionData(OptionType.STRING, "emoji", "Emoji itself or emoji's name to be deleted", true);
        option.setNameLocalization(DiscordLocale.TURKISH, "emoji");
        option.setDescriptionLocalization(DiscordLocale.TURKISH, "Silinecek emojinin kendisi ya da ismi");
        this.args.add(option);

        this.permission = Permission.MANAGE_GUILD_EXPRESSIONS;
        this.botPermission = Permission.MANAGE_GUILD_EXPRESSIONS;
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue(); 

        Localization localization = Localization.getLocalization(event.getUser().getId());

        String emojiInput = Objects.requireNonNull(event.getOption("emoji")).getAsString();
        String emojiName = EmojiInput.extractEmojiName(emojiInput);

        var guild = event.getGuild();
        RichCustomEmoji emote = null;

        // Get the emoji using the name extracted from the input
        emote = guild.getEmojisByName(emojiName, false).stream().findFirst().orElse(null);

        // If emoji is not found
        if (emote == null) {
            event.getHook().sendMessage(String.format(localization.getMsg("delete_command", "emoji_not_found"), BotConfig.noEmoji(), emojiName)).queue();
            return;
        }

        // Delete the emoji
        emote.delete().reason(event.getUser().toString()).queue(
                success -> event.getHook().sendMessage(String.format(localization.getMsg("delete_command", "deleted_emoji"), BotConfig.yesEmoji(), emojiName)).queue(),
                error -> event.getHook().sendMessage(String.format(localization.getMsg("delete_command", "failed"), BotConfig.noEmoji())).queue()
        );
    }
}