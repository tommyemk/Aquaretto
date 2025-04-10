package entity

/**
 * The animal type of tile
 */
enum class Animal {

    /**
     * The player is able to hold and place DOLPHIN-Tiles
     */
    DOLPHIN,

    /**
     * The player is able to hold and place ORCA-Tiles
     */
    ORCA,

    /**
     * The player is able to hold and place SEA_TURTLE-Tiles
     */
    SEA_TURTLE,

    /**
     * The player is able to hold and place PENGUIN-Tiles
     */
    PENGUIN,

    /**
     * The player is able to hold and place POLAR_BEAR-Tiles
     */
    POLAR_BEAR,

    /**
     * The player is able to hold and place SEA_LION-Tiles
     */
    SEA_LION,

    /**
     * The player is able to hold and place CROCODILE-Tiles
     */
    CROCODILE,

    /**
     * The player is able to hold and place HIPPO-Tiles
     */
    HIPPO;

    override fun toString() =
        when (this) {
            DOLPHIN -> "Dolphin"
            ORCA -> "Orca"
            SEA_TURTLE -> "Sea Turtle"
            PENGUIN -> "Penguin"
            POLAR_BEAR -> "Polar Bear"
            SEA_LION -> "Sea Lion"
            CROCODILE -> "Crocodile"
            HIPPO -> "Hippo"
        }

}