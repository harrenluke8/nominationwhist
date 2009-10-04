package org.blim.whist;
import javax.servlet.http.*;

public class Game extends HttpServlet {
	private GameData gameData = new GameData();
	private GameService gameService = new GameService();
	
	public void doGet (HttpServletRequest request,
	                       HttpServletResponse response) {

	    try {
	    	gameData.resetData();
	    	gameService.dealRound(13, gameData.playerOne(), gameData.playerTwo(), gameData.playerThree(), gameData.playerFour());
	        request.setAttribute ("playerOne", gameData.playerOne());
	        request.setAttribute ("playerTwo", gameData.playerTwo());
	        request.setAttribute ("playerThree", gameData.playerThree());
	        request.setAttribute ("playerFour", gameData.playerFour());
	        getServletConfig().getServletContext().getRequestDispatcher("/jsp/whist.jsp").forward(request, response);
	    } catch (Exception ex) {
	        ex.printStackTrace ();
	    }
	}
}
