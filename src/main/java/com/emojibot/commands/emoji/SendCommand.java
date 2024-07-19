package com.emojibot.commands.emoji;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.EmojiCache;
import com.emojibot.commands.utils.Command;
import com.emojibot.commands.utils.EmojiInput;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;


public class SendCommand extends Command {
    private EmojiCache emojiCache;


    public SendCommand(Bot bot) {
        super(bot);
        this.name = "send";
        this.description = "Sends any custom emoji using it's name only";
        this.args.add(new OptionData(OptionType.STRING, "name", "Name of an emoji to send", true));
        this.cooldownDuration = 4;
        this.emojiCache = bot.getEmojiCache();
        this.botPermission = Permission.MESSAGE_EXT_EMOJI;

    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        String emojiInput = event.getOption("name").getAsString();

        String emojiName = (EmojiInput.extractEmojiName(emojiInput));
        String emojiId = EmojiInput.extractEmojiId(emojiInput);
        if(emojiId != null) {
            event.getHook().sendMessage(String.format("%s Please only write the **name** of the emoji for me to send. Otherwise, you can send the emoji yourself. :^)", BotConfig.noEmoji())).queue();
            return;
        }

        String formattedEmoji = null;

        // Search for it in the server using the name
        var emoji = event.getGuild().getEmojisByName(emojiName, false).stream().findFirst().orElse(null);

        // If not found in the server, search for the emoji in the bot's cache 
        if(emoji == null) {
            emoji = emojiCache.getEmojis(emojiName).stream().findFirst().orElse(null);
        }

        // If emoji is found using either method, get it as mention
        if(emoji != null) {
            formattedEmoji = emoji.getAsMention();
        } 

        if(formattedEmoji == null) {
            event.getHook().sendMessage(String.format("%s I can't find the emoji, please send the emoji itself or the name of it.", BotConfig.noEmoji())).queue();
            return;
        }

        event.getHook().sendMessage(formattedEmoji).queue();

    }
}
