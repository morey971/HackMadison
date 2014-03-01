import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * GUI window for displaying the game to the user and receiving user input.
 * This class won't work properly unless the Board you use has 8 rows and 8
 * columns.
 * <p><b>STUDENTS SHOULD NOT EDIT THIS FILE.</b></p>
 * @author pollen
 */
@SuppressWarnings("serial") public final class GameWindow {
	/**
	 * Common superinterface for Game and GameEC. You, as the student, should
	 * not worry about what this does. We will cover it later in CS302. For now,
	 * just know that this is required for the GameWindow to work properly.
	 */
	public interface GameInterface {
		Board getBoard();
		GamePiece getSelectedPiece();
		boolean hasFoxLost();
		boolean haveGeeseLost();
		void handleBoardClick(Position clicked);
		boolean isFoxTurn();
	}

	/**
	 * The number of rows and columns that are displayed on this window. This
	 * will ALWAYS be 8.
	 */
	public static final int BOARD_SIZE = 8;

	private static final Color
	RED_SQUARE_COLOR = Color.PINK,
	BLACK_SQUARE_COLOR = Color.BLACK,
	FOX_COLOR = Color.ORANGE,
	GOOSE_COLOR = Color.WHITE,
	HALO_COLOR = Color.BLUE,
	TEXT_COLOR = Color.BLUE.darker(),
	FADE_COLOR = new Color(1, 1, 1, 0.9f);

	/** Normal, thin stroke. */
	private static final Stroke NORMAL_STROKE = new BasicStroke(1);

	/** The Game passed to the most recent gameUpdated call. */
	private GameInterface game;

	/** Window containing the GUI. */
	private final JFrame frame;


	/** GUI element for displaying the Board. */
	private final BoardPanel boardPanel;

	/** Indicates whether we have detected game-over. */
	private boolean gameOver = false;

	/** Constructs a new GameWindow but doesn't display it. */
	public GameWindow() {
		if (!ENABLED) {
			// Don't touch anything that could cause an X11 call, since this
			// might mess up a test being run over SSH.
			frame = null;

			boardPanel = null;
		} else {
			frame = new JFrame();
			frame.setTitle("Player1 and Player2");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			JPanel content = new JPanel();
			content.setLayout(new BorderLayout());
			frame.setContentPane(content);

			boardPanel = new BoardPanel();
			content.add(boardPanel, BorderLayout.CENTER);

			// Make a row of status labels along the bottom of the window.
			Box status = new Box(BoxLayout.X_AXIS);
			content.add(status, BorderLayout.SOUTH);


			// Determine frame size based on subcomponents.
			frame.pack();
		}

		synchronized (INSTANCES) {
			INSTANCES.add(this);
		}
	}

	/**
	 * Tells the GUI that the game has changed somehow. This causes the GUI
	 * to repaint itself onto the screen. Your code should call this method
	 * every time it needs the display to change based on changes to the Game.
	 * You should also call this method right away after creating a GameWindow
	 * to display the initial board. If you don't ever call this method,
	 * your GameWindow will just show an empty gray window.
	 * 
	 * @param game The current Game object. Must be an object of either the
	 *             Game class or the GameEC class (if you are doing EC).
	 */
	public void gameUpdated(GameInterface game) {
		this.game = game;
		boardPanel.repaint();
	}

	/**
	 * Makes this window visible to the user. When the window is first
	 * created, it will be invisible. You'll have to call this method at least
	 * once to make the window visible.
	 */
	public void show() {
		if (ENABLED) {
			frame.setVisible(true);
		}
	}

	/** GUI element for displaying a Board to the user. */
	private class BoardPanel extends JPanel {
		/** Minimum halo stroke width, in pixels. */
		private static final float MIN_HALO_WIDTH = 5;

		/** Standard halo width, as a percentage of cell width/height. */ 
		private static final float HALO_WIDTH = 0.2f;

		/**
		 * The padding for painting pieces in squares, as a percentage of the
		 * square's width.
		 */
		private static final float PIECE_PADDING = 0.06f;

		/** Creates a new BoardPanel. */
		public BoardPanel() {
			final Dimension size = new Dimension(600, 600);
			setSize(size);
			setPreferredSize(size);

			addMouseListener(new ClickHandler());
		}

		/**
		 * Gets a list of all the positions where halos are currently being
		 * displayed. The halos indicate selected pieces and possible moves.
		 */
		private List<Position> getHalos() {
			List<Position> halos = new ArrayList<Position>();
			GamePiece selectedPiece = game.getSelectedPiece();
			if (selectedPiece != null) {
				halos.add(selectedPiece.getPosition());
				halos.addAll(selectedPiece.getLegalMoves());
			}
			return halos;
		}

		@Override public void paint(Graphics graphics) {
			if (game == null) {
				return;
			}

			Graphics2D g = (Graphics2D)graphics;
			g.setStroke(NORMAL_STROKE);

			final int width = getWidth();
			final int height = getHeight();
			final Board board = game.getBoard();
			final float cellWidth = width / (float)BOARD_SIZE;
			final float cellHeight = height / (float)BOARD_SIZE;
			final Font font = new Font(
					Font.SANS_SERIF, Font.BOLD, Math.round(
							min(cellHeight, cellWidth) * 0.54f));
			g.setFont(font);

			// Compute the dimensions of the letters F and G in this Font.
			final FontMetrics metrics = g.getFontMetrics();
			final Rectangle2D
			fSize = metrics.getStringBounds("F", g),
			gSize = metrics.getStringBounds("G", g);

			// Temporary storage used throughout this method.
			float x, y;

			// Paint the board. First paint the red and black checkered
			// background.
			for (int row = 0; row < BOARD_SIZE; row++) {
				for (int col = 0; col < BOARD_SIZE; col++) {
					g.setColor(((row + col) % 2 == 0)
							? BLACK_SQUARE_COLOR
									: RED_SQUARE_COLOR);
					g.fill(new Rectangle2D.Float(
							col * cellWidth,
							row * cellHeight,
							cellWidth,
							cellHeight));
				}
			}

			// If the student hasn't created a Board yet, don't display
			// any pieces.
			if (board == null) {
				return;
			}

			// Paint the pieces onto the board. First paint the fox.
			g.setColor(FOX_COLOR);
			Position foxPos = board.getFox().getPosition();
			g.fill(new Ellipse2D.Float(
					x = (foxPos.getColumn() + PIECE_PADDING) * cellWidth,
					y = (foxPos.getRow() + PIECE_PADDING) * cellHeight,
					(1 - PIECE_PADDING * 2) * cellWidth,
					(1 - PIECE_PADDING * 2) * cellHeight));
			g.setColor(TEXT_COLOR);
			g.drawString("F",
					x + cellWidth * 0.5f - (float)fSize.getWidth() * 0.75f,
					y + cellHeight * 0.5f + (float)fSize.getHeight() * 0.25f);

			// Now paint the geese.
			for (GamePiece goose : board.getGeese()) {
				g.setColor(GOOSE_COLOR);
				Position goosePos = goose.getPosition();
				g.fill(new Ellipse2D.Float(
						x = (goosePos.getColumn() + PIECE_PADDING) * cellWidth,
						y = (goosePos.getRow() + PIECE_PADDING) * cellHeight,
						(1 - PIECE_PADDING * 2) * cellWidth,
						(1 - PIECE_PADDING * 2) * cellHeight));
				g.setColor(TEXT_COLOR);
				g.drawString("G",
						x + cellWidth * 0.5f - (float)gSize.getWidth() * 0.625f,
						y + cellHeight * 0.5f + (float)gSize.getHeight() * 0.25f);
			}

			if (!game.hasFoxLost() && !game.haveGeeseLost()) {
				// The game is not over, so paint focus halos in the
				// appropriate cells.
				g.setColor(HALO_COLOR);
				g.setStroke(new BasicStroke(
						max(MIN_HALO_WIDTH,
								HALO_WIDTH * max(cellWidth, cellHeight))));
				for (Position halo : getHalos()) {
					g.draw(new Ellipse2D.Float(
							(halo.getColumn() + HALO_WIDTH/2) * cellWidth - 1,
							(halo.getRow() + HALO_WIDTH/2) * cellHeight - 1,
							(1 - HALO_WIDTH) * cellWidth,
							(1 - HALO_WIDTH) * cellHeight));
				}
			} else { // The game is over.
				// Print an end-of-game message over a faded background.
				String message = game.hasFoxLost() ? "GEESE WON" : "FOX WON";
				g.setColor(FADE_COLOR);
				g.fill(new Rectangle2D.Float(0, 0, width, height));
				g.setColor(TEXT_COLOR);
				g.drawString(
						message,
						width * 0.3f,
						height * 0.47f);
			}
		}

		/** Handles click events on this panel. */
		private class ClickHandler extends MouseAdapter {
			@Override public void mouseClicked(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON1) {
					return; // Filter out non-left-clicks.
				}

				final float cellWidth = getWidth() / (float)BOARD_SIZE;
				final float cellHeight = getHeight() / (float)BOARD_SIZE;

				// Determine which row and column were clicked.
				final int row = (int)(e.getY() / cellHeight);
				final int col = (int)(e.getX() / cellWidth);

				if (LOG_CLICKS) {
					System.out.printf("{%d, %d},\n", row, col);
				}

				// Forward the click to the student's code.
				game.handleBoardClick(new Position(row, col));
			}
		}
	}

	// Grading hooks. Students: you should pretend these don't exist.

	/**
	 * List of all instances that have been created to date.
	 * <p><b>Student code should not use this field.</p>
	 */
	static final List<GameWindow> INSTANCES =
		new ArrayList<GameWindow>();

	/**
	 * Flag to enable/disable graphics.
	 * <p><b>Student code should not use this field.</p>
	 */
	static boolean ENABLED = true;

	/** Flag to enable logging of all click events to the console. */
	static boolean LOG_CLICKS = false;
}
