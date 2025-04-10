package entity

/**
 * Enum to distinguish between the four possible CoworkerTasks:
 * manager, animal keeper, cashier and Trainer
 */
enum class CoworkerTask {

    MANAGER,
    ANIMAL_KEEPER,
    CASHIER,
    TRAINER;

    /**
     * Provides a String representation to represent one of the four CoworkerTasks.
     *
     * @return the type of coworker represented as a String
     */
    override fun toString() = when(this) {
        MANAGER -> "Manager"
        ANIMAL_KEEPER -> "Animal Keeper"
        CASHIER -> "Cashier"
        TRAINER -> "Trainer"
    }
}