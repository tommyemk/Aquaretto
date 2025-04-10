package view

import entity.AnimalTile
import entity.Tile
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.visual.CompoundVisual
import tools.aqua.bgw.visual.ImageVisual

/**
 * Instances of [TokenManager] are useful to create [TokenView]s to a given [entity.Tile] or [entity.Coworker].
 * In particular, the TokenManager will retrieve the right [ImageVisual] from a resource file.
 */

class TokenManager(val gameScene: GameScene) {

    private val images = mutableListOf<ImageVisual>()

    init {
        //In the resource .png, the animal types are arranged in columns
        val offsetX = listOf(1124,640,160,800,318,480,1280,962) //The order matches the ordinals of the Animal enum
        val offsetY = listOf(0,160,320,480) //There are four versions for each animal type

        for (x in offsetX) {
            for(y in offsetY) {
                images.add(ImageVisual("Tiles.png", 160,160, x, y))
            }
        }
        //Coworker Image Visual
        images.add(ImageVisual("Tiles.png", 160,160, offsetX = 0))
        //Coin Image Visual
        images.add(ImageVisual("Tiles.png", 160,160, offsetX=0, offsetY = 160))

    }

    /**
     * tokenfortile
     */
    fun tokenForTile(tile : Tile) : TokenView {

        var index = 33
        if (tile is AnimalTile) {
            index = tile.animalType.ordinal*4
            if (tile.isMale) {
                index += 1
            } else if (tile.isFemale) {
                index += 2
            } else if (tile.isTrainable || tile.hasFish) {
                index += 3
            }
        }
        return TokenView(
            width = 90, height = 90, visual = images[index]
        ).apply { isDraggable = true }
    }

    /**
     * tokenforcoworker
     */
    fun tokenForCoworker() : CoworkerToken {
        return CoworkerToken(visual = images[32], gameScene)
    }

    /**
     * tokenforextension
     */
    fun tokenForExtension(posX : Int, posY : Int, isSmall : Boolean = true) : ExtensionToken {
        return ExtensionToken(posX, posY, isSmall)
    }

}
