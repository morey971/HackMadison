///////////////////////////////////////////////////////////////////////////////
//                   ALL STUDENTS COMPLETE THESE SECTIONS
// Main Class File:  FoxAndGeeseAi.java
// File:             Board.java
// Semester:         CS302 Spring 2013
//
// Author:           Tiffany Morey : tmorey@wisc.edu
// CS Login:         morey
// Lecturer's Name:  Deb Deppeler
// Lab Section:      327
//
//                   PAIR PROGRAMMERS COMPLETE THIS SECTION
// Pair Partner:     Nathan Swanson : npswanson@wisc.edu
// CS Login:         nswanson
// Lecturer's Name:  Alicia Maxwell
// Lab Section:      361
//
//                   STUDENTS WHO GET HELP FROM ANYONE OTHER THAN THEIR PARTNER
// Credits:          My Brain 
//////////////////////////// 80 columns wide //////////////////////////////////

import java.util.ArrayList;
public class Game implements GameWindow.GameInterface {
	////////////////////////////80 columns wide //////////////////////////////////
	//	INSTANCE VARIABLES	
	////////////////////////////80 columns wide //////////////////////////////////	
	/**
	 * GameWindow startingScreen: stores the GameWindow Object that is created
	 * in the game constructor. 
	 */
	private GameWindow startingScreen;

	/**
	 * Board board : stores the Board Object that is created in the game
	 * constructor.
	 */
	private Board board;

	/**
	 * boolean isFoxTurn: an instance variable that dictates 
	 * 
	 */
	private boolean isFoxTurn;
	/**
	 * GamePiece selectedGamePiece: this instance variable holds any of the  
	 * GamePiece in the game. It contains one position variable stating the 
	 * position of the GamePiece, one boolean stating whether or not is is a 
	 * type fox  GamePiece, a reference to the board object that it belongs to,
	 *  and an ArrayList of legal moves that are specific to this GamePiece.
	 */
	private GamePiece selectedGamePiece;
	/**
	 * Ai goose: an instance object that sets the pieces Ai to the passed
	 * Ai.
	 */

	public Game() {
		startingScreen = new GameWindow();
		board = new Board();
		startingScreen.gameUpdated(this);
		startingScreen.show();
		this.isFoxTurn = false;
	}


	/**
	 * Gets the Board object containing all the pieces in this Game. This method
	 * will be called by the GameWindow when it tries to display your game
	 * board on the screen. If this method returns {@code null}, the GameWindow
	 * will never show any pieces on the screen.
	 */
	public Board getBoard() {
		return this.board;
	}


	/**
	 * Gets the currently selected GamePiece. A piece becomes selected
	 * when the user clicks on it on the GUI. The user should only be permitted
	 * to select pieces whose turn it currently is. Only one piece can be the
	 * selected piece at any given time. This method will be called by the
	 * GameWindow when it tires to display your game pieces on the screen.
	 *
	 * @return The currently selected GamePiece, or {@code null} if no GamePiece
	 * is currently selected.
	 */
	public GamePiece getSelectedPiece() {

		if (this.selectedGamePiece == null)
		{
			return null;
		}
		if (isFoxTurn && 
		selectedGamePiece.getPosition().equals(board.getFox().getPosition()))
		{
			return board.getFox();
		}
		else if (!isFoxTurn &&
		!selectedGamePiece.getPosition().equals(board.getFox().getPosition()))
		{
			return selectedGamePiece;
		}
		return null;
	}

	/**
	 * Determines if the fox has lost the game. This method will be called by
	 * the GameWindow to determine if it should display the game-end screen.
	 *
	 * @return True if and only if the fox has lost the game. False if the game
	 * is still in progress or if the fox has won.
	 */
	public boolean hasFoxLost() {

		/**
		 * ArrayList<Position> p: an ArrayList of the legal moves that the 
		 * fox piece can move to when this method is called.
		 */
		ArrayList<Position> p = board.getFox().getLegalMoves();

		if (board.getFox()!=null && (p==null||p.size()<=0))
		{
			return true;
		}
		return false;
	}



	/**
	 * Determines if the geese have lost the game.This method will be called by
	 * the GameWindow to determine if it should display the game-end screen.
	 *
	 * @return True if and only if the geese have lost the game. False if the
	 * game is still in progress or if the geese have won.
	 */
	public boolean haveGeeseLost() {
		/**
		 * boolean moveLeftForAGoose: boolean that determines whether or not
		 * there are any moves left for any of the geese. If there is, then
		 * moveLeftForAGoose will be set to true and you will need to return
		 * false for haveGeeseLost.
		 */
		boolean moveLeftForAGoose = false;

		for (int i = 0; i<board.getGeese().size(); i++)
		{
			/**
			 * ArrayList<Position> p: ArrayList containing all the legal moves
			 * that the goose at the i-th index can move to.
			 * Note: this array needs to reset for each goose element. That's
			 * why it is inside the for loop.
			 */
			ArrayList<Position> p = board.getGeese().get(i).getLegalMoves();
			if (p==null||p.size()>0)
			{
				moveLeftForAGoose = true;
				break;
			}
		}
		if (board.getFox().getPosition().getRow()==GameWindow.BOARD_SIZE-1)
		{
			return true;
		}
		//return true if there is not a move left for the goose pieces
		return !moveLeftForAGoose;
	}




	/**
	 * Handles the user clicking a square on the game board. This can either
	 * change the selected game piece or move the selected game piece,
	 * depending on whether the user clicks a square containing a piece or a
	 * square where the selected piece could legally move. <b>Your code should
	 * not call this method.</b> The GameWindow class will call it for you
	 * whenever the user clicks a square on the displayed game board.
	 *
	 * <p>If the user clicks a piece controlled by the current player (i.e. if
	 * they click a fox while it is the fox's
	 * turn, or a goose while it is the geese's turn), that piece should become
	 * the selected piece. In particular, if a user clicks on an already
	 * selected piece, that piece should remain selected.If the user clicks a
	 * square containing a piece whose turn it isn't,
	 * or an empty square that isn't a legal move for the selected piece,
	 * this method should deselect all pieces by setting the selected piece
	 * to null.</p>
	 *
	 * @param clicked Position that was clicked.
	 */
	public void handleBoardClick(Position clicked) {

		//with the Ai
		if (this.goose instanceof Ai)
		{
			//checks to make sure the game isn't over
			if (hasFoxLost()||haveGeeseLost())
			{
				return;
			}
			//fox's turn
			if (isFoxTurn)
			{
				// selects fox if fox is clicked.
				if (clicked.equals(board.getFox().getPosition()))
				{
					this.selectedGamePiece = board.getFox();
					startingScreen.gameUpdated(this);
				}
				//handles every other click on the board besides the fox
				if (this.selectedGamePiece==null)
				{
					return;
				}
				/**
				 * boolean clickedLegalMoveAi: makes sure that you have clicked
				 * a legal move for the selectedGamePiece 
				 */
				boolean clickedLegalMoveAi = false;

				// A loop that sets clickedLegalMoveAi to true if you clicked a 
				// legal move for the selectedGamePiece
		for (int j = 0; j<getSelectedPiece().getLegalMoves().size(); j++)
				{
					/**
					 * Position p: contains the legal move at index j for the 
					 * selectedGamePiece
					 */
					Position p = getSelectedPiece().getLegalMoves().get(j);
					if (board.getPieceAt(p)==null && clicked.equals(p))
					{
						clickedLegalMoveAi = true;
					}
				}
				//moves the selectedGamePiece if the position clicked was a 
				//legal move
				if (clickedLegalMoveAi)
				{
					getSelectedPiece().moveTo(clicked);
					this.selectedGamePiece = null;
					this.isFoxTurn = !this.isFoxTurn;
					startingScreen.gameUpdated(this);
				}
			}
			//not the fox's turn
			if (!isFoxTurn)
			{
				this.goose.makeNextMove(board);
				isFoxTurn = !isFoxTurn;
				startingScreen.gameUpdated(this);

			}
		}

		//Without the Ai

		//check if game has been lost. Keeps the players from moving the 
		//pieces after the game has ended.
		if (hasFoxLost()||haveGeeseLost())
		{
			return;
		}

		/**
		 * boolean clickedLegalMove:  makes sure that you have clicked a legal
		 *  move for the selectedGamePiece.
		 */
		boolean clickedLegalMove = false;
		//if no GamePiece is selected yet
		if (getSelectedPiece()==null)
		{
			this.selectedGamePiece = board.getPieceAt(clicked);
			startingScreen.gameUpdated(this);
		}
		else
			//a GamePiece has been selected
			if (getSelectedPiece()!=null)
			{
				// tests to see if you have selected a position that is a legal
				// move for the selectedGamePiece to make
			for (int j = 0; j<getSelectedPiece().getLegalMoves().size(); j++)
			{
				/**
				* Position p: contains the legal move at index j for the 
				* selectedGamePiece
 				*/
				Position p = getSelectedPiece().getLegalMoves().get(j);
				if (board.getPieceAt(p)==null && clicked.equals(p))
				{
					clickedLegalMove = true;
				}
			}
				//if you did click on a legal move for the selectedGamePiece
				if (clickedLegalMove)
				{
					getSelectedPiece().moveTo(clicked);
					this.isFoxTurn = !this.isFoxTurn;
					startingScreen.gameUpdated(this);
				}
				//fox's turn if you didn't click on a legal move for the 
				//selectedGamePiece
				else if (this.isFoxTurn)
				{
					if (clicked.equals(board.getFox().getPosition()))
					{
						this.selectedGamePiece = board.getFox();
						startingScreen.gameUpdated(this);
					}
					else {
						this.selectedGamePiece = null;
						startingScreen.gameUpdated(this);
					}
				}
				//geese's turn if you didn't click on a legal move for the 
				//selectedGamePiece
				else if (!this.isFoxTurn)
				{
					/**
					 * GamePiece piece: contains the GamePiece located at the 
					 * position "clicked".
					 */
					GamePiece piece = board.getPieceAt(clicked);
					
					//if you clicked on the fox or an empty square
					if (piece == null || piece.equals(board.getFox()))
					{
						this.selectedGamePiece = null;
						startingScreen.gameUpdated(this);
					}
					
					//if you clicked on a goose
					for (int i = 0; i<board.getGeese().size(); i++)
					{
					if (clicked.equals(board.getGeese().get(i).getPosition()))
						{
							this.selectedGamePiece = board.getGeese().get(i);
							startingScreen.gameUpdated(this);
						}
					}

				}
			}
	}


	/**
	 * Determines whether it is the fox's turn. This method will be called
	 * by the GameWindow to display the proper turn at the bottom of
	 * the window.
	 *
	 * @return True if it is currently the fox's turn. False if it is currently
	 * the geese's turn.
	 */
	public boolean isFoxTurn() {
		
		return isFoxTurn;
	}
}
