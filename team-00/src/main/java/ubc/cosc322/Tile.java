package ubc.cosc322;

/**
 * A parent class for Queen, and Arrow. Used to store their locations on the board
 */
public class Tile { 

	protected int row;
    protected int col;
  
  public Tile(int i, int j) { 
	row = i;
    col = j;
  }

}