package entity

import kotlinx.serialization.Serializable

/**
 * An interface that marks entities as placeable within the game context.
 * This interface is implemented by any game entity that can be positioned or placed within the game board or park.
 */
@Serializable
sealed interface Placeable {

    /**
     * abstract method for cloning
     */
    fun clone(): Placeable
}