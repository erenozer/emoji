package com.emojibot.commands.premium;

import com.emojibot.Bot;
import com.emojibot.BotConfig;
import com.emojibot.utils.Localization;
import com.emojibot.utils.button_listeners.PremiumManager;
import com.emojibot.utils.command.EmojiCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PackCommand extends EmojiCommand {

    public PackCommand(Bot bot) {
        super(bot);
        this.name = "pack";
        this.description = "Pack your server's emojis into a folder!";
        this.cooldownDuration = 5;

        this.localizedNames.put(DiscordLocale.TURKISH, "paketle");
        this.localizedDescriptions.put(DiscordLocale.TURKISH, "Sunucudaki emojilerin dosyalarını topluca almanızı sağlar");

        this.permission = Permission.MANAGE_GUILD_EXPRESSIONS;
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        Localization localization = Localization.getLocalization(event.getUser().getId());

        String guildId = event.getGuild().getId();
        Guild guild = event.getGuild();

        // Check if the server is premium
        if (!PremiumManager.getPremiumStatus(guildId)) {
            event.getHook().sendMessage(localization.getMsg("premium_command", "need_premium")).queue();
            return;
        }

        List<RichCustomEmoji> emojis = guild.getEmojis();

        event.getHook().sendMessage(
            String.format(localization.getMsg("pack_command", "starting_process"), BotConfig.infoEmoji())).queue();

        try {
            File zipFile = createZipFile(emojis);
            FileUpload fileUpload = FileUpload.fromData(zipFile, zipFile.getName());

            event.getHook().editOriginalAttachments(fileUpload).queue(
                success -> {
                    if (zipFile != null && zipFile.exists()) {
                        zipFile.delete(); // Delete the file after sending
                    }
                    event.getHook().editOriginal(String.format(localization.getMsg("pack_command", "success"), BotConfig.yesEmoji())).queue();
                },
                error -> {
                    if (zipFile != null && zipFile.exists()) {
                        zipFile.delete(); // Delete the file on error
                    }
                    error.printStackTrace();
                    event.getHook().editOriginal(String.format(localization.getMsg("pack_command", "error"), BotConfig.noEmoji())).queue();
                }
            );
        } catch (IOException e) {
            e.printStackTrace();
            event.getHook().editOriginal(String.format(localization.getMsg("pack_command", "error"), BotConfig.noEmoji())).queue();
        }
    }

    private File createZipFile(List<RichCustomEmoji> emojis) throws IOException {
        File zipFile = File.createTempFile("emojis", ".zip");

        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            Set<String> fileNames = new HashSet<>();

            for (RichCustomEmoji emoji : emojis) {
                String fileName = getFileName(emoji, fileNames);
                URL emojiUrl = new URL(emoji.getImageUrl());

                try (InputStream inputStream = emojiUrl.openStream()) {
                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zos.putNextEntry(zipEntry);
                    IOUtils.copy(inputStream, zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    System.out.println("Error downloading or adding emoji: " + emoji.getName() + " " + e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return zipFile;
    }

    private String getFileName(RichCustomEmoji emoji, Set<String> fileNames) {
        String name = emoji.getName();
        String format = emoji.getImageUrl().contains(".gif") ? "gif" :
                        emoji.getImageUrl().contains(".jpg") ? "jpg" : "png";
        String fileName = name + "." + format;

        int counter = 1;
        while (fileNames.contains(fileName)) {
            fileName = name + "_" + counter + "." + format;
            counter++;
        }

        fileNames.add(fileName);
        return fileName;
    }
}