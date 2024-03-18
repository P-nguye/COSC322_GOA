package ubc.cosc322;
import ubc.cosc322.engine.core.Board;
import ubc.cosc322.engine.core.Turn;
import ubc.cosc322.engine.util.IntList;

public class COSC322Validator {
	
	public static boolean validateAndLog(Board board, Turn turn) {
		boolean result = validate(board, turn);
		if (result) {
			System.out.println("RECEIVED MOVE IS VALID");
		} else {
			System.out.println("RECEIVED MOVE IS NOT VALID");
		}
		return result;
	}
	//This method validate whether a square is valid for a queen to move in or not
	public static boolean validate(Board board, Turn turn) {
		board = board.clone();
		IntList moves = new IntList(board.getMaxMovesAbsolute());
		board.generateMoves(moves);
		if (!moves.contains(turn.queenMove)&& !moves.contains(turn.arrowMove)) {
			return false;
		}
		
		return true;
	}

}
