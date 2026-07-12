package com.watch.omnitrix

/**
 * One selectable "DNA sample" on the dial.
 * drawableRes points at the transparent-background silhouette in res/drawable-nodpi.
 *
 * Slots 1-7 use the artwork you supplied.
 * Slots 8-10 are placeholder silhouettes (alien_08/09/10.png) — drop in your own
 * transparent PNGs with the same file names to replace them, no code changes needed.
 *
 * Rename `name` freely; it's just a label shown under the glow screen.
 */
data class Alien(
    val id: Int,
    val name: String,
    val drawableRes: Int
)

object AlienRoster {
    val ALL: List<Alien> = listOf(
        Alien(1, "SAMPLE 01", R.drawable.alien_01),
        Alien(2, "SAMPLE 02", R.drawable.alien_02),
        Alien(3, "SAMPLE 03", R.drawable.alien_03),
        Alien(4, "SAMPLE 04", R.drawable.alien_04),
        Alien(5, "SAMPLE 05", R.drawable.alien_05),
        Alien(6, "SAMPLE 06", R.drawable.alien_06),
        Alien(7, "SAMPLE 07", R.drawable.alien_07),
        Alien(8, "SAMPLE 08", R.drawable.alien_08),
        Alien(9, "SAMPLE 09", R.drawable.alien_09),
        Alien(10, "SAMPLE 10", R.drawable.alien_10)
    )
}
