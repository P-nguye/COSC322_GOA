package ubc.cosc322.engine.test;

import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.Dimensions;
import ubc.cosc322.engine.generators.ContestedMoveGenerator;
import ubc.cosc322.engine.generators.LegalMoveGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import ubc.cosc322.engine.core.Board;
import ubc.cosc322.engine.heuristics.DividerHeuristic;
import ubc.cosc322.engine.heuristics.EndgameHeuristic;
import ubc.cosc322.engine.heuristics.HybridRolloutHeuristic;
import ubc.cosc322.engine.heuristics.RolloutHeuristic;
import ubc.cosc322.engine.heuristics.SwitchHeuristic;
import ubc.cosc322.engine.players.MonteCarloPlayer;
import ubc.cosc322.engine.players.RandomPlayer;
import ubc.cosc322.engine.util.IntList;

/** Used for offline local testing. */
public class LocalTest {
	
	public static void main(String[] args) {

		final int ITERATION_COUNT = 1;

		Board initialBoard = new Board(); // standard 10x10 4 queen board
		// Board initialBoard = loadBoardFromFile(new Dimensions(10, 10), Color.BLACK, "/Users/luctowers/Documents/ubco/cosc322/team-01/scratch/game.txt");

		MonteCarloPlayer white = new MonteCarloPlayer(() -> new RolloutHeuristic(new RandomPlayer(new LegalMoveGenerator())), () -> new LegalMoveGenerator(), 8, 15000, 0.3);
		MonteCarloPlayer black = new MonteCarloPlayer(
			() -> new SwitchHeuristic(
				50,
				new RolloutHeuristic(new RandomPlayer(new LegalMoveGenerator())),
				new HybridRolloutHeuristic(new RandomPlayer(new ContestedMoveGenerator()))
			),
			() -> new LegalMoveGenerator(),
			8, 15000, 0.3
		);

		EndgameHeuristic endgame = new EndgameHeuristic();
		DividerHeuristic divider = new DividerHeuristic();

		try (HeadToHeadAnalyzer analyzer = new HeadToHeadAnalyzer(initialBoard, white, black)) {

			analyzer.onTurn(board -> {
				System.out.println(board);
				System.out.println(white.getStats().whiteWinRatio);
				System.out.println(white.getStats().evaluations);
				System.out.println(black.getStats().whiteWinRatio);
				System.out.println(black.getStats().evaluations);
				board.computeChambers();
				System.out.print("WHITE:");
				IntList whiteChambers = board.getWhiteChambers();
				for (int i = 0; i < whiteChambers.size(); i++) {
					int chamber = whiteChambers.get(i);
					int chamberSize = board.getChamberSize(chamber);
					System.out.print(" " + chamber + "@" + chamberSize);
				}
				System.out.println();
				System.out.print("BLACK:");
				IntList blackChambers = board.getBlackChambers();
				for (int i = 0; i < blackChambers.size(); i++) {
					int chamber = blackChambers.get(i);
					int chamberSize = board.getChamberSize(chamber);
					System.out.print(" " + chamber + "@" + chamberSize);
				}
				System.out.println();
				System.out.println("endgame=" + endgame.evaluate(board));
				System.out.println("divider=" + divider.evaluate(board));
			});
			System.out.println("iteration count: " + ITERATION_COUNT);

			long startTime = System.currentTimeMillis();
			analyzer.play(ITERATION_COUNT);
			long stopTime = System.currentTimeMillis();
			long elapsed = stopTime - startTime;
			System.out.println("execution time: " + elapsed + " ms");

			System.out.println("white win-rate: " + analyzer.getWinRate(Color.WHITE));
			System.out.println("black win-rate: " + analyzer.getWinRate(Color.BLACK));
			System.out.println("white win-count: " + analyzer.getWinCount(Color.WHITE));
			System.out.println("black win-count: " + analyzer.getWinCount(Color.BLACK));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static Board loadBoardFromFile(Dimensions dimensions, Color color, String filepath) {
		try {
			return new Board(dimensions, color, new String(Files.readAllBytes(Paths.get(filepath))));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
