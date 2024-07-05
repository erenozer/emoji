package com.emojibot.commands.emoji;

import com.emojibot.EmojiCache;
import com.emojibot.commands.Command;
import com.emojibot.Bot;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class SearchCommand extends Command {
    private final EmojiCache emojiCache;

    public SearchCommand(Bot bot) {
        super(bot);
        this.name = "search";
        this.description = "Search for specific emojis.";

        OptionData emojiNameArgument = new OptionData(OptionType.STRING, "name", "Emoji name to be searched", true);
        this.args.add(emojiNameArgument);

        this.emojiCache = bot.getEmojiCache();
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        String emojiName = event.getOption("name").getAsString();
        List<RichCustomEmoji> emojiList = emojiCache.getEmojis(emojiName);

        if (emojiList != null && !emojiList.isEmpty()) {
            StringBuilder replyMessage = new StringBuilder();
            for (RichCustomEmoji emoji : emojiList) {
                replyMessage.append(emoji.getAsMention()).append(" ");
            }
            event.reply(replyMessage.toString()).queue();
        } else {
            event.reply("No emojis found with that name.").queue();
        }
    }
}