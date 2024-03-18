package ubc.cosc322.engine.heuristics;

import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.util.IntList;
import ubc.cosc322.engine.core.Board;

/**
 * A fast heuristic based of the mobility of a color's queens.
 * While fast, this heuristic is pretty terrible.
 **/
public class MobilityHeuristic implements Heuristic {

	IntList traceBuffer;

	public MobilityHeuristic() {
		this.traceBuffer = new IntList(35);
	}

	@Override
	public int evaluate(Board board) {
		int heuristic = 0;
		// compute for both white and black
		for (Color color : Color.values()) {
			// count the positions that can be reached by queens of that color
			int mobility = 0;
			IntList queens = board.getQueens(color);
			for (int i = 0; i < queens.size(); i++) {
				board.trace(queens.get(i), traceBuffer);
				mobility += traceBuffer.size();
				traceBuffer.clear();
			}
			// white mobility makes heuristic positive, black makes it negative
			if (color == Color.WHITE) {
				heuristic += mobility;
			} else if (color == Color.BLACK) {
				heuristic -= mobility;
			}
		}
		return heuristic;
	}
	
}
