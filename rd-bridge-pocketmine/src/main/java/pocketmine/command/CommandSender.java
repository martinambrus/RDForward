package pocketmine.command;

/**
 * Something that can issue a command — a player, the console, or a
 * dispatcher emulating either. Narrower than PocketMine's real
 * {@code CommandSender}: only the bits the bridge forwards from rd-api
 * {@code CommandRegistry.dispatch} are exposed.
 */
public interface CommandSender {

    String getName();

    boolean isOp();

    void sendMessage(String message);
}
