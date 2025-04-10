package entity

import kotlinx.serialization.Serializable

/**
 * implements waterpark entity for aquaretto game
 *
 * waterpark class is known by player entity
 *
 * @param fieldMap which maps coordinates to placables
 * @param allowedAnimalType which animal tape is allowed
 */

@Serializable
data class Waterpark (
    //FieldMap probably needs to implement PlaceAble interface
    var fieldMap : MutableMap<Pair<Int,Int>,Placeable> = mutableMapOf(),
    var allowedAnimalType : Int = 3,
    val allowedExtensionList : MutableList<Pair<Int,Int>> = mutableListOf(
        Pair(9, 8), Pair(10, 8), Pair(11, 8),
        Pair(8, 9), Pair(9, 9), Pair(10, 9), Pair(11, 9), Pair(12, 9),
        Pair(8, 10), Pair(9, 10), Pair(10, 10), Pair(11, 10), Pair(12, 10),
        Pair(9, 11), Pair(10, 11), Pair(11, 11), Pair(12, 11),
        Pair(10, 12), Pair(11, 12),
        CASH_POS_1, CASH_POS_2, ANIM_POS_1, ANIM_POS_2, DEPOT_POS
    )
    // Added all the coordinates where it's allowed to place a tile -Marcel
){
    companion object{
        val CASH_POS_1 : Pair<Int,Int> = Pair(100,10)
        val CASH_POS_2 : Pair<Int,Int> = Pair(120,10)
        val ANIM_POS_1 : Pair<Int,Int> = Pair(140,10)
        val ANIM_POS_2 : Pair<Int,Int> = Pair(160,10)
        val DEPOT_POS : Pair<Int,Int> = Pair(180,10)
    }

    /**
     * clones ones waterpark
     */
    fun clone():Waterpark{
        val newWaterpark = Waterpark(
            fieldMap = this.fieldMap.entries.associate { ( it.key.copy() to (it.value.clone()))}.toMutableMap(),
            allowedAnimalType = this.allowedAnimalType,
            allowedExtensionList = this.allowedExtensionList.map { it.copy() }.toMutableList()
        )
        return newWaterpark
    }

    /**
     * Equality is only checked in tests and coworker can't be a data class (for hashmap reasons)
     * so some extra stuff is necessary
     */
    override fun equals(other: Any?): Boolean {
        if (other !is Waterpark) {
            return false
        }
        fieldMap.keys.forEach {
            if (!other.fieldMap.containsKey(it)) {
                return false
            }
            val thisTile = fieldMap[it]
            val otherTile = other.fieldMap[it]
            if (thisTile is Coworker) {
                if (otherTile !is Coworker || thisTile.coworkerTask != otherTile.coworkerTask) {
                    return false
                }
            } else if (thisTile != otherTile) {
                return false
            }
        }
        return allowedAnimalType == other.allowedAnimalType &&
                allowedExtensionList == other.allowedExtensionList
    }
}