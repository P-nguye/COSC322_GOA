package ubc.cosc322.engine.core;

/** A class to help with math related to positions on the board. */
public final class Dimensions {

	/** The total numbers spaces on the board. */
	public final int boardSize;
	
	/** The dimensions of the board. */
	public final int boardWidth, boardHeight;

	/** The dimensions of the array that stores the board. */
	public final int arrayWidth, arrayHeight;

	/** The total size of the array that stores the board. */
	public final int arraySize;

	/** A mask that can anded with a index to get the x coordinate. */
	public final int arrayWidthMask;

	/** The number of bits to shift to the u coordinate from an index. */
	public final int arrayHeightShift;

	/** The maximum number of positions that can be reached orthogonally and diagonally from a position */
	public final int maxTrace;

	/** Array of direction offsets for. */
	private final int[] directionOffsets;


	/** Creates a dimensions object with given axis. */
	public Dimensions(int boardWidth, int boardHeight) {
		this.boardSize = boardWidth * boardHeight;
		this.boardWidth = boardWidth;
		this.boardHeight = boardHeight;
		this.maxTrace = boardHeight + boardWidth + 2 * Math.min(boardHeight, boardWidth) - 4;
		this.arrayHeight = boardHeight;
		int arrayWidth = 1;
		int arrayHeightShift = 0;
		while (boardWidth != 0) {
			boardWidth = boardWidth >> 1;
			arrayWidth = arrayWidth << 1;
			arrayHeightShift += 1;
		}
		this.arrayWidth = arrayWidth;
		this.arraySize = arrayWidth * arrayHeight;
		this.arrayHeightShift = arrayHeightShift;
		this.arrayWidthMask = arrayWidth - 1;
		Direction[] directions = Direction.values();
		this.directionOffsets = new int[directions.length];
		for (int i = 0; i < directions.length; i++) {
			directionOffsets[i] = (
				directions[i].x * 1 +
				directions[i].y * arrayWidth
			);
		}
	}

	/** Converts an integer position to an x coordinate. */
	public int x(int position) {
		return position & arrayWidthMask;
	}

	/** Converts an integer position to a y coordinate. */
	public int y(int position) {
		return position >> arrayHeightShift;
	}

	/** Converts x and y coordinates to an integer position. */
	public int position(int x, int y) {
		return x + y * arrayWidth;
	}

	/** Determines whether an integer position is within the board. */
	public boolean outOfBounds(int position) {
		return position < 0 || position >= arraySize || x(position) >= boardWidth;
	}

	/** Gets one of the 8 offsets than can be added to an integer position. */
	public int getDirectionOffset(int index) {
		return directionOffsets[index];
	}

}
