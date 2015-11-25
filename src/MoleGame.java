import java.util.Random;

public class MoleGame {

	int moveCount;
	Player player;
	FSM model;
	
	
	public MoleGame(){
		moveCount = 0;
		player = new Player();
		model = new FSM();
	}
	
	public int getMove(){
		Random r = new Random();
		int Low = 1;
		int High = 100;
		return r.nextInt(High-Low) + Low;
	}
	
	public boolean finished(){
		//TODO ending condition
		if (moveCount < 20)
			return false;
		return true;
	}
	
	public void startGame(){
		while(!finished()){
			int move = getMove();
			int clue = getClueFromMove(move);
			int guess = player.makeGuess(clue, null);
			System.out.println("Clue: " + clue + ", guess " + guess + ", mole is " + move);
			moveCount++;
		}
	}

	private int getClueFromMove(int move) {
		// TODO provide a better function
		return move + 2;
	}
}
