package com.emojibot.commands.emoji;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.utils.EmojiCache;
import com.emojibot.utils.Localization;
import com.emojibot.utils.command.EmojiCommand;
import com.emojibot.utils.command.EmojiInput;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;


public class SendCommand extends EmojiCommand {
    private EmojiCache emojiCache;


    public SendCommand(Bot bot) {
        super(bot);
        this.name = "send";
        this.description = "Sends any custom emoji using it's name only";

        this.localizedNames.put(DiscordLocale.TURKISH, "gönder");
        this.localizedDescriptions.put(DiscordLocale.TURKISH, "Sadece ismi ile özel bir emoji gönderir");

        OptionData option = new OptionData(OptionType.STRING, "name", "Name of an emoji to send", true);
        option.setNameLocalization(DiscordLocale.TURKISH, "emoji-ismi");
        option.setDescriptionLocalization(DiscordLocale.TURKISH, "Gönderilecek emojinin ismi");

        this.args.add(option);
        this.cooldownDuration = 4;
        this.emojiCache = bot.getEmojiCache();
        this.botPermission = Permission.MESSAGE_EXT_EMOJI;
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        Localization localization = Localization.getLocalization(event.getUser().getId());

        String emojiInput = event.getOption("name").getAsString();

        String emojiName = (EmojiInput.extractEmojiName(emojiInput));
        String emojiId = EmojiInput.extractEmojiId(emojiInput);

        if(emojiId != null) {
            event.getHook().sendMessage(String.format(localization.getMsg("send_command", "only_name"), BotConfig.noEmoji())).queue();
            return;
        }

        String formattedEmoji = null;

        // Search for it in the server using the name
        var emoji = event.getGuild().getEmojisByName(emojiName, false).stream().findFirst().orElse(null);

        // If not found in the server, search for the emoji in the bot's cache 
        if(emoji == null) {
            emoji = emojiCache.getEmojis(emojiName).stream().findAny().orElse(null);
        }

        // If emoji is found using either method, get it as mention
        if(emoji != null) {
            formattedEmoji = emoji.getAsMention();
        } 

        if(formattedEmoji == null) {
            event.getHook().sendMessage(String.format(localization.getMsg("send_command", "emoji_not_found"), BotConfig.noEmoji())).queue();
            return;
        }

        event.getHook().sendMessage(formattedEmoji).queue();

    }
}
