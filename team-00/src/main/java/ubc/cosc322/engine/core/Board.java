package ubc.cosc322.engine.core;

import ubc.cosc322.engine.analysis.ChamberAnalyzer;
import ubc.cosc322.engine.util.ConsoleColors;
import ubc.cosc322.engine.util.IntList;

/** A mutable board state for the game of amazons. */
public class Board {

	public static final int MAX_QUEENS_PER_COLOR = 4;

	/** The dimensions of the board. Public because Dimensions is immutable. */
	public final Dimensions dimensions;

	// stores the state of each board position
	// each byte corresponds to a piece type, see Piece.java
	// the dimensions of this array consistent with the Dimensions object.
	private byte[] board;

	// whether white or black is expected to move next
	private Color colorToMove;

	// whether and queen or arrow move is expected
	private MoveType nextMoveType;

	// note: 2 moves occur per turn
	private int moveCount;

	// shortcuts to the positions of queens
	// this is a performance optimization
	private IntList blackQueens;
	private IntList whiteQueens;

	// optional chamber analyzer variables
	// they are very useful, but not always needed
	// chambersUpdated flag allows it to be lazily updated
	private ChamberAnalyzer chamberAnalyzer;
	private boolean chambersUpdated;
	private IntList blackChambers;
	private IntList whiteChambers;
	private IntList allChambers;

	// stores the positions related to the most recent moves
	// undoing moves is not required so a stack isn't needed
	private int lastQueenSource, lastQueenDestination, lastArrowMove;

	/** Creates a blank board, no arrows, no queen. */
	public Board(Dimensions dimensions) {
		this.dimensions = dimensions;
		this.board = new byte[dimensions.arraySize];
		this.colorToMove = Color.BLACK;
		this.nextMoveType = MoveType.QUEEN;
		this.moveCount = 0;
		this.whiteQueens = new IntList(MAX_QUEENS_PER_COLOR);
		this.blackQueens = new IntList(MAX_QUEENS_PER_COLOR);
		this.lastQueenSource = -1;
		this.lastQueenDestination = -1;
		this.lastArrowMove = -1;
		this.chamberAnalyzer = new ChamberAnalyzer(dimensions);
	}

	/** Creates a standard game of amazons board. */
	public Board() {
		this(new Dimensions(10, 10));
		placeQueen(Color.WHITE, dimensions.position(0, 3));
		placeQueen(Color.WHITE, dimensions.position(3, 0));
		placeQueen(Color.WHITE, dimensions.position(6, 0));
		placeQueen(Color.WHITE, dimensions.position(9, 3));
		placeQueen(Color.BLACK, dimensions.position(0, 6));
		placeQueen(Color.BLACK, dimensions.position(3, 9));
		placeQueen(Color.BLACK, dimensions.position(6, 9));
		placeQueen(Color.BLACK, dimensions.position(9, 6));
	}

	/** Copy contructor. */
	public Board(Board other) {
		this.dimensions = other.dimensions;
		this.colorToMove = other.colorToMove;
		this.nextMoveType = other.nextMoveType;
		this.lastQueenSource = other.lastQueenSource;
		this.lastQueenDestination = other.lastQueenDestination;
		this.lastArrowMove = other.lastArrowMove;
		this.moveCount = other.moveCount;
		this.board = other.board.clone();
		this.chamberAnalyzer = other.chamberAnalyzer.clone();
		this.whiteQueens = other.whiteQueens.clone();
		this.blackQueens = other.blackQueens.clone();
	}

	/** Places a new queen of color at position. */
	public void placeQueen(Color color, int position) {
		placePiece(Queen.queenOfColor(color), position);
		getQueens(color).push(position);
	}

	/** Places a new arrow at position. */
	public void placeArrow(int position) {
		chamberAnalyzer.placeArrow(position);
		// chamber analyzer needs to be recomputed, mark it as such
		chambersUpdated = false;
		placePiece(Queen.ARROW, position);
	}

	private void placePiece(byte piece, int position) {
		 //DEBUG ASSERTION
		 if (dimensions.outOfBounds(position)) {
		 	throw new IllegalArgumentException("invalid board position");
		 }
		 if (board[position] != Queen.NONE) {
		 	throw new IllegalArgumentException("position is not empty");
		 }
		board[position] = piece;
	}

	/** Modify board state with move, move type in assumed. */
	public void doMove(int move) {
		doMove(nextMoveType, move);
	}

	/** Modify board state with move, move type in explicit. */
	public void doMove(MoveType type, int move) {
		moveCount++;
		switch (type) {
			case QUEEN:
				int queenSource = Move.decodeQueenSource(move);
				int queenDestination = Move.decodeQueenDestination(move);
				updateQueenPosition(queenSource, queenDestination);
				lastQueenSource = queenSource;
				lastQueenDestination = queenDestination;
				break;
			case ARROW:
				placeArrow(move);
				lastArrowMove = move;
				colorToMove = colorToMove.opposite();
				break;
			default:
				throw new IllegalArgumentException("Illegal move type");
		}
		nextMoveType = type.next();
	}

	/** Modify board state with turn, this can change the next move type. */
	public void doTurn(Turn turn) {
		doMove(MoveType.QUEEN, turn.queenMove);
		doMove(MoveType.ARROW, turn.arrowMove);
	}

	// updates a queens position, in the board and all helper structs
	private void updateQueenPosition(int oldPosition, int newPosition) {
		byte queen = board[oldPosition];
		Color color = Queen.colorOfQueen(queen);
		if (color == null) {
			return;
		}
		IntList positions = getQueens(color);
		int index = positions.search(oldPosition);
		if (index != -1) {
			positions.set(index, newPosition);
		}
		colorToMove = color;
		board[oldPosition] = Queen.NONE;
		board[newPosition] = queen;
	}

	/** Returns queen position for a given color. */
	public IntList getQueens(Color color) {
		// TODO: make these returns immutable
		switch (color) {
			case WHITE:
				return whiteQueens;
			case BLACK:
				return blackQueens;
			default:
				throw new IllegalArgumentException("invalid color");
		}
	}

	/** Number of moves/half-turns since the Board object was created. */
	public int getMoveCount() {
		return moveCount;
	}

	/** Get whether white or black is expected to move next. */
	public Color getColorToMove() {
		return colorToMove;
	}

	/** Get whether and queen or arrow move is expected. */
	public MoveType getNextMoveType() {
		return nextMoveType;
	}

	/** Outputs positions that can be reached orthogonally or diagonally from a
	 *  given position. */
	public void trace(int source, IntList output) {
		for (int d = 0; d < Direction.COUNT; d++) {
			int offset = dimensions.getDirectionOffset(d);
			int position = source + offset;
			while (!dimensions.outOfBounds(position) && board[position] == Queen.NONE) {
				output.push(position);
				position += offset;
			}
		}
	}

	// used to check whether queen is completely surrounded
	private boolean surroundedByArrows(int position) {
		for (int d = 0; d < Direction.COUNT; d++) {
			int offset = dimensions.getDirectionOffset(d);
			int adjacent = position + offset;
			if (!dimensions.outOfBounds(adjacent)) {
				byte piece = board[adjacent];
				if (piece != Queen.ARROW) {
					return false;
				}
			}
		}
		return true;
	}

	/** Outputs all legal moves, assumes move type. */
	public void generateMoves(IntList output) {
		switch (nextMoveType) {
			case QUEEN:
				generateQueenMoves(output);
				break;
			case ARROW:
				generateArrowMoves(lastQueenDestination, output);
				break;
			default:
				throw new IllegalStateException("illegal move type");
		}
	}

	/** Outputs all legal moves, assumes move type. */
	public void generateQueenMoves(IntList output) {
		IntList queensToMove = getQueens(colorToMove);
		for (int q = queensToMove.size() - 1; q >= 0; q--) {
			int outputBase = output.size();
			int queen = queensToMove.get(q);
			trace(queen, output);
			if (output.size() == outputBase && surroundedByArrows(queen)) {
				queensToMove.removeIndex(q);
			}
			for (int i = outputBase; i < output.size(); i++) {
				output.set(i, Move.encodeQueenMove(queen, output.get(i)));
			}
		}
	}

	/** Outputs all legal moves, assumes move type. */
	public void generateQueenMoves(int position, IntList output) {
		int outputBase = output.size();
		trace(position, output);
		if (output.size() == outputBase && surroundedByArrows(position)) {
			Color color = Queen.colorOfQueen(board[position]);
			getQueens(color).removeValue(position);
		}
		for (int i = outputBase; i < output.size(); i++) {
			output.set(i, Move.encodeQueenMove(position, output.get(i)));
		}
	}

	/** Outputs all legal moves, assumes move type. */
	private void generateArrowMoves(int position, IntList output) {
		trace(position, output);
	}

	/** Gets a piece in at a board position. Use dimensions.position(x,y).
	 *  Use Piece static values to check for piece type. */
	public byte getPiece(int position) {
		return board[position];
	}

	/** Gets the max number of moves that could be return by generateMoves.
	 *  Move type is assumed. */
	public int getMaxMoves() {
		switch (nextMoveType) {
			case QUEEN:
				return MAX_QUEENS_PER_COLOR*dimensions.maxTrace;
			case ARROW:
				return dimensions.maxTrace;
			default:
				throw new IllegalStateException("illegal move type");
		}
	}

	/** Gets the max number of moves that could be return by generateMoves. */
	public int getMaxMovesAbsolute() {
		return MAX_QUEENS_PER_COLOR*dimensions.maxTrace;
	}

	/** Updates the state of all chamber related variables, if needed. */
	public void computeChambers() {
		if (chambersUpdated) {
			return;
		}
		// these variables are null when the board is constructed.
		if (whiteChambers == null) {
			whiteChambers = new IntList(MAX_QUEENS_PER_COLOR);
		} else {
			whiteChambers.clear();
		}
		if (blackChambers == null) {
			blackChambers = new IntList(MAX_QUEENS_PER_COLOR);
		} else {
			blackChambers.clear();
		}
		if (allChambers == null) {
			allChambers = new IntList(2*MAX_QUEENS_PER_COLOR);
		} else {
			allChambers.clear();
		}
		// let chamber analyzer compute
		chamberAnalyzer.update();
		// create some useful helpers about queen chambers
		for (int i = 0; i < whiteQueens.size(); i++) {
			int queen = whiteQueens.get(i);
			int chamber = chamberAnalyzer.getPositionChamber(queen);
			whiteChambers.push(chamber);
			allChambers.push(chamber);
		}
		for (int i = 0; i < blackQueens.size(); i++) {
			int queen = blackQueens.get(i);
			int chamber = chamberAnalyzer.getPositionChamber(queen);
			blackChambers.push(chamber);
			allChambers.push(chamber);
		}
		allChambers.sort();
		// mark as updated so this doesn't need to redone
		chambersUpdated = true;
	}

	/** Gets all the chambers that white queens reside in. */
	public IntList getWhiteChambers() {
		// TODO: make this return immutable
		return whiteChambers;
	}

	/** Gets all the chambers that black queens reside in. */
	public IntList getBlackChambers() {
		// TODO: make this return immutable
		return blackChambers;
	}

	/** Gets all the chambers that queens of a color queens reside in. */
	public IntList getQueenChambers(Color color) {
		// TODO: make this return immutable
		switch (color) {
			case WHITE:
				return whiteChambers;
			case BLACK:
				return blackChambers;
			default:
				throw new IllegalArgumentException("invalid color");
		}
	}

	/** Gets all the chambers that queens reside in. */
	public IntList getQueenChambers() {
		return allChambers;
	}

	/** Gets the size of a chamber in board spaces. */
	public int getChamberSize(int chamber) {
		return chamberAnalyzer.getChamberSize(chamber);
	}

	/** copy the board state. */
	@Override
	public Board clone()
    {
		return new Board(this);
    }

	/** Convert the board to a multine line human-readable format. */
	@Override
	public String toString() { 
		StringBuilder builder = new StringBuilder();
		String line = horiztonalLine();
		int highlightedBlank = -1;
		int highlightedQueen = -1;
		int highlightedArrow = -1;
		switch (nextMoveType) {
			case QUEEN:
				highlightedArrow = lastArrowMove;
			case ARROW:
				highlightedBlank = lastQueenSource;
				highlightedQueen = lastQueenDestination;
		}
		builder.append(ConsoleColors.GREEN);
		builder.append(line);
		builder.append('\n');
		for (int y = dimensions.boardHeight-1; y >= 0; y--) {
			builder.append("| ");
			for (int x = 0; x < dimensions.boardWidth; x++) {
				int position = dimensions.position(x, y);
				byte piece = board[position];
				if (piece == Queen.NONE) {
					if (position == highlightedBlank) {
						builder.append(ConsoleColors.YELLOW);
						builder.append('!');
						builder.append(ConsoleColors.GREEN);
					} else {
						builder.append(' ');
					}
				} else if (piece == Queen.ARROW) {
					if (position == highlightedArrow) {
						builder.append(ConsoleColors.YELLOW);
						builder.append('*');
						builder.append(ConsoleColors.GREEN);
					} else {
						builder.append('*');
					}
				} else if (piece == Queen.WHITE_QUEEN) {
					if (position == highlightedQueen) {
						builder.append(ConsoleColors.YELLOW);
						builder.append('W');
						builder.append(ConsoleColors.GREEN);
					} else {
						builder.append(ConsoleColors.RESET);
						builder.append('W');
						builder.append(ConsoleColors.GREEN);
					}
				} else if (piece == Queen.BLACK_QUEEN) {
					if (position == highlightedQueen) {
						builder.append(ConsoleColors.YELLOW);
						builder.append('B');
						builder.append(ConsoleColors.GREEN);
					} else {
						builder.append(ConsoleColors.RESET);
						builder.append('B');
						builder.append(ConsoleColors.GREEN);
					}
				} else {
					builder.append('?');
				}
				builder.append(" | ");
			}
			builder.append('\n');
			builder.append(line);
			builder.append('\n');
		}
		builder.append(ConsoleColors.RESET);
		builder.append(colorToMove.toString() + " to make " + nextMoveType.toString() + " move");
		return builder.toString();
	}

	// helper function for board toString
	private String horiztonalLine() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 4*dimensions.boardWidth+1; i++) {
			if (i % 4 == 0) {
				builder.append('+');
			} else {
				builder.append('-');
			}
		}
		return builder.toString();
	}

	/** A hacky way to load strings as boards for testing. */
	public Board(Dimensions dimensions, Color colorToMove, String boardString) {
		this(dimensions);
		this.colorToMove = colorToMove;
		boolean nextCharCouldBePiece = false;
		int x = 0;
		int y = dimensions.boardHeight - 1;
		for (int i = 0; i < boardString.length(); i++){
			char character = boardString.charAt(i);
			if (nextCharCouldBePiece) {
				nextCharCouldBePiece = false;
				if (character == 'W') {
					placeQueen(Color.WHITE, dimensions.position(x, y));
				} else if (character == 'B') {
					placeQueen(Color.BLACK, dimensions.position(x, y));
				} else if (character == '*') {
					placeArrow(dimensions.position(x, y));
				} else if (character != ' ') {
					x--;
				}
				x++;
			} else if (character == ' ') {
				nextCharCouldBePiece = true;
			}
			if (character == '\n') {
				if (x != 0) {
					y--;
				}
				x = 0;
			}
		}
	}

}
