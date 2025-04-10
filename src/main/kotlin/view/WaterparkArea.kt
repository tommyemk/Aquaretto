package view

import tools.aqua.bgw.components.container.Area
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.event.DragEvent

/**
 * A class that represents a single field of a player's [entity.Waterpark].
 * Such a field is a drop acceptor for [entity.Placeable] tiles.
 * However, it will not accept tiles that just have been drawn (scale=1.3).
 *
 * @property x the x coordinate within the grid of the [WaterparkView]
 * @property y the y coordinate within the grid of the [WaterparkView]
 */

class WaterparkArea(val x : Int, val y : Int, val scene: GameScene) :
    Area<TokenView>(width=100, height=100) {

    init {
        visual = GRASS

        //Specify Drag-and-Drop functionality
        dropAcceptor = { dragEvent ->
            requestPlacement(dragEvent) &&
                    (scene.viewState == ViewState.InventoryActionB ||
                            scene.viewState == ViewState.InventoryBuy ||
                            scene.viewState == ViewState.InventorySpecial ||
                            scene.viewState == ViewState.InventoryMove)
        }
        onDragDropped = { dragEvent ->
            val token = (dragEvent.draggedComponent as TokenView)
            add(token.apply { reposition(5,5)})
            token.isDraggable = false
            scene.notify(this, token)
        }
    }

    private fun requestPlacement(dragEvent: DragEvent) : Boolean {
        if (dragEvent.draggedComponent is TokenView) {
            return (
                    components.isEmpty()
                    && dragEvent.draggedComponent.scale == 1.0
                    && (dragEvent.draggedComponent is CoworkerToken ||
                        scene.request(this, token = dragEvent.draggedComponent as TokenView))
                    )
        }
        return false
    }

}