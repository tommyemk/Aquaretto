package view

import entity.Player
import service.RootService
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.style.BackgroundRadius
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

/**
 * A [MenuScene] that will be displayed once a game has come to an end. It displays each player's
 * name and their points (in descending order to determine a winner). Additionally, two Buttons are
 * displayed: One just exits the game while the other starts a new game by switching to the [StartMenuScene].
 */
class LeaderboardScene : MenuScene(680, 1080), Refreshable {

    private val finalPointLabel = Label(
        posX = 220, posY = 90,
        width = 240,
        text = "Final Points",
        font = Font(size=36, color=DARK, family="Calibri", fontWeight = Font.FontWeight.BOLD )
    )

    private val gridPane = GridPane<Label>(
        posX = 200, posY = 200,
        rows = 5, columns = 2,
        layoutFromCenter = false,
        spacing = 20
    )

    private val playerNameLabelList = listOf(
        Label(
            text = "",
            font = Font(size=36, color= LIGHT, family="Calibri", fontWeight = Font.FontWeight.BOLD)
            ).apply { alignment = Alignment.CENTER_LEFT },
        Label(
            text = "",
            font = Font(size=36, color= LIGHT, family="Calibri", fontWeight = Font.FontWeight.BOLD)
        ).apply { alignment = Alignment.CENTER_LEFT },
        Label(
            text = "",
            font = Font(size=36, color= LIGHT, family="Calibri", fontWeight = Font.FontWeight.BOLD)
        ).apply { alignment = Alignment.CENTER_LEFT },
        Label(
            text = "",
            font = Font(size=36, color= LIGHT, family="Calibri", fontWeight = Font.FontWeight.BOLD)
        ).apply { alignment = Alignment.CENTER_LEFT },
        Label(
            text = "",
            font = Font(size=36, color= LIGHT, family="Calibri", fontWeight = Font.FontWeight.BOLD)
        ).apply { alignment = Alignment.CENTER_LEFT }
    )

    private val playerPointLabelList = listOf(
        Label(
            text = "",
            font = Font(size=36, color= LIGHT, family="Calibri", fontWeight = Font.FontWeight.BOLD)
        ).apply { alignment = Alignment.CENTER_RIGHT },
        Label(
            text = "",
            font = Font(size=36, color= LIGHT, family="Calibri", fontWeight = Font.FontWeight.BOLD)
        ).apply { alignment = Alignment.CENTER_RIGHT },
        Label(
            text = "",
            font = Font(size=36, color= LIGHT, family="Calibri", fontWeight = Font.FontWeight.BOLD)
        ).apply { alignment = Alignment.CENTER_RIGHT },
        Label(
            text = "",
            font = Font(size=36, color= LIGHT, family="Calibri", fontWeight = Font.FontWeight.BOLD)
        ).apply { alignment = Alignment.CENTER_RIGHT },
        Label(
            text = "",
            font = Font(size=36, color= LIGHT, family="Calibri", fontWeight = Font.FontWeight.BOLD)
        ).apply { alignment = Alignment.CENTER_RIGHT }
    )

    private val backGroundPane = Pane<Label>(
        posX = 150, posY = 185,
        width = 380, height = 20*3 + playerNameLabelList[0].height*2,
        visual = ColorVisual(SCENE)
    )

    val exitButton = Button(
        posX = 130, posY = 750,
        width = 180, height = 50,
        visual = ColorVisual(LIGHT).apply { opacity = 0.5 },
        text = "Exit",
        font = Font(size=24, color=DARK, family = "Calibri",
            fontStyle = Font.FontStyle.ITALIC, fontWeight = Font.FontWeight.BOLD)
    )

    val newGameButton = Button(
        posX = 370, posY = 750,
        width = 180, height = 50,
        visual = ColorVisual(LIGHT).apply { opacity = 0.5 },
        text = "New Game",
        font = Font(size=24, color=ACCENT, family = "Calibri",
            fontStyle = Font.FontStyle.ITALIC, fontWeight = Font.FontWeight.BOLD)
    )


    init {
        background = ColorVisual(BLUE)
        opacity = 1.0

        for(i in playerNameLabelList.indices){
            gridPane[0, i] = playerNameLabelList[i]
            gridPane[1, i] = playerPointLabelList[i]
        }

        addComponents(
            finalPointLabel, exitButton, newGameButton, backGroundPane, gridPane
        )
    }

    /**
     * Function to set the names, points and background for the game that just ended. Should be called by
     * [refreshAfterEndGame].
     *
     * @param playerPointsList [List] from the service-layer that contains Pairs of [Player]-objects
     * with their respective points
     */
    fun setLabelsAfterGame(playerPointsList: List<Pair<Player, Int>>){

        require(playerPointsList.size in 2..5){"Number of player is illegal."}

        val sortedList = playerPointsList.sortedByDescending { it.second }
        playerPointLabelList.forEach{ it.isVisible = false }
        playerNameLabelList.forEach{ it.isVisible = false }
        for(i in sortedList.indices){
            playerNameLabelList[i].text = sortedList[i].first.name
            playerPointLabelList[i].text = "${sortedList[i].second}"

            playerNameLabelList[i].isVisible = true
            playerPointLabelList[i].isVisible = true
        }

        backGroundPane.height = 20*(sortedList.size + 1) + playerNameLabelList[0].height*(sortedList.size)
    }
}