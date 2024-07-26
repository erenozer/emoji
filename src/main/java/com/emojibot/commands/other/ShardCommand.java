package com.emojibot.commands.other;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.commands.utils.EmojiCommand;
import com.emojibot.commands.utils.language.Localization;
import com.emojibot.events.ButtonListener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.sharding.ShardManager;

public class ShardCommand extends EmojiCommand {

    private static class CurrentValues {
        int currentPage;
        int totalPages;
        int pageSize;
        List<JDA> shards;

        public CurrentValues(int currentPage, int totalPages, int pageSize, List<JDA> shards) {
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.pageSize = pageSize;
            this.shards = shards;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public int getPageSize() {
            return pageSize;
        }

        public List<JDA> getShards() {
            return shards;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }
    }

    private static final Map<String, CurrentValues> currentValues = new HashMap<>();
    private static final Timer timer = new Timer(); // Timer for session expiration

    public ShardCommand(Bot bot) {
        super(bot);
        this.name = "shard";
        this.description = "Displays the shards of the bot";
        this.cooldownDuration = 20;
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        ShardManager shardManager = event.getJDA().getShardManager();

        if (shardManager == null) {
            event.getHook().sendMessage(String.format("%s ShardManager not found.", BotConfig.noEmoji())).setEphemeral(true).queue();
            return;
        }

        List<JDA> shards = shardManager.getShards();
        int pageSize = 10;

        // Calculate total pages
        int totalPages = (int) Math.ceil((double) shards.size() / pageSize);

        // Create a unique session id for the validity of the buttons
        String sessionId = ButtonListener.createUniqueId(event.getUser().getId());

        // Put the current status of the buttons in the map with sessionId as key
        currentValues.put(sessionId, new CurrentValues(1, totalPages, pageSize, shards));

        // Show the first page initially
        showPage(event, shards, 1, totalPages, pageSize, sessionId);

        // Schedule expiration of the buttons after 2 minutes
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                expireSession(sessionId, event);
            }
        }, Duration.ofSeconds(150).toMillis());
    }

    private static void showPage(SlashCommandInteractionEvent event, List<JDA> shards, int currentPage, int totalPages, int pageSize, String sessionId) {
        showPage(event.getHook(), event.getUser().getId(), shards, currentPage, totalPages, pageSize, sessionId);
    }

    private static void showPage(ButtonInteractionEvent event, List<JDA> shards, int currentPage, int totalPages, int pageSize, String sessionId) {
        showPage(event.getHook(), event.getUser().getId(), shards, currentPage, totalPages, pageSize, sessionId);
    }

    private static void showPage(InteractionHook hook, String userId, List<JDA> shards, int currentPage, int totalPages, int pageSize, String sessionId) {
        Localization localization = Localization.getLocalization(userId);
        // Edit the message to show the given currentPage
        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, shards.size());

        StringBuilder shardString = new StringBuilder();
        for (int i = startIndex; i < endIndex; i++) {
            JDA shard = shards.get(i);
            long guildCount = shard.getGuildCache().size();
            String shardStatus = formatShardStatus(shard.getStatus());
            long latency = shard.getGatewayPing();

            shardString.append(String.format("**Shard %d %s**\n", i + 1, shardStatus))
                       .append(String.format(localization.getMsg("shard_command", "content"), guildCount, latency));
        }

        // Create buttons based on current page
        List<ItemComponent> buttons = new ArrayList<>();
        if (totalPages >= 0) {
            String prevButtonId = sessionId + ":shard:previous";
            String nextButtonId = sessionId + ":shard:next";

            Button currentPageButton = Button.of(ButtonStyle.SECONDARY, "disabled", String.format(localization.getMsg("shard_command", "page"), currentPage, totalPages), Emoji.fromFormatted(BotConfig.infoEmoji())).withDisabled(true);
            Button previousButton = Button.of(ButtonStyle.PRIMARY, prevButtonId, localization.getMsg("shard_command", "previous_page"), Emoji.fromUnicode("⬅")).withDisabled(currentPage == 1);
            Button nextButton = Button.of(ButtonStyle.PRIMARY, nextButtonId, localization.getMsg("shard_command", "next_page"), Emoji.fromUnicode("➡")).withDisabled(currentPage == totalPages);

            buttons.add(currentPageButton);
            buttons.add(previousButton);
            buttons.add(nextButton);
        }

        // Send the embed with buttons
        hook.editOriginal(shardString.toString()).setComponents(ActionRow.of(buttons)).queue();
    }

   
    public static void handleClick(ButtonInteractionEvent event, boolean isNext) {
        event.deferEdit().queue();

        String[] contents = event.getComponentId().split(":");

        // get the UUID:UserId value to get the current page
        String uniqueID = String.format("%s:%s", contents[0], contents[1]);

        if (!currentValues.containsKey(uniqueID)) {
            // Older button, ignore
            return;
        }

        CurrentValues values = currentValues.get(uniqueID);
        int currentPage = values.getCurrentPage();
        int totalPages = values.getTotalPages();
        int pageSize = values.getPageSize();
        List<JDA> shards = values.getShards();

        int newPage = currentPage;

        if (isNext) {
            newPage++;
        } else {
            newPage--;
        }

        values.setCurrentPage(newPage);
        showPage(event, shards, newPage, totalPages, pageSize, uniqueID);
    }

    private static void expireSession(String sessionId, SlashCommandInteractionEvent event) {
        CurrentValues values = currentValues.remove(sessionId); // Remove session data and get values

        if (values == null) {
            return; // Session not found, nothing to expire
        }

        // Retrieve interaction hook and update embed with expired message
        InteractionHook hook = event.getHook();
        if (hook == null) {
            return; // Interaction hook not available
        }

        Localization localization = Localization.getLocalization(event.getUser().getId());

        MessageEmbed expiredEmbed = new EmbedBuilder()
                .addField(localization.getMsg("shard_command", "button_expired"), localization.getMsg("shard_command", "button_expired_desc"), true)
                .setColor(BotConfig.getGeneralEmbedColor())
                .build();

        // Remove the buttons
        hook.editOriginalEmbeds(expiredEmbed).setComponents().queue();
    }

    private static String formatShardStatus(JDA.Status status) {
        return STATUS_MAP.getOrDefault(status, status.toString());
    }

    private static final Map<JDA.Status, String> STATUS_MAP = createStatusMap();

    private static Map<JDA.Status, String> createStatusMap() {
        Map<JDA.Status, String> statusMap = new HashMap<>();
        statusMap.put(JDA.Status.INITIALIZING, "<a:loading:1266077391172800654>");
        statusMap.put(JDA.Status.INITIALIZED, "<a:loading:1266077391172800654>");
        statusMap.put(JDA.Status.LOGGING_IN, "<a:loading:1266077391172800654>");
        statusMap.put(JDA.Status.CONNECTING_TO_WEBSOCKET, "<a:loading:1266077391172800654>");
        statusMap.put(JDA.Status.IDENTIFYING_SESSION, "<a:loading:1266077391172800654>");
        statusMap.put(JDA.Status.AWAITING_LOGIN_CONFIRMATION, "<a:loading:1266077391172800654>");
        statusMap.put(JDA.Status.LOADING_SUBSYSTEMS, "<a:loading:1266077391172800654>");
        statusMap.put(JDA.Status.CONNECTED, "<:online:673508047162834944>");
        statusMap.put(JDA.Status.DISCONNECTED, "<:dnd:673508077357760522>");
        statusMap.put(JDA.Status.RECONNECT_QUEUED, "<:idle:1266077036691198087>");
        statusMap.put(JDA.Status.WAITING_TO_RECONNECT, "<:idle:1266077036691198087>");
        statusMap.put(JDA.Status.ATTEMPTING_TO_RECONNECT, "<a:loading:1266077391172800654>");
        statusMap.put(JDA.Status.SHUTTING_DOWN, "<:dnd:673508077357760522>");
        statusMap.put(JDA.Status.SHUTDOWN, "<:dnd:673508077357760522>");
        statusMap.put(JDA.Status.FAILED_TO_LOGIN, "<:dnd:673508077357760522>");
        return statusMap;
    }
}