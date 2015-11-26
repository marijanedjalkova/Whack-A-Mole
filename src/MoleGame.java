import java.util.Random;

public class MoleGame {
	private final int GAME_LENGTH = 100;
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
		if (moveCount < GAME_LENGTH)
			return false;
		return true;
	}
	
	public void startGame(){
		int correctCount = 0;
		int acceptableCount = 0;
		while(!finished()){
			int move = getMove();
			int clue = getClueFromMove(move, model.currentState);
			int guess = player.makeGuess(clue, model.currentState);
			if (Math.abs(guess - move) < 2){
				acceptableCount++;
				if (guess == move){
					System.out.println("CORRECT!");
					correctCount++;
				} else {
					System.out.println("OK");
				}
			} else {
				System.out.println("====WRONG by " + (guess - move));
			}
			System.out.println("Mole: " + move + ", clue: " + clue + ", guess:  " + guess);
			model.updateState(move);
			moveCount++;
		}
		System.out.println("Guessed " + correctCount + " out of " + GAME_LENGTH);
		System.out.println("Acceptable " + acceptableCount + " out of " + GAME_LENGTH);
	}

	private int getClueFromMove(int move, State currentState) {
		int result;
		if (currentState == null){
			return move;
		}
		int stateValue = currentState.getValue();
		if (stateValue ==1){
			result = move + 5;
			if (result > 100)
				result = result - 100;
		}else{
			result = move - 5;
			if (result < 0)
				result =  100 + result;
		}
		return result;
	}
	
}
