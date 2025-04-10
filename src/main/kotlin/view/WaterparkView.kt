package view

import tools.aqua.bgw.components.container.Area
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.layoutviews.GridPane
import kotlin.math.max
import kotlin.math.min

/**
 * The view of a player's [entity.Waterpark].
 * As an extension of the class [GridPane], it consists of several quadratic [WaterparkArea]s forming a grid.
 * The WaterparkView could hold up to 21×21 WaterparkAreas. However, only a fraction will be initialized at first.
 * This fraction accounts for the actual waterpark board as well as a small margin around it.
 */

class WaterparkView(val scene: GameScene):
    GridPane<Area<TokenView>>(columns=21, rows=21, layoutFromCenter = false, ) {

    /**
     * Get gameScene with: parent.parent.scene (<CameraPane>.<RootComponent>.<GameScene>)
     */

    init {

        //The developed area
        for ( i in 8..12) {
            for ( j in 8..12 ) {
                this[i,j] = WaterparkArea(i, j, scene = scene)
            }
        }
        //The surroundings
        for (i in 6..7) {
            for (j in 6.. 14) {
                this[i,j] = ExtensionArea(i, j, scene = scene)
            }
        }
        for (i in 8..12 ) {
            for (j in 6..7) {
                this[i,j] = ExtensionArea(i,j, scene = scene)
            }
        }
        for (i in 8..12 ) {
            for (j in 13..14) {
                this[i,j] = ExtensionArea(i,j, scene = scene)
            }
        }
        for (i in 13..14) {
            for (j in 6.. 14) {
                this[i,j] = ExtensionArea(i, j, scene = scene)
            }
        }
        this[8,8] = ExtensionArea(8,8, scene = scene)
        this[12,8] = ExtensionArea(12,8, scene = scene)
        this[12,12] = ExtensionArea(12,12, scene = scene)
        //Coworker seats
        this[8,11] = Station(80, scene = scene, "Cashier") //Cashier; due to some computation, the x coordinate differs from 100
        this[8,12] = Station(160, scene = scene, "Manager").apply {
            dropAcceptor = { it.draggedComponent is CoworkerToken && components.isEmpty() }
        } //Manager
        this[9,12] = Station(120, scene = scene,"Keeper") //Animal Keeper; due to some computation, the x coordinate differs from 140

    }

    /**
     * A method to extend the waterpark
     *
     * param coordinates list of fields that now belong to the developed area and can host animals and coworkers
     */

    fun extend(coordinates : List<Pair<Int,Int>>) {

        val minX = max(0, coordinates.sortedBy { it.first }[0].first-2)
        val minY = max(0, coordinates.sortedBy { it.second }[0].second-2)
        val maxX = min(20, minX+6)
        val maxY = min(20, minY+6)

        for(coords in coordinates) {
            this[coords.first, coords.second] = WaterparkArea(coords.first, coords.second, scene = scene)
        }
        for (i in minX..maxX) {
            for (j in minY .. maxY) {
                if (this[i, j] == null) {
                    println("Pair $i, $j is Null")
                    this[i, j] = ExtensionArea(i,j, scene = scene)
                }
            }
        }
    }

}