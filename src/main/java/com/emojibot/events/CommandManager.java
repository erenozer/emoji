package com.emojibot.events;

import com.emojibot.Bot;
import com.emojibot.commands.Command;
import com.emojibot.commands.emoji.EmojiInfoCommand;
import com.emojibot.commands.emoji.EmojifyCommand;
import com.emojibot.commands.emoji.LinkCommand;
import com.emojibot.commands.emoji.SearchCommand;
import com.emojibot.commands.util.PingCommand;



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
    public static final ArrayList<Command> commands = new ArrayList<>();
    public static final Map<String, Command> commandsMap = new HashMap<>();


    /*
    @Override
    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commands = new ArrayList<>();
        commands.add(Commands.slash("ping", "Pong!"));

        OptionData num1 = new OptionData(OptionType.INTEGER, "num1", "First number", true);
        OptionData num2 = new OptionData(OptionType.INTEGER, "num2", "Second number", true);
        commands.add(Commands.slash("subtract", "Subtract two numbers").addOptions(num1, num2));

        event.getGuild().updateCommands().addCommands(commands).queue();

    }

    */

    public CommandManager(Bot bot) {
        createCommandMap(
                new PingCommand(bot),
                new SearchCommand(bot),
                new EmojifyCommand(bot),
                new LinkCommand(bot),
                new EmojiInfoCommand(bot)
        );
    }

    private void createCommandMap(Command ...cmdList) {
        for(Command cmd : cmdList) {
            commandsMap.put(cmd.name, cmd);
            commands.add(cmd);
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
        Command cmd = commandsMap.get(event.getName());
        if (cmd != null) {
            // Check for bot permissions
            Role botRole = Objects.requireNonNull(event.getGuild()).getBotRole();
            if (botRole != null && cmd.botPermission != null) {
                if (!botRole.hasPermission(cmd.botPermission) && !botRole.hasPermission(Permission.ADMINISTRATOR)) {
                    String text = "I need **" + cmd.botPermission.getName() + "** permission to execute that command.";
                    event.reply(text).setEphemeral(true).queue();
                    return;
                }
            }
            // Run command
            cmd.run(event);
        }
    }

    /**
     * For registering GUILD slash commands (for testing purposes)
     */
    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        // Register slash commands
        registerCommands(event);
    }

    /**
     * For registering GUILD slash commands (for testing purposes)
     */
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        // Register slash commands
        registerCommands(event);
    }

    /**
     * Global slash commands (can take up to an hour to update! - for production)
     */

    /*
    @Override
    public void onReady(ReadyEvent event) {
        event.getJDA().updateCommands().queue();
        //event.getJDA().updateCommands().addCommands(unpackCommandData()).queue(succ -> {}, fail -> {});
    }

     */


    private void registerCommands(GenericGuildEvent event) {
        event.getGuild().updateCommands().addCommands(unpackCommandData()).queue(succ -> {}, fail -> {});
    }
}
