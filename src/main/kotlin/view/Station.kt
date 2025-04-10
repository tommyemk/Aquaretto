package view

import tools.aqua.bgw.components.RootComponent
import tools.aqua.bgw.components.container.Area
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.CompoundVisual
import tools.aqua.bgw.visual.TextVisual

/**
 * A class that represents the checkout booth, the feeding station, and the manager position
 * in a player's [entity.Waterpark].
 * Such a station is a drop acceptor for [CoworkerToken]s.
 *
 * @property x the pseudo x coordinate within the waterpark as stations for coworkers are identified by constant coords
 */

class Station(val x : Int, val scene: GameScene, profession: String) :
    Area<TokenView>(width=100, height=100) {

    init {
        visual = CompoundVisual(ColorVisual(LIGHT).apply { opacity = 0.6 }, TextVisual(profession))

        //Specify Drag-and-Drop functionality
        dropAcceptor = { dragEvent -> dragEvent.draggedComponent is CoworkerToken
                && components.size < 2
        }
        onDragDropped = { dragEvent ->
            val token = (dragEvent.draggedComponent as CoworkerToken)
            println("Waterpark: $token")
            add(token.apply { reposition(5,5)})
            scene.notify(this, token)
        }
    }
}