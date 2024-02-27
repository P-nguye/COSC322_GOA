

package ubc.cosc322;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JPanel;

import sfs2x.client.entities.Room;
import ygraph.ai.smartfox.games.BaseGameGUI;
import ygraph.ai.smartfox.games.BoardGameModel;
import ygraph.ai.smartfox.games.GameClient;
import ygraph.ai.smartfox.games.GameMessage;
import ygraph.ai.smartfox.games.GamePlayer;
import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;

/**
 * An example illustrating how to implement a GamePlayer
 * @author Yong Gao (yong.gao@ubc.ca)
 * Jan 5, 2021
 *
 */
public class COSC322Test extends GamePlayer{

	private boolean isPlayerA = false;

    private GameClient gameClient = null; 
    private BaseGameGUI gamegui = null;
	
    private String userName = null;
    private String passwd = null;
 
    private SearchTree search;
    private JFrame guiFrame = null; 
    private GameBoard board = null; 
    private boolean gameStarted = false;
    private GameRules ourBoard = null;
    int turnCount = 0;
    String ourPlayer = "";
    String enemyPlayer = "";
    
	
    /**
     * The main method
     * @param args for name and passwd (current, any string would work)
     */
    public static void main(String[] args) {				 
    	COSC322Test player = new COSC322Test(args[0], args[1]);
    	
    	if(player.getGameGUI() == null) {
    		player.Go();
    	}
    	else {
    		BaseGameGUI.sys_setup();
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                	player.Go();
                }
            });
    	}
    }
	
    /**
     * Any name and passwd 
     * @param userName
      * @param passwd
     */
    public COSC322Test(String userName, String passwd) {
    	this.userName = userName;
    	this.passwd = passwd;
    	
    	//To make a GUI-based player, create an instance of BaseGameGUI
    	//and implement the method getGameGUI() accordingly
    	this.gamegui = new BaseGameGUI(this);
    }
 


    @Override
    public void onLogin() {
    	System.out.println("Congratualations!!! "
    			+ "I am called because the server indicated that the login is successfully");
    	System.out.println("The next step is to find a room and join it: "
    			+ "the gameClient instance created in my constructor knows how!");     	userName = gameClient.getUserName();
    	if(gamegui!= null) {
    		gamegui.setRoomInformation(gameClient.getRoomList());
    	}
    	
    }
    @Override
    public boolean handleGameMessage(String messageType, Map<String, Object> msgDetails) {
    	//This method will be called by the GameClient when it receives a game-related message
    	//from the server.
	
    	//For a detailed description of the message types and format, 
    	//see the method GamePlayer.handleGameMessage() in the game-client-api document. 
    	
    	/*System.out.println("Message from the server:");
    	System.out.println("Message type: "+ messageType);
    	System.out.println("Message detail: "+ msgDetails);*/
    	
    	if (messageType.equals(GameMessage.GAME_STATE_BOARD)) {
            // Handle GAME_STATE_BOARD message
            // Call BaseGameGui.setGameState(...) to set the game board
    		
            gamegui.setGameState((ArrayList<Integer>)msgDetails.get("game-state"));
            
            if(messageType.equals(GameMessage.GAME_ACTION_START)){
    			if(((String) msgDetails.get("player-white")).equals(this.userName())){
    				 gamegui.setGameState((ArrayList<Integer>)msgDetails.get("game-state"));
    				System.out.println("Game State: " +  msgDetails.get("player-white"));
    				ourPlayer = "White Player: " + this.userName();
    				enemyPlayer = "Black Player: " + msgDetails.get("player-black");
                    turnCount++;
                    guiFrame.setTitle("Turn: " + turnCount + " | Move: " + userName() + " | " + ourPlayer + " | " +enemyPlayer);
                    ourBoard = new GameRules(true);
                    System.out.println("Initial Board");
                    ourBoard.printBoard();
    				ourBoard.canEnemyMove();
    				ourBoard.updateLegalQueenMoves();
                    search = new SearchTree(new SearchTreeNode(ourBoard));
                    SearchTreeNode ourBestMove = search.makeMove();
                    Queen ourMove = ourBestMove.getQueen();
                    Arrow ourArrow = ourBestMove.getArrowShot();
                    ourBoard.canEnemyMove();
                    ourBoard.updateLegalQueenMoves();
    				System.out.println("\nOur Move: [" + translateRow(ourMove.row) + ", " + translateCol(ourMove.col) + "]");
    				System.out.println("Our Arrow Shot: [" + translateRow(ourArrow.row) + ", " + translateCol(ourArrow.col) + "]\n");
    				board.markPosition(translateRow(ourMove.row), translateCol(ourMove.col), translateRow(ourArrow.getRowPosition()), translateCol(ourArrow.getColPosition()),
    						translateRow(ourMove.previousRow), translateCol(ourMove.previousCol), false);

                    gameClient.sendMoveMessage(ourMove.combinedMove(translateRow(ourMove.previousRow), translateCol(ourMove.previousCol)),
                            ourMove.combinedMove(translateRow(ourMove.row), translateCol(ourMove.col)),
                            ourArrow.combinedMove(translateRow(ourArrow.getRowPosition()), translateCol(ourArrow.getColPosition())));
                    ourBoard.printBoard();

                }
    			
            }else {
                    ourPlayer = "Black Player: " + this.userName();
                    enemyPlayer = "White Player: " + msgDetails.get("player-white");
                    ourBoard = new GameRules(false);
                    search = new SearchTree(new SearchTreeNode(ourBoard));

                }
        } else if (messageType.equals(GameMessage.GAME_ACTION_MOVE)) {
            // Handle GAME_ACTION_MOVE message
            // Call BaseGameGui.updateGameState(...) to update the game board
            gamegui.updateGameState(msgDetails);

            ArrayList<Integer> queenCur= (ArrayList<Integer>)msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR);
            ArrayList<Integer> queenNext= (ArrayList<Integer>)msgDetails.get(AmazonsGameMessage.QUEEN_POS_NEXT);
            ArrayList<Integer> arrowPos= (ArrayList<Integer>)msgDetails.get(AmazonsGameMessage.ARROW_POS);

            try {
				handleOpponentMove(msgDetails);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
        }
		return true;
	}
    
    
    	private void handleOpponentMove(Map<String, Object> msgDetails) throws CloneNotSupportedException{
            boolean gameOver = false;
            turnCount++;
            guiFrame.setTitle("Turn: " + turnCount + " | Move: " + userName() + " | " + ourPlayer + " | " + enemyPlayer);
    		System.out.println("\nOpponentMove: " + msgDetails.get(AmazonsGameMessage.QUEEN_POS_NEXT));
            System.out.println("Opponent Arrow Shot: " + msgDetails.get(AmazonsGameMessage.ARROW_POS) + "\n");
    		ArrayList<Integer> qcurr = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR);
    		ArrayList<Integer> qnew = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_NEXT);
    		ArrayList<Integer> arrow = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.ARROW_POS);
            // Enemy move
    		Queen enemyQueen = new Queen(convertRow(qnew.get(0)), convertCol(qnew.get(1)), true);
    		enemyQueen.previousRow = convertRow(qcurr.get(0));
    		enemyQueen.previousCol = convertCol(qcurr.get(1));
    		Arrow enemyArrow = new Arrow(convertRow(arrow.get(0)), convertCol(arrow.get((1))));
    		search.makeMoveOnRoot(enemyQueen, enemyArrow);
            board.markPosition(qnew.get(0), qnew.get(1), arrow.get(0), arrow.get(1),
                    qcurr.get(0), qcurr.get(1), true);
            ourBoard.canEnemyMove();
    		ourBoard.updateLegalQueenMoves();
            ourBoard.printBoard();

            // Check if we're at a goal node
            gameOver = ourBoard.goalTest();

            if(gameOver) {
                System.out.println("\n THE GAME IS NOW OVER \n");
            }

            // Our move
            turnCount++;
            guiFrame.setTitle("Turn: " + turnCount + " | Move: " + userName() + " | " + ourPlayer + " | " + enemyPlayer);
            SearchTreeNode ourBestMove = search.makeMove();
            Queen ourMove = ourBestMove.getQueen();
            Arrow ourArrow = ourBestMove.getArrowShot();
            ourBoard.canEnemyMove();
            ourBoard.updateLegalQueenMoves();
            System.out.println("\nOur Move: [" + translateRow(ourMove.row) + ", " + translateCol(ourMove.col) + "]");
            board.markPosition(translateRow(ourMove.row), translateCol(ourMove.col), translateRow(ourArrow.getRowPosition()), translateCol(ourArrow.getColPosition()),
                    translateRow(ourMove.previousRow), translateCol(ourMove.previousCol), false);
    		System.out.println("Our Arrow Shot: [" + translateRow(ourArrow.row) + ", " + translateCol(ourArrow.col) + "]\n");
            gameClient.sendMoveMessage(ourMove.combinedMove(translateRow(ourMove.previousRow), translateCol(ourMove.previousCol)),
    				ourMove.combinedMove(translateRow(ourMove.row), translateCol(ourMove.col)),
    				ourArrow.combinedMove(translateRow(ourArrow.getRowPosition()), translateCol(ourArrow.getColPosition())));
            ourBoard.printBoard();
            gameOver = ourBoard.goalTest();

            if(gameOver) {
                System.out.println("\n THE GAME IS NOW OVER \n");
            }
    	}
        private int convertRow(int row){
            return Math.abs(row - 10);	// formula to convert server's row coordinate system to our Board's coordinate system
        }

        private int convertCol(int col){
            return (col - 1);	      // formula to convert server's column coordinate system to our Board's coordinate system
        }

        private int translateCol(int col){
            return (col + 1);	      // formula to translate our Board's column coordinate system to the server's coordinate system
        }

        private int translateRow(int row){
            return Math.abs(10 - row);	      // formula to convert our Board's row coordinate system to the server's coordinate system
        }


        /**
         * handle a move made by this player --- send the info to the server.
         * @param x queen row index 
         * @param y queen col index
         * @param arow arrow row index
         * @param acol arrow col index
         * @param qfr queen original row
         * @param qfc queen original col
         */
    	public void playerMove(int x, int y, int arow, int acol, int qfr, int qfc){		
    		 
    		ArrayList<Integer> qf = new ArrayList<Integer>(2);
    		qf.add(qfr);
    		qf.add(qfc);
    		
    		ArrayList<Integer> qn = new ArrayList<Integer>(2);
    		qn.add(x);
    		qn.add(y);
    		
    		ArrayList<Integer> ar = new ArrayList<Integer>(2);
    		ar.add(arow);
    		ar.add(acol);
    		
    		//To send a move message, call this method with the required data  
    		this.gameClient.sendMoveMessage(qf, qn, ar);
    		
    	}
    	
        
    	//set up the game board
    	private void setupGUI(){
    	    guiFrame = new JFrame();
    		   
    		guiFrame.setSize(800, 600);
            guiFrame.setTitle("Turn: " + turnCount + " | Move: | " + ourPlayer + " | " + enemyPlayer);
    		
    		guiFrame.setLocation(200, 200);
    		guiFrame.setVisible(true);
    	    guiFrame.repaint();		
    		guiFrame.setLayout(null);
    		
    		Container contentPane = guiFrame.getContentPane();
    		contentPane.setLayout(new  BorderLayout());
    		 
    		contentPane.add(Box.createVerticalGlue()); 
    		
    		board = createGameBoard();		
    		contentPane.add(board,  BorderLayout.CENTER);
    	}
        
    	private GameBoard createGameBoard(){
    		return new GameBoard(this);
    	}
    		
    	public boolean handleMessage(String msg) {
    		System.out.println("Time Out ------ " + msg); 
    		return true;
    	}
    public class GameBoard extends JPanel{
    		
    		private static final long serialVersionUID = 1L;
    		private  int rows = 10;
    		private  int cols = 10; 
    		
    		int width = 500;
    		int height = 500;
    		int cellDim = width / 10; 
    		int offset = width / 20;
    		
    		int posX = -1;
    		int posY = -1;
    	
    		int r = 0;
    		int c = 0;
    		  
    		
    		COSC322Test game = null; 
    	    private BoardGameModel gameModel = null;
    		

    		public GameBoard(COSC322Test game){
    	        this.game = game;
    	        gameModel = new BoardGameModel(this.rows + 1, this.cols + 1);

    	        //if(!game.isGamebot){
    	        	addMouseListener(new  GameEventHandler());
    	        //}
    	      
    		}
    		
    		
    		

    		
    		/**
    		 * repaint the part of the board
    		 * @param qrow queen row index
    		 * @param qcol queen col index 
    		 * @param arow arrow row index
             * @param acol arrow col index
             * @param qfr queen original row
             * @param qfc queen original col
    		 */
    		public boolean markPosition(int qrow, int qcol, int arow, int acol, 
    				  int qfr, int qfc, boolean  opponentMove){						

    			boolean valid = gameModel.positionMarked(qrow, qcol, arow, acol, qfr, qfc, opponentMove);
                if(valid) {
                    repaint();
                }
                else {
                    gameModel.positionMarked(qrow, qcol, arow, acol, qfr, qfc, opponentMove);
                }
                return valid;
    		}
    		
    		// JCmoponent method
    		/*protected void paintComponent(Graphics gg){
    			Graphics g = (Graphics2D) gg;
     			
    			for(int i = 0; i < rows + 1; i++){
    				g.drawLine(i * cellDim + offset, offset, i * cellDim + offset, rows * cellDim + offset);
    				g.drawLine(offset, i*cellDim + offset, cols * cellDim + offset, i*cellDim + offset);					 
    			}
    			
    			for(int r = 0; r < rows; r++){
    			  for(int c = 0; c < cols; c++){
    				
    					posX = c * cellDim + offset;
    					posY = r * cellDim + offset;
    					
    					posY = (9 - r) * cellDim + offset;
    					
    				if(gameModel.gameBoard[r + 1][c + 1].equalsIgnoreCase(BoardGameModel.POS_AVAILABLE)){
    					g.clearRect(posX + 1, posY + 1, 49, 49);
    				}

    				if(gameModel.gameBoard[r + 1][c + 1].equalsIgnoreCase(
    						  BoardGameModel.POS_MARKED_BLACK)){
    					g.fillOval(posX, posY, 50, 50);
    				}
    				else if(gameModel.gameBoard[r + 1][c + 1].equalsIgnoreCase(
    					  BoardGameModel.POS_MARKED_ARROW)) {
    					g.clearRect(posX + 1, posY + 1, 49, 49);
    					g.drawLine(posX, posY, posX + 50, posY + 50);
    					g.drawLine(posX, posY + 50, posX + 50, posY);
    				}
    				else if(gameModel.gameBoard[r + 1][c + 1].equalsIgnoreCase(BoardGameModel.POS_MARKED_WHITE)){
    					g.drawOval(posX, posY, 50, 50);
    				}
    			  }
    		}
    			
    		}//method*/
    		
    		//JComponent method
    		public Dimension getPreferredSize() {
    		        return new Dimension(600,800);
    		 }

    		/**
    		 * Handle mouse events
    		 * 
    		 * @author yongg
    		 */
    		public class GameEventHandler extends MouseAdapter{
    			 
    			    int counter = 0;
    			    
    			    int qrow = 0;
    			    int qcol = 0;
    			
    			    int qfr = 0;
    			    int qfc = 0;
    			    
    			    int arow = 0;
    			    int acol = 0; 
    			
    	            public void mousePressed(MouseEvent e) {
    	            	
    	            	if(!gameStarted){
    	            		//return; 
    	            	}
    	            	
                        int x = e.getX();
                        int y = e.getY();
    	            
                        
                        if(((x - offset) < -5) || ((y - offset) < -5)){
                        	return;
                        }
                        
                        int row = (y - offset) / cellDim + 1;
                        int col = (x - offset) / cellDim + 1;
                        
                        if(counter == 0){
                        	qfr = row;
                        	qfc = col;
                        	
                        	qfr = 11 - qfr;
                        	counter++;
                        }
                        else if(counter ==1){
                        	qrow = row;
                        	qcol = col;
                        	
                        	qrow = 11 - qrow;
                        	counter++;
                        }
                        else if (counter == 2){
                        	arow = row;
                        	acol = col;
                        	
                        	arow = 11 - arow;
                        	counter++;
                        }
                        
                        if(counter == 3){
                          counter = 0; 	
                          boolean validMove = markPosition(qrow, qcol, arow, acol, qfr, qfc, false); // update itself
                          if(validMove){
                        	game.playerMove(qrow, qcol, arow, acol, qfr, qfc); //to server
                          }
                          
                          qrow = 0;
                          qcol = 0;
                          arow = 0;
                          acol = 0;
                          
                        }
    	            }			 
    		 }//end of GameEventHandler		
    	
    	}//end of GameBoard  

        
        
        @Override
        public String userName() {
        	return userName;
        }

    	@Override
    	public GameClient getGameClient() {
    		// TODO Auto-generated method stub
    		return this.gameClient;
    	}

    	@Override
    	public BaseGameGUI getGameGUI() {
    		// TODO Auto-generated method stub
    		return  this.gamegui;
    	}

    	@Override
    	public void connect() {
    		// TODO Auto-generated method stub
        	gameClient = new GameClient(userName, passwd, this);			
    	}

     
    }//end of class