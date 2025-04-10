package view

import tools.aqua.bgw.components.RootComponent
import tools.aqua.bgw.components.container.Area
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.event.DragEvent

/**
 * A class that represents a field in the immediate surroundings of a player's [entity.Waterpark].
 * The player can expand his/her waterpark here.
 * Therefore,such a field is a drop acceptor for [entity.WaterparkExtension]s views.
 *
 * @property x the x coordinate within the grid of the [WaterparkView]
 * @property y the y coordinate within the grid of the [WaterparkView]
 */

class ExtensionArea(val x : Int, val y : Int, val scene: GameScene) :
    Area<TokenView>(width=100, height=100) {

    init {
        visual = TERRAIN

        //Only accept Waterpark Extensions, i.e. ExtensionToken
        dropAcceptor = { dragEvent -> requestExtension(dragEvent) }
        onDragDropped = { dragEvent ->
            opacity = 1.0
            scene.placeExtension(this, dragEvent.draggedComponent as ExtensionToken)
        }
        onDragGestureEntered = {
            if(it.draggedComponent is ExtensionToken) {
                opacity = 0.5
            }
        }
        onDragGestureExited = {
            opacity = 1.0
        }

    }

    private fun requestExtension(dragEvent: DragEvent) : Boolean {
        if (dragEvent.draggedComponent is ExtensionToken) {
            return scene.request(
                this, extension=dragEvent.draggedComponent as ExtensionToken, token=null)
        }
        return false
    }
}