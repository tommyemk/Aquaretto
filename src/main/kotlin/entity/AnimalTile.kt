package entity

import kotlinx.serialization.Serializable

/**
 * A class to represent tiles with animals opposed to [CoinTile]s.
 * There are several different animal types.
 * Additionally, an animal may have special properties, e.g., being 'trainable'.
 * These properties are optional, yet, they are meant to be mutually exclusive.
 *
 * @property id uniquely identifies each tile in the game
 * @property animalType one of eight possibilities of the enum [Animal], e.g. SEA_LION or HIPPO
 * @property isOffspring marks whether the animal is an offspring tile, meaning it is not part of the draw pile
 * @property isMale marks whether the animal is male, meaning it is able to have offspring
 * @property isFemale marks whether the animal is female. Note how 'not isMale' does not imply 'isFemale'
 * @property isTrainable marks whether the animal could be trained by a [Coworker], resulting in extra points
 * @property hasFish marks whether the animal could be fed by a [Coworker], resulting in extra points
 * @property hasOffspring ensures that an animal cannot have offspring twice
 */
@Serializable
data class AnimalTile(
    override val id: Int,
    var isOffspring: Boolean = false,
    var isMale: Boolean = false,
    var isFemale: Boolean = false,
    var isTrainable: Boolean = false,
    var hasFish: Boolean = false,
    var animalType: Animal
) : Tile() {

    //an animal may only have offspring once
    var hasOffspring: Boolean = false

    /**
     * This method is cloning this tile with all
     * the attributes is object currently holds.
     *
     * @return the cloned tile
     */
    override fun clone(): Tile {
        val newAnimalTile = AnimalTile(
            id = this.id,
            isOffspring = this.isOffspring,
            isMale = this.isMale,
            isFemale = this.isFemale,
            isTrainable = this.isTrainable,
            hasFish = this.hasFish,
            animalType
        )
        return newAnimalTile
    }
}