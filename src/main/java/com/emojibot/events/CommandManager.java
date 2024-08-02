package com.emojibot.events;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.commands.emoji.*;
import com.emojibot.commands.other.*;
import com.emojibot.commands.premium.HideCommand;
import com.emojibot.commands.premium.PremiumCommand;
import com.emojibot.commands.staff.*;
import com.emojibot.utils.Localization;
import com.emojibot.utils.button_listeners.LanguageManager;
import com.emojibot.utils.command.EmojiCommand;

import club.minnced.discord.webhook.WebhookClient;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;


import java.util.*;

/* 
! Read the comment on the commandsDiffer function for information about updating global commands. 
*/

public class CommandManager extends ListenerAdapter {
    public static final ArrayList<EmojiCommand> commands = new ArrayList<>();
    public static final Map<String, EmojiCommand> commandsMap = new HashMap<>();
    private static final Dotenv config = Dotenv.configure().load();
    private static final boolean USE_GLOBAL_COMMANDS = BotConfig.getUseGlobalCommands();
    private final CooldownManager cooldownManager = new CooldownManager();

    public CommandManager(Bot bot) {
        createCommandMap(
            new DeleteCommand(bot),
            new UploadCommand(bot),
            new RenameCommand(bot),
            new RandomCommand(bot),
            new SendCommand(bot),
            new JumboCommand(bot),
            new FastCommand(bot),
            new ListCommand(bot),
            new SearchCommand(bot),
            new EmojifyCommand(bot),
            new LinkCommand(bot),
            new EmojiInfoCommand(bot),
            new ShardCommand(bot),
            new PingCommand(bot),
            new LanguageCommand(bot),
            new CommandsCommand(bot),
            new PremiumCommand(bot),
            new StatsCommand(bot),
            new HideCommand(bot),
            new HelpCommand(bot)
        );
    }

    /**
     * Creates a map of commands and adds them to the list of commands.
     * @param commandList
     */
    private void createCommandMap(EmojiCommand... commandList) {
        for (EmojiCommand command : commandList) {
            commandsMap.put(command.name, command);
            commands.add(command);
        }
    }

    /**
     * Unpacks the command data from the commands list.
     * @return List of CommandData objects to be registered with the bot.
     */
    public static List<CommandData> unpackCommandData() {
        List<CommandData> commandData = new ArrayList<>();
        for (EmojiCommand command : commands) {
            SlashCommandData slashCommand = Commands.slash(command.name, command.description).addOptions(command.args);

            // Localize command name and description
            command.localizedNames.forEach(slashCommand::setNameLocalization);
            command.localizedDescriptions.forEach(slashCommand::setDescriptionLocalization);

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

    /**
     * Handles slash command interactions and runs the command if it exists in the command map.
     */
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        EmojiCommand command = commandsMap.get(event.getName());
        if (command != null) {
            String userId = event.getUser().getId();

            // Check if the user is on cooldown
            if (command.cooldownDuration != null && command.cooldownDuration > 0) {
                if (cooldownManager.isOnCooldown(userId, command.name, command.cooldownDuration)) {
                    // Check if the user has been warned about the cooldown
                    if (!cooldownManager.hasBeenWarned(userId, command.name)) {
                        // Not warned yet, warn the user about the cooldown
                        Localization localization = Localization.getLocalization(userId);

                        long remainingCooldown = cooldownManager.getRemainingCooldown(userId, command.name, command.cooldownDuration);
                        event.reply(String.format(localization.getMsg("command_manager", "cooldown_warning"), remainingCooldown))
                            .setEphemeral(true)
                            .queue();
                        cooldownManager.warnUser(userId, command.name);
                    }
                    // User has already been warned, do not run the command
                    return;
                } else {
                    // Not on cooldown, reset the cooldown and run the command
                    cooldownManager.setCooldown(userId, command.name);
                }
            }

            // Check the permissions of the bot for required permissions
            var selfMember = event.getGuild().getSelfMember();

            if (command.botPermission != null) {
                if (!selfMember.hasPermission(command.botPermission) && !selfMember.hasPermission(Permission.ADMINISTRATOR)) {
                    Localization localization = Localization.getLocalization(userId);

                    event.reply(String.format(localization.getMsg("command_manager", "required_perms"), command.botPermission.getName())).setEphemeral(true).queue();
                    return;
                }
            }

            // If user has not set a language, try to set it based on the user's locale
            // If the user's locale is not supported, ask the user to set a language
            if(!LanguageManager.isUserLanguageSet(userId)) {
                DiscordLocale userLocale = event.getUserLocale();

                switch(userLocale) {
                    case ENGLISH_US:
                        LanguageManager.setUserLanguage(userId, "en");
                        break;
                    case ENGLISH_UK:
                        LanguageManager.setUserLanguage(userId, "en");
                        break;
                    case TURKISH:
                        LanguageManager.setUserLanguage(userId, "tr");
                        break;
                    default:
                        event.deferReply().queue();

                        event.getHook().sendMessage("Your language seems to not be supported by Emoji bot. You can select one of the supported languages below.").setEphemeral(true).queue();

                        LanguageManager.askUserForLanguage(event.getHook());
                        return;
                }
            }
            
            // Try to run the command and catch any exceptions
            try {
                command.run(event);
            } catch (Exception e) {
                try {
                    event.getHook().sendMessage(BotConfig.noEmoji() + " Something went wrong, our team has been informed about this issue.").setEphemeral(true).queue();
                } catch (Exception e2) {
                    event.reply(BotConfig.noEmoji() + " Something went wrong, our team has been informed about this issue.").setEphemeral(true).queue();
                }

                e.printStackTrace();

                // Send the error message to the logs channel with the webhook
                try (WebhookClient client = WebhookClient.withUrl(config.get("URL_LOGS_WEBHOOK"))) {
                    String errMessage = String.format(":warning: Unhandled exception with command %s at guild %s (%s), by user %s (%s):\n```%s```", event.getName(), event.getGuild().getName(), event.getGuild().getId(), event.getUser().getAsMention(), event.getUser().getId(), e.getMessage());
                    client.send(errMessage);
                }
            }
        }
    }


    // GLOBAL COMMAND REGISTRY - Production //

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        if (USE_GLOBAL_COMMANDS) {
            List<Command> existingCommands = event.getJDA().retrieveCommands().complete();
            
            if (commandsDiffer(existingCommands, commands)) {
                System.out.println("Commands differ, updating global commands.");
                event.getJDA().updateCommands().addCommands(unpackCommandData()).queue(
                    success -> System.out.println("Commands updated successfully."),
                    failure -> {
                        long retryAfter = 60000; // Default retry-after period
                        String[] parts = failure.getMessage().split("Retry-After: ");
                        if (parts.length > 1) {
                            try {
                                retryAfter = Long.parseLong(parts[1].split(" ")[0]);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                        System.out.printf("Rate limited while updating global commands. Retrying after %d milliseconds.%n", retryAfter);
                        // Retry logic
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                onReady(event); // Retry updating commands
                            }
                        }, retryAfter);
                    }
                );
            } else {
                System.out.println("Commands are up to date, not updating.");
            }
        } else {
            event.getJDA().updateCommands().queue();
        }
    }

    /*
     ! Warning: commandsDiffer method does NOT check for updates to the command's permissions, it only checks the command's name, description, and options. 
     ! So for some reason, if you just change the permissions of a command, manually update some random value in any command to trigger an update.
    */

    /**
    * Compares the existing commands with the new commands to determine if they differ.
    * @param existingCommands 
    * @param newCommands 
    * @return true if the commands differ by name, description or options
    */
    private boolean commandsDiffer(List<Command> existingCommands, ArrayList<EmojiCommand> newCommands) {
        System.out.println("Comparing command sizes: existing = " + existingCommands.size() + ", new = " + newCommands.size());
    
        if (existingCommands.size() != newCommands.size()) {
            System.out.println("Command sizes differ.");
            return true;
        }
    
        for (int i = 0; i < existingCommands.size(); i++) {
            Command existing = existingCommands.get(i);
            EmojiCommand updated = newCommands.get(i);
    
            //System.out.println("Comparing command at index " + i);
            //System.out.println("Existing command: name = " + existing.getName() + ", description = " + existing.getDescription());
            //System.out.println("New command: name = " + updated.name + ", description = " + updated.description);
    
            if (!existing.getName().equals(updated.name) ||
                !existing.getDescription().equals(updated.description) ||
                !compareOptions(existing.getOptions(), updated.args)) {
                System.out.println("Commands differ at index " + i);
                return true;
            }
        }
    
        //System.out.println("Commands are identical.");
        return false;
    }

    /**
     * Compares the existing options with the new options to determine if they differ.
     * @param existingOptions
     * @param newOptions
     * @return true if options are identical
     */
    private boolean compareOptions(List<Command.Option> existingOptions, List<OptionData> newOptions) {
        //System.out.println("Comparing option sizes: existing = " + existingOptions.size() + ", new = " + newOptions.size());
    
        if (existingOptions.size() != newOptions.size()) {
            System.out.println("Option sizes differ.");
            return false;
        }
    
        for (int i = 0; i < existingOptions.size(); i++) {
            Command.Option existingOption = existingOptions.get(i);
            OptionData newOption = newOptions.get(i);
            
            /*
            System.out.println("Comparing option at index " + i);
            System.out.println("Existing option: name = " + existingOption.getName() +
                               ", description = " + existingOption.getDescription() +
                               ", type = " + existingOption.getType());
            System.out.println("New option: name = " + newOption.getName() +
                               ", description = " + newOption.getDescription() +
                               ", type = " + newOption.getType());
            */

            if (!existingOption.getName().equals(newOption.getName()) ||
                !existingOption.getDescription().equals(newOption.getDescription()) ||
                existingOption.getType() != newOption.getType()) {
                System.out.println("Options differ at index " + i);
                return false;
            }
        }
    
        //System.out.println("Options are identical.");
        return true;
    }



    // GUILD COMMAND REGISTRY - Testing purposes only //

    /**
     * For registering guild commands only on the Emoji Tests server.
     */
    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        // Only register guild commands for the test server
        if (event.getGuild().getId().equals("368839796703100928") || event.getGuild().getId().equals("232918641866178560")) {
            if (!USE_GLOBAL_COMMANDS) {
                registerGuildCommands(event);
            } else {
                clearGuildCommands(event);
            }
        }
    }
    
    /**
     * For registering guild commands for testing purposes only.
     */
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
         // Only register guild commands to Emoji Tests & Emoji Bot server
         if (event.getGuild().getId().equals("368839796703100928") || event.getGuild().getId().equals("232918641866178560")) {
            if (!USE_GLOBAL_COMMANDS) {
                registerGuildCommands(event);
            } else {
                clearGuildCommands(event);
            }
        }
    }

    /**
     * Registers the guild commands if they differ from the existing commands.
     * @param event
     */
    private void registerGuildCommands(GenericGuildEvent event) {
        List<Command> existingCommands = event.getGuild().retrieveCommands().complete();

        if (commandsDiffer(existingCommands, commands)) {
            event.getGuild().updateCommands().addCommands(unpackCommandData()).queue();
        }
    }

    /**
     * Clears the guild commands. (For testing purposes only) - GETS RATE LIMITIED USE WITH CAUTION
     * @param event
     */
    private void clearGuildCommands(GenericGuildEvent event) {
        event.getGuild().updateCommands().queue();
    }


}