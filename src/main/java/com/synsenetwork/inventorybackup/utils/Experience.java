package com.synsenetwork.inventorybackup.utils;

import org.bukkit.entity.Player;

/**
 * A utility class for calculating player experience in Minecraft.
 */
public final class Experience {

    /**
     * Gets the total experience of a player.
     *
     * @param player The player whose experience is to be calculated.
     * @return The total experience of the player.
     *
     * @see <a href="https://minecraft.fandom.com/wiki/Experience#Leveling_up">Experience#Leveling_up</a>
     */
    public static int getExp(Player player) {
        return getExpFromLevel(player.getLevel()) + Math.round(getExpToNext(player.getLevel()) * player.getExp());
    }

    /**
     * Calculates the experience required to reach a certain level.
     *
     * @param level The level for which to calculate the experience.
     * @return The experience required to reach the specified level.
     *
     * @see <a href="https://minecraft.fandom.com/wiki/Experience#Leveling_up">Experience#Leveling_up</a>
     */
    public static int getExpFromLevel(int level) {
        if (level > 30) {
            // Formula for levels 31 and above.
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
        if (level > 15) {
            // Formula for levels 16 to 30.
            return (int) (2.5 * level * level - 40.5 * level + 360);
        }
        // Formula for levels 0 to 15.
        return level * level + 6 * level;
    }

    /**
     * Calculates the player's level from their experience points.
     *
     * @param exp The total experience points of the player.
     * @return The player's level based on their experience.
     */
    public static double getLevelFromExp(long exp) {
        // Calculate the integer level from the experience.
        int level = getIntLevelFromExp(exp);

        // Calculate the progress to the next level.
        float remainder = exp - (float) getExpFromLevel(level);

        // Calculate the progress to the next level as a percentage.
        float progress = remainder / getExpToNext(level);

        // Return the level and progress as a double.
        return ((double) level) + progress;
    }

    /**
     * Calculates the integer player level from their experience points.
     *
     * @param exp The total experience points of the player.
     * @return The player's integer level based on their experience.
     */
    public static int getIntLevelFromExp(long exp) {
        if (exp > 1395) {
            // Formula for levels 32 and above.
            return (int) ((Math.sqrt(72 * exp - 54215D) + 325) / 18);
        }
        if (exp > 315) {
            // Formula for levels 17 to 31.
            return (int) (Math.sqrt(40 * exp - 7839D) / 10 + 8.1);
        }
        if (exp > 0) {
            // Formula for levels 0 to 16.
            return (int) (Math.sqrt(exp + 9D) - 3);
        }
        return 0;
    }

    /**
     * Gets the experience required to reach the next level from the current level.
     *
     * @param level The current player level.
     * @return The experience required to reach the next level.
     *
     * @see <a href="https://minecraft.fandom.com/wiki/Experience#Leveling_up">Experience#Leveling_up</a>
     */
    private static int getExpToNext(int level) {
        if (level >= 30) {
            // Formula for levels 31 and above.
            return level * 9 - 158;
        }
        if (level >= 15) {
            // Formula for levels 16 to 30.
            return level * 5 - 38;
        }
        //
        return level * 2 + 7;
    }

    /**
     * Changes the player's total experience points to the specified value.
     * This method sets both the player's level and experience points accordingly.
     *
     * @param player The player whose experience is to be changed.
     * @param exp The new total experience points for the player.
     */
    public static void changeExp(Player player, int exp) {
        // Prevent negative experience.
        if (exp < 0) {
            exp = 0;
        }

        double levelAndExp = getLevelFromExp(exp);
        int level = (int) levelAndExp;
        player.setLevel(level);
        player.setExp((float) (levelAndExp - level));
    }

    // Private constructor to prevent instantiation of the utility class.
    private Experience() {
    }
}
