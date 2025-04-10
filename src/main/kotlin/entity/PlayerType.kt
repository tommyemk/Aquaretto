package entity

/**
 * Describes how a player is controlled
 */
enum class PlayerType {
    /**
     * Player is controlled by human input
     */
    LOCAL_HUMAN,
    /**
     * Player is controlled by AI
     */
    LOCAL_AI,
    /**
     * Player does randomized turns
     */
    LOCAL_RANDOM_AI,
    /**
     * Player is controlled remotely
     */
    ONLINE
}