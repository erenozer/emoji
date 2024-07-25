package com.emojibot.commands.emoji;

import java.util.Random;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.EmojiCache;
import com.emojibot.commands.utils.Command;
import com.emojibot.commands.utils.UsageTerms;
import com.emojibot.commands.utils.language.Localization;

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
        this.args.add(new OptionData(OptionType.INTEGER, "count", "Emoji count, up to 20 emojis at once", false));
        this.cooldownDuration = 4;
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
            
        Localization localization = Localization.getLocalization(event.getUser().getId());

        var countInput = event.getOption("count");
        if(countInput != null) {
            // Count is provided, get the value
            int count = (int)countInput.getAsInt();

            if(count < 1 || count > 20) {
                event.getHook().sendMessage(String.format(localization.getMsg("random_command", "invalid_count"), BotConfig.noEmoji())).queue();
                return;
            }

            var randomEmojis = emojiCache.getRandomEmojis(count);

            if(randomEmojis.isEmpty()) {
                event.getHook().sendMessage(String.format(localization.getMsg("random_command", "no_emojis"), BotConfig.noEmoji())).queue();
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
                event.getHook().sendMessage(String.format(localization.getMsg("random_command", "no_emojis"), BotConfig.noEmoji())).queue();
                return;
            }

            Random random = new Random();
            int randomNumber = random.nextInt(5,21);
            // Random number can be 5 6 7 ... 20 (valid counts for random emojis)

            event.getHook().sendMessage(randomEmoji.getAsMention()).queue();

            if(randomNumber > 12) {
                // On some occasions, suggest the user to get more than one random emoji
                event.getHook().sendMessage(String.format(localization.getMsg("random_command", "multiple_tip"), BotConfig.infoEmoji(), randomNumber)).setEphemeral(true).queue();
            }
        }
    }

}