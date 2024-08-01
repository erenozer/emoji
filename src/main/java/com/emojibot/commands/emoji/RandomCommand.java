package com.emojibot.commands.emoji;

import java.util.Random;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.utils.EmojiCache;
import com.emojibot.utils.Localization;
import com.emojibot.utils.button_listeners.PremiumManager;
import com.emojibot.utils.button_listeners.UsageTerms;
import com.emojibot.utils.command.EmojiCommand;
import com.emojibot.utils.command.TopggManager;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class RandomCommand extends EmojiCommand {
    private EmojiCache emojiCache;

    public RandomCommand(Bot bot) {
        super(bot);
        this.name = "random";
        this.description = "Receive random emojis from the bot!";
        this.cooldownDuration = 4;

        this.localizedNames.put(DiscordLocale.TURKISH, "rastgele");
        this.localizedDescriptions.put(DiscordLocale.TURKISH, "Rastgele emojiler ile yeni emojiler bulun!");

        OptionData option = new OptionData(OptionType.INTEGER, "count", "Emoji count, up to 20 emojis at once", false);
        option.setNameLocalization(DiscordLocale.TURKISH, "sayı");
        option.setDescriptionLocalization(DiscordLocale.TURKISH, "Emoji sayısı, aynı anda en fazla 20 emoji");

        this.args.add(option);

        this.emojiCache = bot.getEmojiCache();
        this.botPermission = Permission.MESSAGE_EXT_EMOJI;
    }


    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        String userId = event.getUser().getId();

        Localization localization = Localization.getLocalization(userId);

        TopggManager topggManager = bot.getTopggManager();
        
        boolean isPremium = PremiumManager.getPremiumStatus(event.getGuild().getId());
        boolean hasVoted = topggManager.hasVoted(userId);

        if(!hasVoted && !isPremium) {
            // User has not voted and the server is not premium, ask for vote
            TopggManager.sendVoteEmbed(event.getHook(), localization);
            return;
        }

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

            if(count < 1) {
                event.getHook().sendMessage(String.format(localization.getMsg("random_command", "lower_bound"), BotConfig.noEmoji())).queue();
                return;
            }

            if(count > 20 && !isPremium) {
                event.getHook().sendMessage(String.format(localization.getMsg("random_command", "upper_bound_no_premium"), BotConfig.infoEmoji())).queue();
                return;
            }

            if(count > 50) {
                event.getHook().sendMessage(String.format(localization.getMsg("random_command", "upper_bound"), BotConfig.noEmoji())).queue();
                return;
            }

            // Get random emojis as a list
            var randomEmojis = emojiCache.getRandomEmojis(count);

            if(randomEmojis.isEmpty()) {
                event.getHook().sendMessage(String.format(localization.getMsg("random_command", "no_emojis"), BotConfig.noEmoji())).queue();
                return;
            }

            StringBuilder emojiMentions = new StringBuilder();

            // Turn the list of emojis into mentions in a single message
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