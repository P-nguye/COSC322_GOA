package ubc.cosc322.engine.generators;

import ubc.cosc322.engine.core.Board;
import ubc.cosc322.engine.util.IntList;

/** A move generator that returns all legal moves. */
public class LegalMoveGenerator implements MoveGenerator {

	@Override
	public void generateMoves(Board board, IntList output) {
		board.generateMoves(output);
	}
	
}
