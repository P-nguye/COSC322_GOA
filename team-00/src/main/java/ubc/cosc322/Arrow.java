package ubc.cosc322;

import java.util.ArrayList;

public class Arrow extends Tile implements Cloneable {
	protected Arrow clone() {
        Arrow aNew = new Arrow(row, col);
        return aNew;
    }
	
	public Arrow(int i, int j) {
		super(i, j);
	}

	public int getColPosition() {
        return super.col;
    }

    public int getRowPosition() {
        return super.row;
    }

    public ArrayList<Integer> combinedMove(int row, int col) {
        ArrayList<Integer> move = new ArrayList<Integer>(2);
        move.add(row);
        move.add(col);
        return move;
    }

}
