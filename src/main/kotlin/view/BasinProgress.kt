package view

import tools.aqua.bgw.components.uicomponents.ProgressBar

/**
 * This view class extends an ordinary [ProgressBar] such that it fits in the Aquaretto game
 * in terms of colors and appropriate step size to monitor the [entity.Animal] populations of a [entity.Player].
 *
 * @property posX the x coordinate
 * @property posY the y coordinate
 */

class BasinProgress(posX : Int = 0, posY : Int = 0) :
    ProgressBar(posX, posY, width=220, barColor = BLUE) {

    /**
     * some function for view
     */
    fun progress() {
        when(progress) {
            0.0 -> { progress = 0.083}
            0.083 -> { progress = 0.167}
            0.167 -> { progress = 0.25}
            0.25 -> { progress = 0.333}
            0.333 -> { progress = 0.417}
            0.417 -> { progress = 0.5
                        barColor = SCENE }
            0.5 -> { progress = 0.583}
            0.583 -> { progress = 0.667}
            0.667 -> { progress = 0.75}
            0.75 -> { progress = 0.833}
            0.833 -> { progress = 0.917}
            0.917 -> { progress = 0.95}
            0.95 -> { progress = 1.0
                    barColor = ACCENT }
            1.0 -> { }
        }
    }

    /**
     * A method to make the bar progress depending on absolut counts
     */
    fun progress(count : Int) {
        when(count) {
            0 -> { progress = 0.0 }
            1 -> { progress = 0.083}
            2 -> { progress = 0.167}
            3 -> { progress = 0.25}
            4 -> { progress = 0.333}
            5 -> { progress = 0.417}
            6 -> { progress = 0.5
                barColor = SCENE }
            7 -> { progress = 0.583}
            8 -> { progress = 0.667}
            9 -> { progress = 0.75}
            10 -> { progress = 0.833}
            11 -> { progress = 0.917}
            12 -> { progress = 0.95}
            13 -> { progress = 1.0
                barColor = ACCENT }
        }
    }

}