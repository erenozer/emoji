package com.emojibot.commands.emoji;

import java.util.Random;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.EmojiCache;
import com.emojibot.commands.utils.Command;
import com.emojibot.commands.utils.UsageTerms;
import com.emojibot.events.ButtonListener;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class RandomCommand extends Command {
    private EmojiCache emojiCache;

    public RandomCommand(Bot bot) {
        super(bot);
        this.name = "random";
        this.description = "Receive random emojis from the bot!";
        this.args.add(new OptionData(OptionType.INTEGER, "count", "Random emoji count, up to 25", false));
        this.cooldownDuration = 5;
        this.emojiCache = bot.getEmojiCache();
        this.botPermission = Permission.MESSAGE_EXT_EMOJI;
    }


    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        // Check for usage terms 
        if(UsageTerms.checkUserStatus(event.getUser().getId()) != 1) {
            // User has not accepted the terms
            UsageTerms.validateTerms(event.getHook());
            return;
        }
            

        var countInput = event.getOption("count");
        if(countInput != null) {
            // Count is provided, get the value
            int count = (int)countInput.getAsInt();

            if(count < 1 || count > 25) {
                event.getHook().sendMessage(String.format("%s Please provide a count between 1 and 25.", BotConfig.noEmoji())).queue();
                return;
            }

            var randomEmojis = emojiCache.getRandomEmojis(count);

            if(randomEmojis.isEmpty()) {
                event.getHook().sendMessage(String.format("%s I couldn't find any emojis to send.", BotConfig.noEmoji())).queue();
                return;
            }

            StringBuilder emojiMentions = new StringBuilder();

            for (RichCustomEmoji emoji : randomEmojis) {
                emojiMentions.append(emoji.getAsMention()).append(" ");
            }

            // Remove the trailing space
            if (emojiMentions.length() > 0) {
                emojiMentions.setLength(emojiMentions.length() - 1);
            }

            event.getHook().sendMessage(emojiMentions.toString()).queue();

        } else {
            // Count is not provided, get a single random emoji
            var randomEmoji = emojiCache.getRandomEmojis(1).stream().findFirst().orElse(null);

            if(randomEmoji == null) {
                event.getHook().sendMessage(String.format("%s I couldn't find any emojis to send.", BotConfig.noEmoji())).queue();
                return;
            }

            Random random = new Random();
            int randomNumber = random.nextInt(5,26);
            // Random number can be 5 6 7 ... 25
            if(randomNumber > 12) {
                event.getHook().sendMessage(String.format("%s **Tip:** You can get more than one random emoji at once! Try /random %d", BotConfig.infoEmoji(), randomNumber)).queue();
            }

            event.getHook().sendMessage(randomEmoji.getAsMention()).queue();
        }
    }



}