package com.emojibot.utils.command;

import com.emojibot.Bot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class EmojiCommand {
    public Bot bot;
    public String name;
    public String description;
    public List<OptionData> args;
    public List<SubcommandData> subCommands;
    public Permission permission;
    public Permission botPermission;
    public Integer cooldownDuration;

    public Map<DiscordLocale, String> localizedNames;
    public Map<DiscordLocale, String> localizedDescriptions;
    

    public EmojiCommand(Bot bot) {
        this.bot = bot;
        this.args = new ArrayList<>();
        this.subCommands = new ArrayList<>();
        this.localizedNames = new HashMap<>();
        this.localizedDescriptions = new HashMap<>();
    }

    public abstract void run(SlashCommandInteractionEvent event);

}


/*
package com.emojibot.commands.utils;

import com.emojibot.Bot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.ArrayList;
import java.util.List;

public abstract class EmojiCommand {
    public Bot bot;
    public String name;
    public String description;
    public List<OptionData> args;
    public List<SubcommandData> subCommands;
    public Permission permission; // Permission user needs to execute this command
    public Permission botPermission; // Permission bot needs to execute this command
    public Integer cooldownDuration;

    public EmojiCommand(Bot bot) {
        this.bot = bot;
        this.args = new ArrayList<>();
        this.subCommands = new ArrayList<>();
    }

    public abstract void run(SlashCommandInteractionEvent event);


}
*/