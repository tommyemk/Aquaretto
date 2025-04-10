package service

/**
 *
 * class doing connection things
 */
enum class ConnectionState {
    /**
     * Disconnected from bgw server
     */
    DISCONNECTED,
    /**
     * Connected to bgw server
     */
    CONNECTED,
    /**
     * After requesting to host a game
     */
    WAITING_FOR_HOST_CONFIRMATION,
    /**
     * After requesting to join a game
     */
    WAITING_FOR_JOIN_CONFIRMATION,
    /**
     * After hosting a game, but before starting it
     */
    WAITING_FOR_GUESTS,
    /**
     * Waiting for host to start
     */
    WAITING_FOR_INIT,
    /**
     * Opponents turn
     */
    WAITING_FOR_OPPONENT,
    /**
     * Local players turn
     */
    WAITING_FOR_MY_TURN
}