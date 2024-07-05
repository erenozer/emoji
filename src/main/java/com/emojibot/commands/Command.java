package com.emojibot.commands;

import com.emojibot.Bot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.ArrayList;
import java.util.List;

public abstract class Command {
    public Bot bot;
    public String name;
    public String description;
    public List<OptionData> args;
    public List<SubcommandData> subCommands;
    public Permission permission; // Permission user needs to execute this command
    public Permission botPermission; // Permission bot needs to execute this command

    public Command(Bot bot) {
        this.bot = bot;
        this.args = new ArrayList<>();
        this.subCommands = new ArrayList<>();
    }

    public abstract void run(SlashCommandInteractionEvent event);


}
