package view

import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color

/**
 * Design choices such as colors and the [WaterparkArea] layout are defined here.
 */

//Color Theme
val NEUTRAL = Color(149, 144, 168) //'Cool Gray'
val DARK = Color(14, 20, 40) //'Oxford Blue'
val LIGHT = Color(240, 247, 244) //'Mint Cream'
val SCENE = Color(24, 64, 119) //'Yale Blue'
val BLUE = Color(141, 169, 196) //'Powder Blue'
val ACCENT = Color(210, 0, 136) //'Red Violet'

//Waterpark 'grass'
val GRASS = ImageVisual("Waterpark.png", width=100, height=100, offsetX = 100)
//'Terrain' where one can expand the waterpark
val TERRAIN = ImageVisual("Waterpark.png", width = 100, height=100)
//Tile back side
val WATER = ImageVisual("Waterpark.png", width=100, height=100, offsetY = 100)
val SQUARE_HIGHLIGHT = ImageVisual("square_highlight.png")
val TRANS_HIGHLIGHT = ImageVisual("transporter_highlight.png")
val TILE = ImageVisual("tile-back.jpg")

//As for font, we use family "Calibri". This is already set in RegularButton.
//You have to set it yourself if you want to create a Button with font color ACCENT (RegularButton is DARK)
//or create an arbitrary label. You can make use of this constant.
const val FAMILY = "Calibri"