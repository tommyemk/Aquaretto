package view

import tools.aqua.bgw.components.container.Area
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.visual.CompoundVisual

/**
 * class for extensionTokens ok
 *
 * @property posX position x
 * @property posY position y
 * @property isSmall some boolean
 */
class ExtensionToken(posX : Int = 0, posY : Int = 0, val isSmall : Boolean = true):
   Area<TokenView>(posX = posX, posY = posY, width=180, height=180) {

    private val fields = listOf(
        TokenView(posX=0, posY=0, width=90,height=90,visual=CompoundVisual(GRASS, SQUARE_HIGHLIGHT)).apply {
            onMousePressed = { this@ExtensionToken.isDraggable=true }
        },
        TokenView(posX=0, posY=90, width=90,height=90,visual= GRASS).apply {
            onMousePressed = { this@ExtensionToken.isDraggable=!isSmall }
        },
        TokenView(posX=90, posY=90, width=90,height=90,visual= GRASS).apply {
            onMousePressed = { this@ExtensionToken.isDraggable=!isSmall }
        }
    )

    private val fourthField = TokenView(posX=90, posY=0, width=90,height=90,visual= GRASS)

    init{
        addAll( fields )
        if(!isSmall) {
            add(fourthField)
        }
        onMouseClicked = {
            rotate(90)
        }

        isDisabledProperty.addListener { _, newVale ->
            fields[0].visual = if (!newVale) CompoundVisual(GRASS, SQUARE_HIGHLIGHT)
            else GRASS
        }
    }

}