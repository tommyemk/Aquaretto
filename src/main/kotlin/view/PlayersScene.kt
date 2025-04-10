package view

import entity.PlayerType
import service.RootService
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

/**
 * [MenuScene] for when either the localGame-Button or the onlineGame-Button from the [StartMenuScene] is
 * clicked. For a local game, [PlayersScene] displays TextFields to enter the local player's names and
 * CheckBoxes to mark them as AIs. For an online game, the names of the players and the
 * invitation-code are displayed.
 */
class PlayersScene(private val rootService: RootService) : MenuScene(680, 1080), Refreshable {

    var numVisible = 2 // number of visible players
    private var currentOrderNumber = 1

    private val enterNameLabel = Label(
        posX = 120, posY = 135,
        width = 400,
        text = "Enter your names:",
        font = Font(size=36, color=DARK, family="Calibri", fontWeight = Font.FontWeight.BOLD )
    )

    private val aiControlledLabel = Label(
        posX = 340, posY = 195,
        width = 300,
        text = "Choose Player-type",
        font = Font(size=16, color=DARK, family="Calibri", fontWeight = Font.FontWeight.BOLD )
    )

    private val orderLabel = Label(
        posX = 60, posY = 195,
        width = 150,
        text = "Set Order",
        font = Font(size=16, color=DARK, family="Calibri", fontWeight = Font.FontWeight.BOLD )
    )

    private val inviteCodeLabel = Label(
        posX = 425, posY = 25,
        text = "Invite Code:",
        width = 250,
        font = Font(size=19, color=DARK, family="Calibri", fontWeight = Font.FontWeight.BOLD )
    )

    private val gridPanePlayers = GridPane<TextField>(
        posX = 180, posY = 230,
        rows = 5, columns = 1,
        layoutFromCenter = false,
        spacing = 20
    )

    private val gridPanePlayerTypes = GridPane<ComboBox<String>>(
        posX = 420, posY = 231,
        rows = 5, columns = 1,
        layoutFromCenter = false,
        spacing = 31
    )

    private val gridPaneOrder = GridPane<Button>(
        posX = 120, posY = 234,
        rows = 5, columns = 1,
        layoutFromCenter = false,
        spacing = 30
    )

    val playerNamesList = listOf(
        TextField(
            text = "Player 1",
            width = 200, height = 40,
            font = Font(size=22, color=DARK, family="Calibri", fontWeight = Font.FontWeight.BOLD)
        ),
        TextField(
            text = "Player 2",
            width = 200, height = 40,
            font = Font(size=22, color=DARK, family="Calibri", fontWeight = Font.FontWeight.BOLD)
        ),
        TextField(
            text = "Player 3",
            width = 200, height = 40,
            font = Font(size=22, color=DARK, family="Calibri", fontWeight = Font.FontWeight.BOLD)
        ),
        TextField(
            text = "Player 4",
            width = 200, height = 40,
            font = Font(size=22, color=DARK, family="Calibri", fontWeight = Font.FontWeight.BOLD)
        ),
        TextField(
            text = "Player 5",
            width = 200, height = 40,
            font = Font(size=22, color=DARK, family="Calibri", fontWeight = Font.FontWeight.BOLD)
        )
    )

    val playerTypeComboBoxList = listOf(
        ComboBox(
            width = 160,
            items = listOf("Human", "Random AI", "Strong AI"),
            prompt = "Human",
            font = Font(size=18, color=DARK, family = "Calibri")
        ),
        ComboBox(
            width = 160,
            items = listOf("Human", "Random AI", "Strong AI"),
            prompt = "Human",
            font = Font(size=18, color=DARK, family = "Calibri")
        ),
        ComboBox(
            width = 160,
            items = listOf("Human", "Random AI", "Strong AI"),
            prompt = "Human",
            font = Font(size=18, color=DARK, family = "Calibri")
        ),
        ComboBox(
            width = 160,
            items = listOf("Human", "Random AI", "Strong AI"),
            prompt = "Human",
            font = Font(size=18, color=DARK, family = "Calibri")
        ),
        ComboBox(
            width = 160,
            items = listOf("Human", "Random AI", "Strong AI"),
            prompt = "Human",
            font = Font(size=18, color=DARK, family = "Calibri")
        )
    )

    val orderButtonList = listOf(
        Button(width = 30, height = 30, text = ""),
        Button(width = 30, height = 30, text = ""),
        Button(width = 30, height = 30, text = ""),
        Button(width = 30, height = 30, text = ""),
        Button(width = 30, height = 30, text = "")
    )

    private val plusButton = Button(
        posX = 200, posY = 230 + 60*numVisible,
        width = 120, height = 35,
        visual = ColorVisual(LIGHT).apply { opacity = 0.5 },
        text = "+",
        font = Font(size=18, color=DARK, family = "Calibri")
    ).apply {
        onMouseClicked = {
            if(numVisible < 5){
                numVisible++
                updateAfterChangedNumVisibleLocal()
                var orderSet = true
                for(i in 0 until numVisible){
                    if(!randomToggleButton.isSelected){
                        orderButtonList[i].isVisible = true
                    }
                    orderSet = orderSet && orderButtonList[i].text.isNotEmpty()
                }
                startButton.isDisabled = numVisible < 2 || numVisible > 5 || !orderSet
            }
        }
    }

    private val minusButton = Button(
        posX = 340, posY = 230 + 60*numVisible,
        width = 120, height = 35,
        visual = ColorVisual(LIGHT).apply { opacity = 0.5 },
        text = "-",
        font = Font(size=18, color=DARK, family = "Calibri")
    ).apply {
        isVisible = false
        onMouseClicked = {
            if(numVisible > 2){
                numVisible--
                updateAfterChangedNumVisibleLocal()
                if(orderButtonList[numVisible].text != ""){
                    resetOrderButtons()
                }
                var orderSet = true
                for(i in 0 until numVisible){
                    if(!randomToggleButton.isSelected){
                        orderButtonList[i].isVisible = true
                    }
                    orderSet = orderSet && orderButtonList[i].text.isNotEmpty()
                }
                startButton.isDisabled = numVisible < 2 || numVisible > 5 || !orderSet
            }
        }
    }

    val startButton = Button(
        posX = 370, posY = 750,
        width = 180, height = 50,
        visual = ColorVisual(LIGHT).apply { opacity = 0.5 },
        text = "Start",
        font = Font(size=24, color=ACCENT, family = "Calibri",
            fontStyle = Font.FontStyle.ITALIC, fontWeight = Font.FontWeight.BOLD)
    )

    private val hostButton = Button(
        posX = 370, posY = 750,
        width = 180, height = 50,
        visual = ColorVisual(LIGHT).apply { opacity = 0.5 },
        text = "Host",
        font = Font(size=24, color=ACCENT, family = "Calibri",
            fontStyle = Font.FontStyle.ITALIC, fontWeight = Font.FontWeight.BOLD)
    ).apply {
        onMouseClicked = {
            rootService.networkService.hostGame(
                playerNamesList[0].text,
                when(playerTypeComboBoxList[0].selectedItem){
                    "Human" -> PlayerType.LOCAL_HUMAN
                    "Random AI" -> PlayerType.LOCAL_RANDOM_AI
                    "Strong AI" -> PlayerType.LOCAL_AI
                    else -> throw IllegalArgumentException("PlayerType must be selected!")
                }
            )
        }
    }

    val backButton = Button(
        posX = 130, posY = 750,
        width = 180, height = 50,
        visual = ColorVisual(LIGHT).apply { opacity = 0.5 },
        text = "Back",
        font = Font(size=24, family = "Calibri",
            fontStyle = Font.FontStyle.ITALIC, fontWeight = Font.FontWeight.BOLD)
    )

    private val resetOrderButton = Button(
        posX = 60, posY = 575,
        width = 160, height = 40,
        text = "Reset Order",
        font = Font(size=24, color=ACCENT, family = "Calibri",
            fontStyle = Font.FontStyle.ITALIC, fontWeight = Font.FontWeight.BOLD)
    ).apply {
        isDisabled = true
        onMouseClicked = { resetOrderButtons() }
    }

    val randomToggleButton = ToggleButton(
        posX = 165, posY = 670,
        width = 350, height = 50,
        text = "Random Player Order?",
        font = Font(size=24, family = "Calibri",
            fontStyle = Font.FontStyle.ITALIC, fontWeight = Font.FontWeight.BOLD)
    ).apply {
        onMouseClicked = {
            if(isSelected){
                orderButtonList.forEach { it.isVisible = false }
                orderLabel.isVisible = false
                resetOrderButton.isVisible = false
                if(!isOnline){
                    startButton.isDisabled = numVisible < 2 || numVisible > 5
                }
            } else {
                if(!isOnline){
                    var orderSet = true
                    for(i in 0 until numVisible){
                        orderButtonList[i].isVisible = true
                        orderSet = orderSet && orderButtonList[i].text.isNotEmpty()
                    }
                    startButton.isDisabled = numVisible < 2 || numVisible > 5 || !orderSet
                    orderLabel.isVisible = true
                    resetOrderButton.isVisible = true
                }
            }
        }
    }

    var isOnline = false

    init {
        background = ColorVisual(BLUE)
        opacity = 0.6

        for(i in playerNamesList.indices){
            playerNamesList[i].apply {
                onKeyTyped = {
                    var nameMissing = false
                    playerNamesList.forEach{nameMissing = nameMissing || it.text == ""}
                    startButton.isDisabled = nameMissing
                }
            }
            gridPanePlayers[0, i] = playerNamesList[i]
        }

        for(i in playerTypeComboBoxList.indices){
            gridPanePlayerTypes[0, i] = playerTypeComboBoxList[i]
        }

        for(i in orderButtonList.indices){
            gridPaneOrder[0, i] = orderButtonList[i]
            orderButtonList[i].apply {
                onMouseClicked = {
                    isDisabled = true
                    resetOrderButton.isDisabled = false
                    text = "$currentOrderNumber"
                    currentOrderNumber++
                    var orderSet = true
                    for(j in 0 until numVisible){
                        orderSet = orderSet && orderButtonList[j].text.isNotEmpty()
                    }
                    startButton.isDisabled = !orderSet && !randomToggleButton.isSelected
                }
            }
        }

        for(i in numVisible until playerNamesList.size){
            playerNamesList[i].isVisible = false
            playerTypeComboBoxList[i].isVisible = false
            orderButtonList[i].isVisible = false
        }

        addComponents(
            enterNameLabel, startButton, gridPanePlayers, gridPanePlayerTypes, aiControlledLabel,
            plusButton, minusButton, inviteCodeLabel, backButton, hostButton, randomToggleButton,
            gridPaneOrder, orderLabel, resetOrderButton
        )

        playerNamesList[0].apply {
            onKeyTyped = {
                this@PlayersScene.hostButton.isDisabled = this.text.isEmpty()
            }
        }
    }

    /**
     * Method is called when the local-Button in the [StartMenuScene] has been clicked. It sets
     * up the [PlayersScene] for a local game, so the invitation-code is invisible.
     */
    fun setupLocalGame(){
        numVisible = 2
        currentOrderNumber = 1
        minusButton.isVisible = false
        plusButton.isVisible = true
        inviteCodeLabel.isVisible = false
        randomToggleButton.isSelected = false
        plusButton.posY = (230 + 60*numVisible).toDouble()
        minusButton.posY = (230 + 60*numVisible).toDouble()
        resetOrderButton.isDisabled = true

        for(i in playerNamesList.indices){
            playerNamesList[i].isVisible = false
            playerNamesList[i].isDisabled = false
            playerNamesList[i].text = "Player ${i + 1}"

            playerTypeComboBoxList[i].isVisible = false
            playerTypeComboBoxList[i].selectedItem = "Human"
            playerTypeComboBoxList[i].isDisabled = false

            orderButtonList[i].isVisible = false
            orderButtonList[i].text = ""
            orderButtonList[i].isDisabled = false
        }

        for(i in 0 until numVisible){
            playerNamesList[i].isVisible = true
            playerTypeComboBoxList[i].isVisible = true
            orderButtonList[i].isVisible = true
        }
        startButton.isDisabled = true
        startButton.isVisible = true
        randomToggleButton.isDisabled = false
        hostButton.isVisible = false
        isOnline = false
        orderLabel.isVisible = true
    }

    /**
     * Method is called when the online-Button in the [StartMenuScene] has been clicked. It sets
     * up the [PlayersScene] for an online game, so the invitation-code is visible and some Buttons
     * are invisible.
     */
    fun setupOnlineGame(){
        numVisible = 1
        currentOrderNumber = 1
        minusButton.isVisible = false
        plusButton.isVisible = false
        inviteCodeLabel.isVisible = true
        randomToggleButton.isSelected = false
        orderLabel.isVisible = false
        resetOrderButton.isVisible = false

        for(i in playerNamesList.indices){
            playerNamesList[i].isVisible = false
            playerNamesList[i].isDisabled = true
            playerTypeComboBoxList[i].isVisible = false
            playerTypeComboBoxList[i].selectedItem = "Human"
            playerTypeComboBoxList[i].isDisabled = true
            orderButtonList[i].isVisible = false
            orderButtonList[i].isDisabled = false
        }
        playerNamesList[0].text = "Player 1"
        playerNamesList[0].isDisabled = false
        playerTypeComboBoxList[0].isDisabled = false
        for(i in 0 until numVisible){
            playerNamesList[i].isVisible = true
            playerTypeComboBoxList[i].isVisible = true
        }

        startButton.isDisabled = true
        randomToggleButton.isDisabled = false
        startButton.isVisible = false
        hostButton.isVisible = true
        isOnline = true
    }

    /**
     * Function to update the [PlayersScene] after button-presses when setting up
     * a local game
     */
    private fun updateAfterChangedNumVisibleLocal(){
        for(i in playerNamesList.indices){
            playerNamesList[i].isVisible = false
            playerTypeComboBoxList[i].isVisible = false
            orderButtonList[i].isVisible = false
        }

        for(i in 0 until numVisible){
            playerNamesList[i].isVisible = true
            playerTypeComboBoxList[i].isVisible = true
            if(!randomToggleButton.isSelected){
                orderButtonList[i].isVisible = true
            }
        }

        plusButton.posY = (230 + 60*numVisible).toDouble()
        plusButton.isVisible = numVisible < 5
        minusButton.posY = (230 + 60*numVisible).toDouble()
        minusButton.isVisible = numVisible > 2
    }

    /**
     * Function to update the [PlayersScene] after certain events when setting up
     * an online game (e.g. a new player joins)
     */
    private fun updateAfterChangedNumVisibleOnline(){
        for(i in playerNamesList.indices){
            playerNamesList[i].isVisible = false
            playerTypeComboBoxList[i].isVisible = false
        }

        var orderSet = true
        for(i in 0 until numVisible){
            playerNamesList[i].isVisible = true
            playerTypeComboBoxList[i].isVisible = true
            orderButtonList[i].isVisible = true
            orderSet = orderSet && orderButtonList[i].text.isNotEmpty()
        }
        startButton.isDisabled = numVisible < 2 || numVisible > 5 || (!orderSet && !randomToggleButton.isSelected)
    }

    /**
     * Update GUI after the host-game button has been clicked
     */
    override fun refreshAfterHostGame(inviteCode: String) {
        inviteCodeLabel.text = "Invite code: $inviteCode"
        playerNamesList[0].isDisabled = true
        hostButton.isVisible = false
        playerTypeComboBoxList[0].isDisabled = true
        orderButtonList[0].isVisible = !randomToggleButton.isSelected
        orderLabel.isVisible = !randomToggleButton.isSelected
        resetOrderButton.isVisible = !randomToggleButton.isSelected
        startButton.isVisible = true
        startButton.isDisabled = true
        randomToggleButton.isDisabled = true
    }

    /**
     * Function to generally update the GUI with the player names
     */
    private fun updatePlayerNames(playerNames: List<String>) {
        numVisible = playerNames.size
        playerNames.forEachIndexed { i, name ->
            playerNamesList[i].text = name
        }
        updateAfterChangedNumVisibleOnline()
    }

    /**
     * Update GUI after a player joins
     */
    override fun refreshAfterPlayerJoin(playerNames: List<String>) {
        updatePlayerNames(playerNames)
    }

    /**
     * Update GUI after a player leaves
     */
    override fun refreshAfterPlayerLeave(playerNames: List<String>) {
        updatePlayerNames(playerNames)
    }

    /**
     * Function that is called when the reset-order button is clicked. Resets the orderButton so that
     * a user can input a different order.
     */
    private fun resetOrderButtons(){
        currentOrderNumber = 1
        orderButtonList.forEach{
            it.isDisabled = false
            it.text = ""
        }
        resetOrderButton.isDisabled = true
        startButton.isDisabled = true
    }
}