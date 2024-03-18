package ubc.cosc322.engine.core;

/** 
 * Pieces are represented by bytes, so this class is not constructable.
 * But it contains utilities to work with these bytes more cleanly.
 **/
public class Queen {

	private Queen() {};

	public static final byte NONE = 0;
	public static final byte WHITE_QUEEN = 1;
	public static final byte BLACK_QUEEN = 2;
	public static final byte ARROW = 3;

	/** Gets the queen of a specific color. */
	public static byte queenOfColor(Color color) {
		switch (color) {
			case WHITE:
				return WHITE_QUEEN;
			case BLACK:
				return BLACK_QUEEN;
			default:
				return NONE;
		}
	}

	/** Gets the color of a queen piece. */
	public static Color colorOfQueen(byte queen) {
		switch (queen) {
			case WHITE_QUEEN:
				return Color.WHITE;
			case BLACK_QUEEN:
				return Color.BLACK;
			default:
				return null;
		}
	}

}
