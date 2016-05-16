package poker.app.model;

import java.io.IOException;

import exceptions.DeckException;
import netgame.common.Hub;
import pokerBase.Action;
import pokerBase.Card;
import pokerBase.Deck;
import pokerBase.GamePlay;
import pokerBase.Player;
import pokerBase.Rule;
import pokerBase.Table;
import pokerEnums.eGame;
import pokerEnums.eGameState;

public class PokerHub extends Hub {

	private Table HubPokerTable = new Table();
	private GamePlay HubGamePlay;
	private int iDealNbr = 0;
	//private PokerGameState state;
	private eGameState eGameState;

	public PokerHub(int port) throws IOException {
		super(port);
	}

	protected void x(int playerID) {

		if (HubGamePlay.getGamePlayers().size() == 4) {
			shutdownServerSocket();
		}
	}

	protected void playerDisconnected(int playerID) {
		shutDownHub();
	}

	protected void messageReceived(int ClientID, Object message) {

		if (message instanceof Action) {
			Action act = (Action) message;
			switch (act.getAction()) {
			case GameState:
				sendToAll(HubPokerTable);
				break;
			case TableState:
				resetOutput();
				sendToAll(HubPokerTable);
				break;
			case Sit:
				resetOutput();
				HubPokerTable.AddPlayerToTable(act.getPlayer());				
				sendToAll(HubPokerTable);				
				break;
			case Leave:
				resetOutput();
				HubPokerTable.RemovePlayerFromTable(act.getPlayer());
				sendToAll(HubPokerTable);				
				break;
				
			case StartGame:
				System.out.println("Starting Game!");
				resetOutput();
				
				eGame game = act.geteGame();
				Rule rule = HubGamePlay.getRule();
				Player dealer = HubPokerTable.PickRandomPlayerAtTable();
				GamePlay gamePlay = new GamePlay(rule, dealer.getPlayerID());
				
				for (int x=1; x<=4; x++){
					Player p = HubPokerTable.getPlayerByPosition(x);
					gamePlay.addPlayerToGame(p);
				}
				
				Deck newDeck = gamePlay.getGameDeck();

				//	Determine the order of players and add each player in turn to GamePlay.lnkPlayerOrder
				//	Example... four players playing...  seated in Position 1, 2, 3, 4
				//			Dealer = Position 2
				//			Order should be 3, 4, 1, 2
				//	Example...  three players playing... seated in Position 1, 2, 4
				//			Dealer = Position 4
				//			Order should be 1, 2, 4
				//		< 10 lines of code
				
				int dealerPos = dealer.getiPlayerPosition();
				
				Player p1 = new Player();
				int p1Pos = p1.getiPlayerPosition();
				
				Player p2 = new Player();
				int p2Pos = p2.getiPlayerPosition();
				
				Player p3 = new Player();
				int p3Pos = p3.getiPlayerPosition();

				Player p4 = new Player();
				int p4Pos = p4.getiPlayerPosition();
				
				//	Set PlayerID_NextToAct in GamePlay (next player after Dealer)
				//		1 line of code
					
				gamePlay.setPlayerNextToAct(p1);
				
				//	Send the state of the game back to the players
				sendToAll(HubGamePlay);
				break;
			case Deal:
				
				
				int iCardstoDraw[] = HubGamePlay.getRule().getiCardsToDraw();
				int iDrawCount = iCardstoDraw[iDealNbr];

				for (int i = 0; i<iDrawCount; i++)
				{
					try {
						Card c = HubGamePlay.getGameDeck().Draw();
					} catch (DeckException e) {
						e.printStackTrace();
					}
				}

				break;
			}
		}

		System.out.println("Message Received by Hub");
	}

}