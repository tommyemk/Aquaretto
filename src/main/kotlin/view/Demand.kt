package view

import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.util.Font

/**
 * The created [Pane] will display a demand that the user can either confirm or cancel by means of two buttons.
 *
 * @property posX the x coordinate, for example in a [GameScene]
 * @property posY the y coordinate
 */
class Demand(posX : Int, posY : Int) :
    Pane<ComponentView>(posX=posX, posY=posY, width=640, height=100, visual = ColorVisual(BLUE)) {

        var question = Label(
            posY=15, width=640, height=20,
            text = "?",
            font = Font(size=16, color= DARK, family = FAMILY)
        )

    val yes = RegularButton(
        posX = 95, posY= 40,
        width = 140,
        text = "Yes",
        fontSize = 14
    )

    val move = RegularButton(
        posX = 245, posY= 40,
        width = 140,
        text = "Move (-1 coin)",
        fontSize = 14
    )
    val stop = RegularButton(
        posX = 395, posY = 40,
        width = 140,
        text = "No",
        fontSize = 14
    )

        init {
            visual.apply { opacity = 0.6 }
            isVisible = false
            addAll( question, yes, stop, move )
        }
}