package view

import tools.aqua.bgw.components.container.GameComponentContainer
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.visual.Visual

/**
 * A class that provides a token for the placeable [entity.Coworker].
 * We want to distinguish [entity.AnimalTile]s and Coworkers by their type
 * as a [Station] in the [WaterparkView] will only accept Coworkers.
 *
 * @property visual the image that is visible for the user.
 *  A [TokenManager] can be used to instantiate this Coworker Token with a suitable visual
 */

class CoworkerToken(visual : Visual, gameScene: GameScene) :
    TokenView(width=90, height=90, visual = visual) {

        /*currently not in use

    fun notify(sender : GameComponentContainer<TokenView>) {
        println("added")
    }

         */

    init {
        isDraggable = true

        onMouseClicked = {
            gameScene.coworkerClicked(this)
        }
    }
}