package com.artillexstudios.axgraves.integrations;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import static com.artillexstudios.axgraves.AxGraves.CONFIG;

/**
 * Integration with EliteMobs plugin to detect dungeon gameplay
 * and prevent grave spawning during dungeon activities where
 * players enter spectator mode and can be revived.
 */
public class EliteMobsIntegration {

    private static Plugin eliteMobsPlugin = null;
    private static boolean integrationEnabled = false;
    private static boolean checked = false;

    /**
     * Initialize the EliteMobs integration
     */
    public static void initialize() {
        if (checked) return;
        checked = true;

        if (!CONFIG.getBoolean("integrations.elitemobs.enabled", true)) {
            return;
        }

        eliteMobsPlugin = Bukkit.getPluginManager().getPlugin("EliteMobs");
        if (eliteMobsPlugin != null && eliteMobsPlugin.isEnabled()) {
            integrationEnabled = true;
        }
    }

    /**
     * Check if a player is currently in an EliteMobs dungeon where
     * grave spawning should be disabled.
     * 
     * @param player The player to check
     * @return true if the player is in an EliteMobs dungeon and graves should be disabled
     */
    public static boolean isInDungeon(Player player) {
        if (!integrationEnabled) {
            return false;
        }

        try {
            // Check if EliteMobs API is available
            Class<?> dungeonObjectManagerClass = Class.forName("com.magmaguy.elitemobs.dungeons.DungeonObjectManager");
            Class<?> eliteMobsWorldManagerClass = Class.forName("com.magmaguy.elitemobs.worlds.EliteMobsWorldManager");
            
            // Use reflection to check if player is in a dungeon world
            Object worldManager = eliteMobsWorldManagerClass.getMethod("getInstance").invoke(null);
            boolean isEliteMobsWorld = (boolean) eliteMobsWorldManagerClass
                .getMethod("isEliteMobsWorld", org.bukkit.World.class)
                .invoke(worldManager, player.getWorld());
            
            if (isEliteMobsWorld) {
                // If in an EliteMobs world, check if it's a dungeon
                Object dungeonManager = dungeonObjectManagerClass.getMethod("getInstance").invoke(null);
                Object dungeon = dungeonObjectManagerClass
                    .getMethod("getDungeonFromWorld", org.bukkit.World.class)
                    .invoke(dungeonManager, player.getWorld());
                
                return dungeon != null;
            }
            
            return false;
        } catch (Exception e) {
            // If any reflection fails, fall back to checking player metadata
            // EliteMobs often uses metadata to track dungeon state
            return player.hasMetadata("elitemobs_dungeon") || 
                   player.hasMetadata("elitemobs_in_dungeon") ||
                   checkWorldNameForDungeon(player);
        }
    }

    /**
     * Fallback method to check if the world name suggests it's a dungeon
     */
    private static boolean checkWorldNameForDungeon(Player player) {
        String worldName = player.getWorld().getName().toLowerCase();
        // EliteMobs typically prefixes dungeon worlds
        return worldName.contains("em_") || 
               worldName.contains("elitemobs_") ||
               worldName.contains("dungeon_") ||
               worldName.contains("_dungeon");
    }

    /**
     * Check if EliteMobs integration is enabled and available
     */
    public static boolean isEnabled() {
        return integrationEnabled;
    }
}