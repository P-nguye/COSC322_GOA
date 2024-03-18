package ubc.cosc322.engine.generators;

import ubc.cosc322.engine.core.Board;
import ubc.cosc322.engine.util.IntList;

/** An interface to abstract move generation for positions in the Game of Amazons. */
public interface MoveGenerator {
	
	/** Returns an arbitrary subset of the legal moves in a given position. */
	public void generateMoves(Board board, IntList output);

}
