package com.emojibot.events;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//TODO timestamps for cooldown messages

public class CooldownManager {
    private final Map<String, Map<String, Instant>> cooldowns = new HashMap<>();
    private final Set<String> warnedUsers = new HashSet<>();

    public boolean isOnCooldown(String userId, String commandName, long cooldownDuration) {
        Instant now = Instant.now();
        Map<String, Instant> userCooldowns = cooldowns.computeIfAbsent(userId, k -> new HashMap<>());
        Instant lastUsage = userCooldowns.get(commandName);

        if (lastUsage == null || now.isAfter(lastUsage.plusSeconds(cooldownDuration))) {
            // Remove from warned users
            warnedUsers.remove(userId + ":" + commandName); 
            return false;
        }

        return true;
    }

    /**
     * Put the user on cooldown after first usage
     * @param userId
     * @param commandName
     */
    public void setCooldown(String userId, String commandName) {
        Map<String, Instant> userCooldowns = cooldowns.computeIfAbsent(userId, k -> new HashMap<>());
        userCooldowns.put(commandName, Instant.now());
    }

    /**
     * Remaining time for cooldown
     * @param userId
     * @param commandName
     * @param cooldownDuration
     * @return
     */
    public long getRemainingCooldown(String userId, String commandName, long cooldownDuration) {
        Instant now = Instant.now();
        Map<String, Instant> userCooldowns = cooldowns.get(userId);
        if (userCooldowns == null) return 0;

        Instant lastUsage = userCooldowns.get(commandName);

        if (lastUsage == null) {
            return 0;
        }

        long remainingCooldown = cooldownDuration - (now.getEpochSecond() - lastUsage.getEpochSecond());
        return remainingCooldown > 0 ? remainingCooldown : 0;
    }

    /**
     * Checks if user is already warned
     * @param userId
     * @param commandName
     * @return
     */
    public boolean hasBeenWarned(String userId, String commandName) {
        return warnedUsers.contains(userId + ":" + commandName);
    }

    /**
     * Warns user for their first cooldown attempt
     * @param userId
     * @param commandName
     */
    public void warnUser(String userId, String commandName) {
        warnedUsers.add(userId + ":" + commandName);
    }
}
