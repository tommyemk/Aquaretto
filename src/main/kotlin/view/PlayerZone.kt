package view

import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.container.Area
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.GameComponentView
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import java.awt.Color

/**
 * A class that arranges game elements that are assigned to a [entity.Player], for instance, his/her depot.
 * The created personal zone also recognizes if a player has taken a [Transporter].
 * The transporter's load will be unloaded at a collection point.
 *
 * @property posX x coordinate in the scene, e.g., a [GameScene]
 * @property posY y coordinate
 * @property playerName the name of the owner of the zone. It will be displayed at the bottom.
 * //@property targetSpot a container for [TokenView]s where the load can be added to //will probably be deleted
 * @property index Int to facilitate the communication between GUI and service, used by the [GameScene]
 */

class PlayerZone(
    posX : Int = 0, posY : Int = 0,
    var playerName : String = "",
    var targetSpot : LinearLayout<GameComponentView> = LinearLayout(),
    var index : Int,
    val scene: GameScene
    ) : Pane<ComponentView>(posX, posY, width=342, height=420) {

        private val font = Font(color = DARK, size=16, family = FAMILY)

    private val progressBars = listOf(
        BasinProgress(posX = 60, posY = 150),//.apply { isVisible = false },
        BasinProgress(posX = 60, posY = 190),//.apply { isVisible = false },
        BasinProgress(posX = 60, posY = 230),//.apply { isVisible = false },
        BasinProgress(posX = 60, posY = 270),//.apply { isVisible = false },
        BasinProgress(posX = 60, posY = 310),//.apply { isVisible = false }
    )
    private val labels = listOf(
        Label(posX = 60, posY = 125, width = 220, font = font, text = "Looking forward to animals"),
        Label(posX = 60, posY = 165, width = 140, font = font),//.apply { isVisible = false },
        Label(posX = 60, posY = 205, width = 140, font = font),//.apply { isVisible = false },
        Label(posX = 60, posY = 245, width = 140, font = font),//.apply { isVisible = false },
        Label(posX = 60, posY = 285, width = 140, font = font),//apply { isVisible = false },
        Label(posX = 25, posY = 340, width = 140, font = font, text = "Coworkers: 0"),
        Label(posX = 175, posY =340, width = 100, font = font, alignment = Alignment.CENTER_LEFT, text = "Coins: 1")
    )
    val player = Label(
        posX = 10, posY = 110,
        width = 321, height = 310,
        text = playerName,
        font = Font( size = 24, color=Color.WHITE, fontWeight = Font.FontWeight.BOLD),
        alignment = Alignment.BOTTOM_CENTER
    )

    val highlight = Label(
        posX = 10, posY = 110,
        width = 321, height = 310,
        visual = ColorVisual(ACCENT)
    ).apply {
        isVisible = false
    }

    val depot = Area<TokenView>(
        posX = 121,
        width=100, height=100,
        visual = ImageVisual("depot.png")
    )
    val depotRequest = Button(
        posX = 121,
        width=100, height=100,
        visual = Visual.EMPTY
    ).apply{
        //Specify Drag-and-Drop functionality
        //Only accept animal tiles
        dropAcceptor = { dragEvent -> dragEvent.draggedComponent is TokenView
                && dragEvent.draggedComponent !is CoworkerToken
                && (scene.viewState == ViewState.InventoryActionB)
        }
        onDragDropped = { dragEvent ->
            depot.add((dragEvent.draggedComponent as TokenView).apply {
                reposition(5,5)
                isDraggable = false
            })
            println("new animal in depot $index: depot.components")
            if (depot.isNotEmpty()) {
                scene.notify(depot, depot.components[depot.components.size-1])
            }
        }
        //Request for adding, discarding or purchasing an animal
        onMouseClicked = {
            if (depot.isNotEmpty() && scene.viewState == ViewState.StartTurn) {
               scene.notify(depot)
            }
        }
        isDisabledProperty.addListener { _, newValue ->
            visual = if(newValue || scene.viewState != ViewState.StartTurn) {
                Visual.EMPTY
            } else {
                SQUARE_HIGHLIGHT
            }
        }
    }
    val transporterZone = Label(
        posX = 20, posY = 120,
        width = 301, height = 270,
        visual = ColorVisual(LIGHT)
    ).apply{
        onDragGestureEntered = {
            if (it.draggedComponent is Transporter) {
                scene.notify((it.draggedComponent as Transporter))
            }
            isDisabled = true
        }
    }

    init {

        isVisible = true

        addAll(
            highlight,
            player,
            transporterZone,
            depot, depotRequest,
            progressBars[0], progressBars[1], progressBars[2], progressBars[3], progressBars[4],
            labels[0], labels[1], labels[2], labels[3], labels[4], labels[5], labels[6]
        )

    }

    /**
     * A method that will update the player statistics (basin progress, number of coworkers, and number of coins).
     *
     * @param emphasize or not this player zone by means of a highlight, default is true
     * @param groups the number of [entity.Animal]s of each type in the [entity.Player.waterPark]
     * @param numCoworkers the number of Coworkers the player has assembled
     * @param numCoins the number of coins of the player
     */

    fun update(emphasize : Boolean = true, groups : List<Pair<*,Int>>, numCoworkers : Int, numCoins : Int){
        highlight.isVisible = emphasize
        println("update method")
        for (i in groups.indices) {
            labels[i].apply {
                text = groups[i].first.toString()
                isVisible = true
            }
            progressBars[i].apply{
                progress(groups[i].second)
                isVisible = true
            }
        }
        labels[5].text = "Coworkers: $numCoworkers"
        labels[6].text = "Coins: $numCoins"
    }

    /**
     * A method to only update the number of coins (after a player has taken a transporter with coins)
     *
     * @param numCoins the number of coins by which the current number will be increased
     */
    fun update(numCoins : Int) {
        labels[6].text =
            "Coins: $numCoins"
    }

    /**
     * A method to remove the top-most animal from the depot.
     */
    fun pop(): TokenView? {
        println("pop: depot components: ${depot.components}")
        if(depot.components.isNotEmpty()) {
            println("depot is not empty")
            val animalToken = depot.components[depot.components.size - 1]
            depot.remove(animalToken)
            return animalToken
        }
        return null
    }

    /**
     * A method to open or close the transporter zone for incoming (taken) transporters.
     *
     * @param e Boolean expression. If true, the transporter zone will accept a transporter taken by the player
     */

    fun open(e : Boolean = true) {
        transporterZone.isDisabled = !e
    }
}