package com.emojibot.events;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.commands.emoji.*;
import com.emojibot.commands.other.*;
import com.emojibot.commands.staff.*;
import com.emojibot.commands.utils.Command;

import com.emojibot.events.CooldownManager;
import club.minnced.discord.webhook.WebhookClient;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CommandManager extends ListenerAdapter {
    // Commands list
    public static final ArrayList<Command> commands = new ArrayList<>();
    // Commands map for quick access
    public static final Map<String, Command> commandsMap = new HashMap<>();
    // Config for webhook URLs
    private static final Dotenv config = Dotenv.configure().load();
    // Flag to use global commands or guild command
    private static final boolean USE_GLOBAL_COMMANDS = BotConfig.getUseGlobalCommands();
    // Cooldown manager
    private final CooldownManager cooldownManager = new CooldownManager();

    public CommandManager(Bot bot) {
        // Register all the commands
        createCommandMap(
                new DeleteCommand(bot),
                new UploadCommand(bot),
                new RenameCommand(bot),

                new ListCommand(bot),
                new SearchCommand(bot),
                new EmojifyCommand(bot),
                new LinkCommand(bot),
                new EmojiInfoCommand(bot),

                new PingCommand(bot),
                new HelpCommand(bot)
        );
    }

    private void createCommandMap(Command ...commandList) {
        for(Command command : commandList) {
            commandsMap.put(command.name, command);
            commands.add(command);
        }
    }

    public static List<CommandData> unpackCommandData() {
        // Register slash commands
        List<CommandData> commandData = new ArrayList<>();
        for (Command command : commands) {
            SlashCommandData slashCommand = Commands.slash(command.name, command.description).addOptions(command.args);
            if (command.permission != null) {
                slashCommand.setDefaultPermissions(DefaultMemberPermissions.enabledFor(command.permission));
            }
            if (!command.subCommands.isEmpty()) {
                slashCommand.addSubcommands(command.subCommands);
            }
            commandData.add(slashCommand);
        }
        return commandData;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // Get command by name
        Command command = commandsMap.get(event.getName());
        if (command != null) {
            String userId = event.getUser().getId();
            // Check cooldown
            if (command.cooldownDuration != null && command.cooldownDuration > 0) {
                if (cooldownManager.isOnCooldown(userId, command.name, command.cooldownDuration)) {
                    if (!cooldownManager.hasBeenWarned(userId, command.name)) {
                        long remainingCooldown = cooldownManager.getRemainingCooldown(userId, command.name, command.cooldownDuration);
                        event.reply(String.format(":turtle: You are on **cooldown**! Please wait %d second(s) before using this command again, further attemps will be ignored.", remainingCooldown))
                                .setEphemeral(true)
                                .queue();
                        cooldownManager.warnUser(userId, command.name);
                    }
                    // Ignore attemps from already warned users
                    return;
                } else {
                    cooldownManager.setCooldown(userId, command.name);
                }
            }

            // Check for required bot permissions
            Role botRole = event.getGuild().getBotRole();
            if (botRole != null && command.botPermission != null) {
                if (!botRole.hasPermission(command.botPermission) && !botRole.hasPermission(Permission.ADMINISTRATOR)) {
                    String text = BotConfig.noEmoji() + " I need **" + command.botPermission.getName() + "** permission to execute that command.";
                    event.reply(text).setEphemeral(true).queue();
                    return;
                }
            }

            // Try to run the command, if something fails, catch the exception and log it
            try {
                command.run(event);
            } catch (Exception e) {
                // Send a message to the user that something went wrong
                try {
                    event.getHook().sendMessage(BotConfig.noEmoji() + " Something went wrong, our team has been informed about this issue.").setEphemeral(true).queue();
                } catch (Exception e2) {
                    event.reply(BotConfig.noEmoji() + " Something went wrong, our team has been informed about this issue.").setEphemeral(true).queue();

                }

                e.printStackTrace();

                try (WebhookClient client = WebhookClient.withUrl(config.get("URL_LOGS_WEBHOOK"))) {
                    String errMessage = String.format(":warning: Unhandled exception with command %s at guild %s (%s), by user %s (%s):\n%s", event.getName(), event.getGuild().getName(), event.getGuild().getId(), event.getUser().getAsMention(), event.getUser().getId(), e.getMessage());
                    client.send(errMessage);
                } 
            }
        }
    }
    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        if (!USE_GLOBAL_COMMANDS) {
            registerGuildCommands(event);
        } else {
            clearGuildCommands(event);
        }
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        if (!USE_GLOBAL_COMMANDS) {
            registerGuildCommands(event);
        } else {
            clearGuildCommands(event);
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        if (USE_GLOBAL_COMMANDS) {
            event.getJDA().updateCommands().addCommands(unpackCommandData()).queue();
        }
    }

    private void registerGuildCommands(GenericGuildEvent event) {
        event.getGuild().updateCommands().addCommands(unpackCommandData()).queue(succ -> {}, fail -> {});
    }

    private void clearGuildCommands(GenericGuildEvent event) {
        event.getGuild().updateCommands().queue(succ -> {}, fail -> {});
    }
}
