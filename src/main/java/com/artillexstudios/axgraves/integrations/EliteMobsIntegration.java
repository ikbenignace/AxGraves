package com.artillexstudios.axgraves.integrations;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import static com.artillexstudios.axgraves.AxGraves.CONFIG;

/**
 * Simple integration with EliteMobs plugin to detect when players are in dungeons
 * and prevent grave spawning to allow EliteMobs' revival mechanics to work properly.
 */
public class EliteMobsIntegration {

    private static boolean isEliteMobsAvailable = false;

    /**
     * Initialize the EliteMobs integration - automatically detects if EliteMobs is installed
     */
    public static void initialize() {
        // Only check if integration is enabled in config
        if (!CONFIG.getBoolean("integrations.elitemobs.enabled", true)) {
            return;
        }

        // Simple check if EliteMobs plugin is installed and enabled
        Plugin eliteMobsPlugin = Bukkit.getPluginManager().getPlugin("EliteMobs");
        isEliteMobsAvailable = (eliteMobsPlugin != null && eliteMobsPlugin.isEnabled());
    }

    /**
     * Check if a player is in an EliteMobs dungeon where grave spawning should be disabled.
     * Uses simple and reliable detection methods.
     * 
     * @param player The player to check
     * @return true if the player is in an EliteMobs dungeon
     */
    public static boolean isInDungeon(Player player) {
        // If EliteMobs isn't available, no need to check
        if (!isEliteMobsAvailable) {
            return false;
        }

        // Check for EliteMobs dungeon metadata (most reliable method)
        if (player.hasMetadata("elitemobs_dungeon") || 
            player.hasMetadata("elitemobs_in_dungeon")) {
            return true;
        }

        // Check if world name indicates it's an EliteMobs dungeon
        String worldName = player.getWorld().getName().toLowerCase();
        return worldName.startsWith("em_") || 
               worldName.startsWith("elitemobs_") ||
               worldName.contains("_dungeon") ||
               worldName.startsWith("dungeon_");
    }

    /**
     * Check if EliteMobs integration is available
     */
    public static boolean isEnabled() {
        return isEliteMobsAvailable;
    }
}