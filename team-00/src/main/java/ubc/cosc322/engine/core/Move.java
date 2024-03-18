package ubc.cosc322.engine.core;

/** 
 * Move are represented by integers, so this class is not constructable.
 * But it contains methods to decode/encode move information.
 **/
public class Move {

	private Move() {}

	/** Packs two positions into single int for the queen move. */
	public static int encodeQueenMove(int queenSource, int queenDestination) {
		return (queenDestination << 16) | queenSource;
	}

	/** Unpacks the queen source from a queen move. */
	public static int decodeQueenSource(int queenMove) {
		return queenMove & 0xFFFF;
	}

	/** Unpacks the queen destination from a queen move. */
	public static int decodeQueenDestination(int queenMove) {
		return queenMove >> 16;
	}

}
