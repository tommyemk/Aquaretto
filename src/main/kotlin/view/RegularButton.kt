package view

import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.style.BackgroundRadius
import tools.aqua.bgw.style.BorderColor
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.style.BorderWidth
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

/**
 * This class extends ordinary [Button]s in order to match the design choices.
 * While the height is fixed, the width is flexible. Suggested are 200 and 150.
 */
class RegularButton(posX : Int, posY : Int, width : Int, text : String = "", fontSize: Int = 24) :
Button(
    posX=posX, posY=posY, width=width, height=50, text=text,
    font = Font(size=fontSize, color=DARK, family = FAMILY, fontStyle = Font.FontStyle.ITALIC),
    visual = ColorVisual(LIGHT).apply {
        borderRadius = BorderRadius(5)
        backgroundRadius = BackgroundRadius(100)
    }
)