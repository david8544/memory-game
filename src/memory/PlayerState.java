package memory;


/**
 * Represents player game state:
 *      FRESH: player does not hold any card
 *      ONE_CARD: plater holds one card
 *      TWO_Cards: player holds two cards
 * @author davidmagrefty
 *
 */
public enum PlayerState {
    FRESH, ONE_CARD_VALID, TWO_CARDS_MATCH, TWO_CARDS_NO_MATCH, ONE_CARD_INVALID
}
