package view

import entity.AnimalTile
import entity.Tile
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.GameComponentView
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.uicomponents.Orientation
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.CompoundVisual
import tools.aqua.bgw.visual.ImageVisual

/**
 * This class extends an ordinary [LinearLayout] such that it can conveniently act as a view for [entity.Transporter].
 * The transporter can be loaded with game tiles as [TokenView]s (Action A).
 * It can also be 'taken' (dragged) by a player (Action B).
 *
 * @property posX x coordinate in the scene, e.g., the [GameScene]
 * @property posY y coordinate
 * @property cap the capacity of the transporter, default is 3
 * @property index Int to facilitate the communication between GUI and service, used by the [GameScene]
 */
class Transporter(posX : Int = 0, posY : Int = 0, var cap : Int = 3, val index : Int, val scene: GameScene) :
    LinearLayout<TokenView>(
        posX=posX, posY=posY, width=80, height=240, spacing=10, visual=ColorVisual(165, 116, 59)
    ) {
    private val defaultVisual = ImageVisual("Waterpark.png", 31,91, 103,102)
    init {
        visual = defaultVisual
        orientation = Orientation.VERTICAL
        alignment = Alignment.CENTER

        //Specify Drag-and-Drop functionality
        //Only accept tiles
        dropAcceptor = { dragEvent ->
            dragEvent.draggedComponent is TokenView &&
                    components.size < cap &&
                    scene.viewState == ViewState.ActionA
        }
        onDragDropped = { dragEvent ->
            add(
                (dragEvent.draggedComponent as TokenView).apply {
                    scale(0.8)
                    isDraggable = false
                    if (visual is CompoundVisual) {
                        visual = (visual as CompoundVisual).children[0]
                    }
                }
            )
            //The transporter itself is draggable once being loaded
            isDraggable = true
            scene.notify(this)
        }

        isDraggableProperty.addListener { _, newValue ->
            visual = if (newValue && !isDisabled) {
                CompoundVisual(
                    defaultVisual,
                    TRANS_HIGHLIGHT
                )
            } else {
                defaultVisual
            }
        }
    }

    /**
     * CURRENTLY NOT IN USE
     * A method to unload the transporter.
     * Unloading the transporter will clear this layout, but animal tiles will be carried to a target spot.
     *
     * @param targetSpot the [LinearLayout] certain tiles from the transporter will be transferred to
     * @param bimap a [BidirectionalMap] to determine whether the loaded TokenViews correspond to [AnimalTile]s
     * @return Pair of Integers indicating the number of loaded animals (first) and coins (second)
     */

    fun unload(targetSpot : LinearLayout<GameComponentView>, bimap : BidirectionalMap<Tile,TokenView>) : Pair<Int,Int> {
        val n = components.size
        var m = 0
        val items = components
        clear()
        items.filter { bimap.backward(it) is AnimalTile }.forEach {
            it.apply {
                scale(1.0)
                isDraggable = true
            }
            m++
            targetSpot.add(it)
            println("Transporter: $targetSpot")
        }
        opacity = 0.2
        isDraggable = false
        return Pair(m, n - m)
    }

    /**
     * A method to unload the transporter. This method is used by the [GameScene] after being notified that this
     * transporter has been taken.
     */
    fun unload() {
        clear()
        opacity = 0.2
        isDisabled = true
        visual = defaultVisual
    }

    /**
     * A method to release a transporter after one round is over and all transporters can be loaded again.
     */
    fun release() {
        clear()
        opacity = 1.0
        isDisabled = false
    }

    fun enable() {
        isDraggable = components.isNotEmpty()
    }

    fun disable() {
        isDraggable = false
    }

}