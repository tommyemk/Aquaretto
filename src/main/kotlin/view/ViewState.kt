package view

enum class ViewState {
    StartTurn,
    ActionA,
    InventoryActionB,
    InventorySpecial,
    InventoryBuy,
    InventoryMove,
    BuyConfirmation,
    DiscardConfirmation,
    EndTurn
}