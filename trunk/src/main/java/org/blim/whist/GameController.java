package org.blim.whist;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * TODO: Does not protect against unauthenticated users.
 * 
 * @author Lee Denison (lee@longlost.info)
 */
@Controller
public class GameController {

	private SessionFactory sessionFactory;
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	@Autowired
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@RequestMapping("/")
	public ModelAndView gameList(Principal user) {
		Map<String, Object> model = new HashMap<String, Object>();
		
		model.put("user", user.getName());
		
		return new ModelAndView("ListGames", model);
	}

	@Transactional
	@RequestMapping(value = "/create-game", method = RequestMethod.POST)
	public ModelAndView createGame(Principal user) {
		Map<String, Object> model = new HashMap<String, Object>();
		Game game = new Game();
		Session session = sessionFactory.getCurrentSession();
	
		game.setCreationDate(new Date());
		game.setRoundSequence(Game.ROUND_SEQUENCE_DFLT);
		game.getPlayers().add(user.getName());
		session.save(game);
		session.flush();
	
		model.put("id", game.getId());
	
		return new ModelAndView("redirect:/game", model);
	}

	@Transactional
	@RequestMapping(value = "/join-game", method = RequestMethod.POST)
	public ModelAndView joinGame(HttpServletResponse response, @RequestParam("id") Long gameId, Principal user) {
		Map<String, Object> model = new HashMap<String, Object>();
		Game game = new Game();
		Session session = sessionFactory.getCurrentSession();
	
		session.load(game, gameId);
				
		if (!game.getPlayers().contains(user.getName())) {
			game.getPlayers().add(user.getName());
			session.save(game);
		}
	
		model.put("id", gameId);
		
		return new ModelAndView("redirect:/game", model);
	}

	@Transactional
	@RequestMapping(value = "/start-game", method = RequestMethod.POST)
	public ModelAndView startGame(HttpServletResponse response, @RequestParam("id") Long gameId, Principal user) {
		Map<String, Object> model = new HashMap<String, Object>();
		Game game = new Game();
		Session session = sessionFactory.getCurrentSession();
	
		session.load(game, gameId);
				
		if (game.getPlayers().get(0).equals(user.getName())) {
			game.addRound();
			game.addTrick();
			session.save(game);
		}
	
		model.put("id", gameId);
		
		return new ModelAndView("redirect:/game", model);
	}

	@RequestMapping("/game")
	public ModelAndView gameState(@RequestParam("id") Long gameId, Principal user) {
		Map<String, Object> model = new HashMap<String, Object>();
		Game game = new Game();
		List<JSONObject> JSONRounds = Lists.newArrayList();
		Session session = sessionFactory.getCurrentSession();

		session.load(game, gameId);

		for (int idx = 0; idx < game.getRounds().size(); idx++) {
			JSONRounds.add(roundAsJSON(game, idx));
		}
		
		model.put("game", game);
		model.put("rounds", JSONRounds.toString());
		model.put("roundCount", game.getRoundSequence().length);
		model.put("user", user.getName());
		model.put("userIndex", game.getPlayerIndex(user.getName()));
		
		return new ModelAndView("GameBoard", model);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping("/games")
	public void games(
			HttpServletResponse response,
			Principal user) throws IOException {
		Session session = sessionFactory.getCurrentSession();
		JSONArray JSONGames = new JSONArray();
		
		List<Game> games = session.createQuery("from Game").list();
	
		for (Game game : games) {
			if (game.getRounds().size() == 0) {
				JSONObject JSONGame = new JSONObject();
				JSONGame.put("creationDate",game.getCreationDate().toString());
				JSONGame.put("players",game.getPlayers());
				JSONGame.put("id",game.getId());
				JSONGames.add(JSONGame);				
			}
		}
		
		response.getWriter().print(JSONGames);
	}

	@Transactional
	@RequestMapping(value = "/gameStart", method = RequestMethod.POST)
	@SuppressWarnings("unchecked")
	public void gameStartCheck(
			HttpServletResponse response,
			HttpServletRequest request,
			Principal user) throws IOException {
		Game game = new Game();
		JSONObject JSONResult = new JSONObject();
	
		JSONObject JSONInput = parseInput(request);		
		if (JSONInput.containsKey("internalError")) {
			response.getWriter().print(JSONInput);
			return;
		}
					
		Session session = sessionFactory.getCurrentSession();
			
		Long gameId = ((Number) JSONInput.get("id")).longValue();
		session.load(game, gameId);
	
		Round currentRound = game.getCurrentRound();
		
		if (currentRound == null) {
			// Not started yet
			JSONResult.put("phase", -1);
		} else {
			JSONResult.put("phase", 0);
		}
	
		JSONResult.put("players", game.getPlayers());
		response.getWriter().print(JSONResult);
	}

	@Transactional
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@SuppressWarnings("unchecked")
	public void update(
			HttpServletResponse response,
			HttpServletRequest request,
			Principal user) throws IOException {
		Game game = new Game();
		JSONObject JSONResult = new JSONObject();
	
		JSONObject JSONInput = parseInput(request);		
		if (JSONInput.containsKey("internalError")) {
			response.getWriter().print(JSONInput);
			return;
		}
					
		Session session = sessionFactory.getCurrentSession();
			
		Long gameId = ((Number) JSONInput.get("id")).longValue();
		Integer clientPhase = ((Number) JSONInput.get("phase")).intValue();
	
		session.load(game, gameId);
	
		Round currentRound = game.getCurrentRound();
		int idx = game.getRounds().indexOf(currentRound);
	
		// Check for game end
		if (game.getRounds().size() == game.getRoundSequence().length &&
				Iterables.getLast(game.getRounds()).isFinished()) {
			JSONResult.put("round", roundAsJSON(game, idx));
			JSONResult.put("trick", trickAsJSON(game, idx));
			JSONResult.put("phase", 3);
			response.getWriter().print(JSONResult);
			return;
		}
	
		int currentPhase;
		if (currentRound.getNumberOfBids() < game.getPlayers().size()) {
			currentPhase = 0;
		} else if (currentRound.getTrumps() == null) {
			currentPhase = 1;
		} else {
			currentPhase = 2;
		}
	
		if (clientPhase <= 0 || currentPhase == 0) {
			JSONResult.put("hand", handAsJSON(game, game.getPlayerIndex(user.getName())));
		}
		if (clientPhase != 2 || currentPhase != 2) {
			JSONResult.put("round", roundAsJSON(game, idx));
		}
		if (clientPhase == 2 || currentPhase == 2) {
			JSONResult.put("trick", trickAsJSON(game, idx));
		}
		
		// Calculate activePlayer
		JSONResult.put("activePlayer", game.activePlayer());
		JSONResult.put("phase", currentPhase);
		
		response.getWriter().print(JSONResult);
	}

	@Transactional
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/bid", method = RequestMethod.POST)
	public void bid(
			HttpServletResponse response,
			HttpServletRequest request,
			Principal user) throws IOException {
		Game game = new Game();
		JSONObject JSONResult = new JSONObject();

		JSONObject JSONInput = parseInput(request);		
		if (JSONInput.containsKey("internalError")) {
			response.getWriter().print(JSONInput);
			return;
		}
					
		Session session = sessionFactory.getCurrentSession();

		Long gameId = ((Number) JSONInput.get("id")).longValue();
		Integer bid = ((Number) JSONInput.get("bid")).intValue();

		session.load(game, gameId);
			
		Round currentRound = game.getCurrentRound();
		int player = game.getPlayerIndex(user.getName());
		currentRound.bid(player, bid);
		
		session.save(game);

		JSONResult.put("result", "0");
		
		response.getWriter().print(JSONResult);
	}	

	@Transactional
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/trumps", method = RequestMethod.POST)
	public void trumps(
			HttpServletResponse response,
			HttpServletRequest request,
			Principal user) throws IOException {
		Game game = new Game();
		JSONObject JSONResult = new JSONObject();

		JSONObject JSONInput = parseInput(request);		
		if (JSONInput.containsKey("internalError")) {
			response.getWriter().print(JSONInput);
			return;
		}
					
		Session session = sessionFactory.getCurrentSession();

		Long gameId = ((Number) JSONInput.get("id")).longValue();
		Card.Suit trumps = Enum.valueOf(Card.Suit.class, JSONInput.get("trumps").toString());

		session.load(game, gameId);
			
		Round currentRound = game.getCurrentRound();
		currentRound.setTrumps(trumps);
		
		session.save(game);

		JSONResult.put("result", "0");
		
		response.getWriter().print(JSONResult);
	}
	
	@Transactional
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/play-card", method = RequestMethod.POST)
	public void playCard(
			HttpServletResponse response,
			HttpServletRequest request,
			Principal user) throws IOException {
		Game game = new Game();
		JSONObject JSONResult = new JSONObject();

		JSONObject JSONInput = parseInput(request);		
		if (JSONInput.containsKey("internalError")) {
			response.getWriter().print(JSONInput);
			return;
		}

		Session session = sessionFactory.getCurrentSession();

		Long gameId = ((Number) JSONInput.get("id")).longValue();
		JSONResult.put("card", JSONInput.get("card").toString());
		String enumConstant = JSONInput.get("card").toString().replace("-", "_");
		Card card = Enum.valueOf(Card.class, enumConstant);

		session.load(game, gameId);

		int player = game.getPlayerIndex(user.getName());
		game.playCard(player, card);
		
		session.save(game);
		
		JSONResult.put("result", "0");
				
		response.getWriter().print(JSONResult);
	}	
	
	@SuppressWarnings("unchecked")
	private JSONObject parseInput(HttpServletRequest request) {
		JSONObject json = new JSONObject();
		BufferedReader reader = null;
		
		try {
			reader = request.getReader();
		} catch (IOException error) {
			json.put("internalError", error.getMessage());
			return json;
		}

		Object obj = JSONValue.parse(reader);
		
		if (!(obj instanceof JSONObject)) {
			json.put("internalError", "Received an invalid JSONObject");
		} else {
			json = (JSONObject) obj;			
		}
		
		return json;
	}
	
	@SuppressWarnings("unchecked")
	private JSONArray handAsJSON(Game game, int player) {
		JSONArray JSONHand = new JSONArray();
		List<Card> sortedCards = new ArrayList<Card>();
		
	    sortedCards.addAll(Iterables.getLast(game.getRounds()).getHands().get(player).getCards());
	    Collections.sort(sortedCards, new OrderComparator());
	    
	    JSONHand.addAll(sortedCards);
	    
	    return JSONHand;
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject roundAsJSON(Game game, int idx) {
	    JSONObject JSONRound = new JSONObject();
		Round round = game.getRounds().get(idx);
	    
	    JSONRound.put("idx", idx);
	    JSONRound.put("trumps", round.getTrumps());
	    JSONRound.put("bids", round.getBids());
	    JSONRound.put("scores", game.scores());
	    JSONRound.put("highestBidder", game.highestBidder());
	    JSONRound.put("numberOfCards", round.getNumberOfCards());

	    return JSONRound;
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject trickAsJSON(Game game, int idx) {
		Round round = game.getCurrentRound();
		JSONObject JSONTrick = new JSONObject();
			
		if (round.getTricks().size() > 0) {
			Trick trick = Iterables.getLast(round.getTricks());
			JSONTrick.put("cards", trick.getCards());
		    JSONTrick.put("tricksWon", round.tricksWon());
		}

		// Bit of a hack, but as we automatically move on when you play a card
		// the last trick winner of a round is never reported to the client
		if (game.isFinished() || (idx == 1 && round.getTricks().size() == 1)) {
		    JSONTrick.put("prevTricksWon", game.getRounds().get(idx - 1).tricksWon());
		}

		return JSONTrick;
	}
	
}