package com.emojibot.commands.staff;

import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.utils.EmojiCache;
import com.emojibot.utils.Localization;
import com.emojibot.utils.button_listeners.PremiumManager;
import com.emojibot.utils.button_listeners.UsageTerms;
import com.emojibot.utils.command.EmojiCommand;
import com.emojibot.utils.command.TopggManager;
import com.emojibot.utils.menu_listeners.SelectMenuListener;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Icon;

public class RandomUploadCommand extends EmojiCommand {
    private EmojiCache emojiCache;

    public RandomUploadCommand(Bot bot) {
        super(bot);
        this.name = "random-upload";
        this.description = "Uploads multiple random emojis to your server easily";
        this.cooldownDuration = 20;

        this.localizedNames.put(DiscordLocale.TURKISH, "rastgele-yükle");
        this.localizedDescriptions.put(DiscordLocale.TURKISH, "Sunucunuza yeni rastgele emojiler yükler!");

        OptionData option = new OptionData(OptionType.INTEGER, "count", "Emoji count, up to 20 emojis at once", false);
        option.setNameLocalization(DiscordLocale.TURKISH, "adet");
        option.setDescriptionLocalization(DiscordLocale.TURKISH, "Rastgele emoji adeti, aynı anda en fazla 20 emoji");

        this.args.add(option);

        this.emojiCache = bot.getEmojiCache();

        this.permission = Permission.MANAGE_GUILD_EXPRESSIONS;
        this.botPermission = Permission.MANAGE_GUILD_EXPRESSIONS;
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        String userId = event.getUser().getId();
        Localization localization = Localization.getLocalization(userId);
        TopggManager topggManager = bot.getTopggManager();

        boolean isPremium = PremiumManager.getPremiumStatus(event.getGuild().getId());
        boolean hasVoted = topggManager.hasVoted(userId);

        if (!hasVoted && !isPremium) {
            TopggManager.sendVoteEmbed(event.getHook(), localization);
            return;
        }

        if (UsageTerms.checkUserStatus(event.getUser().getId()) != 1) {
            UsageTerms.validateTerms(event.getHook());
            return;
        }

        var countInput = event.getOption("count");
        int count = (countInput != null) ? (int) countInput.getAsInt() : 1;

        if (count < 1 || count > 20) {
            event.getHook().sendMessage(String.format(localization.getMsg("randomupload_command", "invalid_count"), BotConfig.noEmoji())).queue();
            return;
        }

        List<RichCustomEmoji> randomEmojis = emojiCache.getRandomEmojis(count);
        if (randomEmojis.isEmpty()) {
            event.getHook().sendMessage(String.format(localization.getMsg("random_command", "no_emojis"), BotConfig.noEmoji())).queue();
            return;
        }

        // Set to keep track of unique emoji IDs
        Set<String> uniqueEmojiIds = new HashSet<>();

        // Create a select menu with a unique ID for the user and required click count (range 1 to count)
        StringSelectMenu.Builder selectMenu = StringSelectMenu.create(SelectMenuListener.createUniqueId(userId) + ":random_emojis")
                .setPlaceholder(localization.getMsg("randomupload_command", "click_here"))
                .setRequiredRange(1, count);

        // Add the options to the select menu with the emoji image
        for (RichCustomEmoji emoji : randomEmojis) {
            if (uniqueEmojiIds.add(emoji.getId())) {
                selectMenu.addOption(emoji.getName(), emoji.getId(), Emoji.fromFormatted(emoji.getAsMention()));
            }
        }

        event.getHook().sendMessage(String.format(localization.getMsg("randomupload_command", "select_below"), count))
                .addActionRow(selectMenu.build())
                .queue();
    }

    public static void handleSelect(StringSelectInteractionEvent event) {
        if (!event.getComponentId().contains(":random_emojis")) return;
    
        Localization localization = Localization.getLocalization(event.getUser().getId());
        List<String> selectedEmojiIds = event.getValues();
        AtomicBoolean failEncountered = new AtomicBoolean(false);
    
        Runnable uploadEmojis = () -> {
            for (String emojiId : selectedEmojiIds) {
                RichCustomEmoji emoji = event.getJDA().getEmojiById(emojiId);
                if (emoji == null) {
                    // Log the issue or notify the user that the emoji could not be found
                    failEncountered.set(true);
                    continue;
                }
    
                try {
                    // Check if the emoji image can be fetched
                    InputStream inputStream = new URL(emoji.getImageUrl()).openStream();
                    Icon icon = Icon.from(inputStream);
                    
                    // Ensure the rate limit is respected
                    TimeUnit.MILLISECONDS.sleep(500);
    
                    event.getGuild().createEmoji(emoji.getName(), icon)
                            .queue(
                                success -> {
                                    // Log successful upload if needed
                                },
                                failure -> {
                                    failEncountered.set(true);
                                    // Log failure for debugging
                                }
                            );
                } catch (Exception e) {
                    failEncountered.set(true);
                    // Log exception for debugging
                }
            }
        };
    
        // Use a new thread to handle async processing
        new Thread(uploadEmojis).start();
    
        // Known issue: The response might be sent before the emojis are fully uploaded
        String response;
        if (failEncountered.get()) {
            response = String.format(localization.getMsg("randomupload_command", "success_with_failed"), BotConfig.yesEmoji());
        } else {
            response = String.format(localization.getMsg("randomupload_command", "success"), BotConfig.yesEmoji());
        }
    
        // Edit the original message to remove the select menu and add the result message
        event.editMessage(response).setComponents().queue();
    }

    
}