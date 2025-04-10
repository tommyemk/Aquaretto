package service

import entity.AquarettoGame
import service.AI.AIActionService
import view.Refreshable

/**
 * rootservice doing the rootservice and conecting the other services
 */
class RootService(val currentGame: AquarettoGame = AquarettoGame()) {

    val playerActionService = PlayerActionService(this)
    val aquarettoGameService = AquarettoGameService(this)
    val AIActionService = AIActionService(this)
    val networkService = NetworkService(this)

    /**
     * adds refreshables
     */
    fun addRefreshable(newRefreshable: Refreshable) {
        aquarettoGameService.addRefreshable(newRefreshable)
        playerActionService.addRefreshable(newRefreshable)
        //aIActionService.addRefreshable(newRefreshable)
        networkService.addRefreshable(newRefreshable)
    }

    /**
     * adds refreshables too or something
     */
    fun addRefreshables(vararg newRefreshables: Refreshable) {
        newRefreshables.forEach { addRefreshable(it) }
    }
}