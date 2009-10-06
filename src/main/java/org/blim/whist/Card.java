package org.blim.whist;

import java.util.List;
import com.google.common.collect.Lists;
import org.json.simple.JSONAware;

public class Card implements JSONAware {
	
    public enum Value { TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE }
    public enum Suit { SPADES, HEARTS, CLUBS, DIAMONDS }

    private final Value value;
    private final Suit suit;

    private static final List<Card> cards = Lists.newArrayList();
    
    private Card(Value value, Suit suit) {
        this.value = value;
        this.suit = suit;
    }

    public Value value() { return value; }
    public Suit suit() { return suit; }
    
    public String toString() { return value.toString() + "-" + suit.toString(); }

    static {
    	for (Value value : Value.values()) {
    		for (Suit suit : Suit.values()) {
                cards.add(new Card(value, suit));
            }
        }
    }

    public static List<Card> createDeck() {
        return Lists.newArrayList(cards);
    }

    public static List<Card> sortCards(List<Card> cards) {
    	return Lists.sortedCopy(cards, new OrderComparator());
    }

	public String toJSONString() {
		return "\"" + toString() + "\"";
	}

}