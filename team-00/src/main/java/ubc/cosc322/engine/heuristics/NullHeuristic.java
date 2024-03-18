package ubc.cosc322.engine.heuristics;

import ubc.cosc322.engine.core.Board;

/** A test heuristic that thinks every position is a draw. */
public class NullHeuristic implements Heuristic {

	@Override
	public int evaluate(Board board) {
		return 0;
	}

}
