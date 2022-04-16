package memory;

/**
 * A Listener for this board gameplay
 */
public interface Listener {
    /**
     * Called when the board changes:
     * A change is defined as any cards turning face up or face 
     * down or being removed from the board.
     * @param boardString the new state of the board
     */
    public void boardChanged(final String boardString);

}
