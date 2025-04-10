package entity

import kotlinx.serialization.Serializable

/**
 * Represents a coworker in the Aquaretto game, which is a specific type of tile that can be placed in the water park.
 *
 * @property coworkerTask Defines the specific task or role this coworker performs in the game,
 * impacting gameplay and strategy.
 */

@Serializable
class Coworker(var coworkerTask : CoworkerTask) : Placeable{

    /**
     * Creates new coworker and with the CoworkerTask of this object.
     *
     * @return the new coworker
     */
    override fun clone(): Placeable {
        val newCoworker = Coworker( coworkerTask = this.coworkerTask)
        return newCoworker
    }
}
