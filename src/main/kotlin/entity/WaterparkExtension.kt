package entity

import kotlinx.serialization.Serializable

/**
 * This class represents the WaterparkExtension of the player
 * which can be placed on the board in order to maximise the current player waterpark size.
 *
 * @param rotation the rotation of the WaterparkExtension
 *      0: Turned to the top
 *      1: Turned to the right
 *      2: Turned to the bottom
 *      3: Turned to the left
 * @param x the x coordinate of the waterpark
 * @param y the x coordinate of the waterpark
 * @param isSmall whether the WaterparkExtension is small or big
 */

@Serializable
data class WaterparkExtension(var rotation: Int = 0, var x: Int = -1, var y: Int = -1, val isSmall: Boolean = false)  {

    /**
     * clones extensions. wow
     */
    fun clone(): WaterparkExtension{
        val newWaterparkExtension = WaterparkExtension(
            rotation = this.rotation,
            x = this.x,
            y = this.y,
            isSmall = this.isSmall
        )
        return newWaterparkExtension
    }
}