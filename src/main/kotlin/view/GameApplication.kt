package view

import entity.Player
import entity.PlayerType
import service.RootService
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.dialog.ExtensionFilter
import tools.aqua.bgw.dialog.FileDialog
import tools.aqua.bgw.dialog.FileDialogMode

/**
 * This class creates a window the game can take place in.
 * The game itself will consist of a [GameScene], and several [MenuScene]s including a [StartMenuScene].
 */
class GameApplication : BoardGameApplication(windowTitle = "Aquaretto"), Refreshable {

    private val rootService = RootService()

    private val gameScene = GameScene(rootService).apply {
        save.onMouseClicked = {
            val dialog = FileDialog(mode = FileDialogMode.SAVE_FILE, title = "Save Game",
                initialFileName = "newGame.json", //initialDirectory = File("./saves/"),
                extensionFilters = listOf(ExtensionFilter("JSON-Files", "*.json"))
            )
            showFileDialog(dialog).ifPresent {
                files -> files.forEach{
                    rootService.aquarettoGameService.saveGame(it)
                }
            }
        }
        exit.onMouseClicked = { showMenuScene(startMenuScene) }
    }

    private val playersScene = PlayersScene(rootService).apply {
        backButton.onMouseClicked = {
            hideMenuScene(0)
            showMenuScene(startMenuScene)
        }

        startButton.onMouseClicked = {
            val playerList = mutableListOf<Pair<String, PlayerType>>()
            for (i in 1 .. numVisible) {
                var nextOrder = i - 1
                if(!randomToggleButton.isSelected){
                    nextOrder = orderButtonList.indexOf(orderButtonList.find { it.text == "$i" })
                }
                val type = when(playerTypeComboBoxList[nextOrder].selectedItem){
                    "Human" -> PlayerType.LOCAL_HUMAN
                    "Random AI" -> PlayerType.LOCAL_RANDOM_AI
                    "Strong AI" -> PlayerType.LOCAL_AI
                    else -> throw IllegalArgumentException("PlayerType must be selected!")
                }
                playerList.add(Pair(playerNamesList[nextOrder].text, type))
            }
            rootService.aquarettoGameService.startGame(
                playerList,
                isOnline = isOnline,
                shuffle = randomToggleButton.isSelected,
                speed = 0
            )
            hideMenuScene(0)
        }
    }

    private val startMenuScene: StartMenuScene = StartMenuScene(rootService).apply {
        local.onMouseClicked = {
            playersScene.setupLocalGame()
            hideMenuScene(0)
            showMenuScene(playersScene)

        }

        online.onMouseClicked = {
            playersScene.setupOnlineGame()
            hideMenuScene(0)
            showMenuScene(playersScene)
        }

        exit.onMouseClicked = { exit() }
        resumeGame.onMouseClicked = {
            val dialog = FileDialog(FileDialogMode.OPEN_FILE,
                title = "Load Game", //initialDirectory = File("./saves/"),
                extensionFilters = listOf(ExtensionFilter("JSON-Files", "*.json")))
            showFileDialog(dialog).ifPresent { files ->
                files.forEach { file -> rootService.aquarettoGameService.loadGame(file) }
            }
        }
    }

    private val joinScene = JoinScene().apply {
        leaveButton.apply {
            onMouseClicked = {
                rootService.networkService.disconnect()
                showMenuScene(startMenuScene)
            }
        }
    }
    private val leaderboardScene = LeaderboardScene().apply {
        exitButton.onMouseClicked = { exit() }
        newGameButton.onMouseClicked = {
            hideMenuScene(0)
            showMenuScene(startMenuScene)
        }
    }

    init {
        /*
        val resource = GameApplication::class.java.getResource("/PenguinAttack.ttf")
            ?: throw FileNotFoundException()
        val fontFile = File(resource.toURI())
        loadFont(fontFile)
         */

        rootService.addRefreshables(
            this,
            startMenuScene,
            playersScene,
            joinScene,
            gameScene,
            leaderboardScene
        )
        this.showMenuScene(startMenuScene)
        this.showGameScene(gameScene)
    }

    override fun refreshAfterStartGame() {
        hideMenuScene(0)
    }

    override fun refreshAfterEndGame(points: List<Pair<Player, Int>>) {
        this.showMenuScene(leaderboardScene)
        leaderboardScene.setLabelsAfterGame(points)
    }

    override fun refreshAfterLoadGame() {
        hideMenuScene(0)
    }

    override fun refreshAfterJoinGame(playerNames: List<String>) {
        this.showMenuScene(joinScene)
    }

}
