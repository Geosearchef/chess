

enum class Player(val mask: Int) {
    WHITE(0b00000000), BLACK(0b01000000);

    val otherPlayer: Player get() = Player.values().find { it != this }!!
}

