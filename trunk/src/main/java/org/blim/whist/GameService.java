package org.blim.whist;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

public class GameService {

	public static final int MAX_CARDS = 52;
	
	public static final int[] ROUND_SEQUENCE_DFLT = {13,12,11,10,9,8,7,6,5,4,3,2,2,2,2,3,4,5,6,7,8,9,10,11,12,13};

	private SessionFactory sessionFactory;

	@Autowired
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public List<Integer> gameScores(Game game) {
		List<Integer> gameScores = Lists.newArrayList();
		List<Integer> roundScores = Lists.newArrayList();
		
		for (int i = 0; i < game.getPlayers().size(); i++) {
			gameScores.set(i, new Integer(0));	
		}

		for (int i = 0; i < game.getRounds().size(); i++) {
			roundScores = roundScores(game.getRounds().get(i));
			for (int j = 0; j < game.getPlayers().size(); j++) {
				gameScores.set(j, new Integer(gameScores.get(j) + roundScores.get(i)));
			}
		}		
		
		return gameScores;
	}

	public List<Integer> roundScores(Round round) {
		
		List<Integer> scores = tricksWon(round);
		
		for (int i = 0; i < round.getHands().size(); i++) {
			if (scores.get(i).equals(round.getBids().get(i))) {
				scores.set(i, new Integer(scores.get(i) + 10));
			}
		}		
		
		return scores;
	}

	public List<Integer> tricksWon(Round round) {
		List<Integer> tricks = Lists.newArrayList();

		for (int i = 0; i < round.getHands().size(); i++) {
			tricks.add(new Integer(0));	
		}

		for (Trick trick : round.getTricks()) {
			int winningPlayer = trickWinner(trick, round.getTrumps());
			tricks.set(winningPlayer, new Integer(tricks.get(winningPlayer) + 1));
		}
		
		return tricks;
	}
	
	public int trickWinner(Trick trick, Card.Suit trumps) {
		Card highestCard = null;
		int highestPlayer = -1;
		
		int numberOfCards = trick.getCards().size();
		for (int i = trick.getFirstPlayer(); i - trick.getFirstPlayer() < numberOfCards; i++) {
			if (candidateIsWinningCard(highestCard, trick.getCards().get(i % numberOfCards), trumps)) {
				highestPlayer = i % numberOfCards;
				highestCard = trick.getCards().get(i % numberOfCards);
			}
		}
		
		return highestPlayer;
	}

	private boolean candidateIsWinningCard(Card current, Card candidate, Card.Suit trumps) {
		if (current == null) {
			return true;
		}
		
		if (current.getSuit().equals(candidate.getSuit())) {
			return current.getValue().compareTo(candidate.getValue()) < 0;
		}
		else {
			return candidate.getSuit().equals(trumps);
		}
	}

/*	public boolean roundFinished() { 
		if (trickHistory.size() == numberOfCards &&
		    trickHistory.get(numberOfCards - 1).getCards().size() == hands.size()) {
		    	return true;
		    } else 
		    	return false;
		}
*/
		
}