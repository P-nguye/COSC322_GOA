package ubc.cosc322.engine.generators;

import ubc.cosc322.engine.core.Board;
import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.MoveType;
import ubc.cosc322.engine.util.IntList;

/**
 * An experimental move generator that moves queen in contested chambers
 * (chambers that have enemy queens) before it moves queens in uncontested
 * chambers.
 */
public class ContestedMoveGenerator implements MoveGenerator {

	private IntList contestedQueens;
	private IntList uncontestQueens;

	public ContestedMoveGenerator() {
		this.contestedQueens = new IntList(Board.MAX_QUEENS_PER_COLOR);
		this.uncontestQueens = new IntList(Board.MAX_QUEENS_PER_COLOR);
	}

	@Override
	public void generateMoves(Board board, IntList output) {

		int outputBase = output.size();

		if (board.getNextMoveType() != MoveType.QUEEN) {
			board.generateMoves(output);
			return;
		}

		contestedQueens.clear();
		uncontestQueens.clear();
		board.computeChambers();

		Color color = board.getColorToMove();
		IntList friendlyQueens = board.getQueens(color);
		IntList friendlyChambers = board.getQueenChambers(color);
		IntList enemyChambers = board.getQueenChambers(color.opposite());
		IntList allChambers = board.getQueenChambers();
		for (int i = 0; i < friendlyChambers.size(); i++) {
			int chamber = friendlyChambers.get(i);
			int size = board.getChamberSize(chamber);
			int queen = friendlyQueens.get(i);
			if (size > allChambers.count(chamber)) {
				if (enemyChambers.contains(chamber)) {
					contestedQueens.push(queen);
				} else {
					uncontestQueens.push(queen);
				}
			}
		}

		if (!contestedQueens.empty()) {

			for (int i = 0; i < contestedQueens.size(); i++) {
				int queen = contestedQueens.get(i);
				board.generateQueenMoves(queen, output);
			}

		}
		
		if (!uncontestQueens.empty() && outputBase == output.size()) {

			int smallestChamber = -1;
			int minSize = Integer.MAX_VALUE;
			for (int i = 0; i < friendlyChambers.size(); i++) {
				int queen = friendlyQueens.get(i);
				int chamber = friendlyChambers.get(i);
				int size = board.getChamberSize(chamber);
				if (size < minSize && uncontestQueens.contains(queen)) {
					smallestChamber = chamber;
					minSize = size;
				}
			}

			for (int i = friendlyQueens.size() - 1; i >= 0; i--) {
				int queen = friendlyQueens.get(i);
				int chamber = friendlyChambers.get(i);
				if (chamber == smallestChamber) {
					board.generateQueenMoves(queen, output);
				}
			}

		}

	}
	
}
