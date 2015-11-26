import java.util.Random;

public class MoleGame {
	private final int GAME_LENGTH = 100;
	int moveCount;
	Player player;
	FSM model;
	int correctCount;
	int acceptableCount;
	int correctStateCount;
	int acceptableError = 2;
	int wrapError;
	Result gameResult;
	
	
	public MoleGame(){
		moveCount = 0;
		player = new Player();
		model = new FSM();
		correctCount = 0;
		acceptableCount = 0;
		correctStateCount = 0;
		wrapError = 0;
		
	}
	
	public void resetGame(){
		moveCount = 0;
		model = new FSM();
		correctCount = 0;
		acceptableCount = 0;
		correctStateCount = 0;
		gameResult = null;
		wrapError = 0;
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
		
		while(!finished()){
			int move = getMove();
			int clue = getClueFromMove(move, model.currentState);
			int[] guess = player.makeGuess(clue, model.currentState);
			int pos_guess = guess[0];
			int state_guess = guess[1];
			model.updateState(move);
			analyze_guess(pos_guess, state_guess, move, model.currentState.getValue(), clue);
			moveCount++;
		}
		
		gameResult = new Result(correctCount, 
								GAME_LENGTH, 
								acceptableCount, 
								correctStateCount, 
								wrapError, 
								player.net.numNeuronsPerDimension,
								player.net.vnwIndex,
								player.net.rbf,
								player.net.trainMethod,
								player.isKohonen);
		//print_results();
		
	}
	
	private void print_results(){
		System.out.println("Guessed " + correctCount + " out of " + GAME_LENGTH);
		System.out.println("Acceptable " + acceptableCount + " out of " + GAME_LENGTH);
		System.out.println("Guessed states: " + correctStateCount + " out of " + GAME_LENGTH);
		System.out.println("Wrapping error: " + wrapError);
		
	}

	private int getClueFromMove(int move, State currentState) {
		int result;
		if (currentState == null){
			result =  move + 5;
			if (result >= 100)
				result = result - 100;
		} else {
			if (currentState.getValue() ==1){
				result = move + 5;
				if (result >= 100)
					result = result - 100;
			}else{
				result = move - 5;
				if (result < 0)
					result = result + 100;
			}
		}
		return result;
	}
	
	private void analyze_guess(int mole_guess, int state_guess, int mole_pos, int curStateValue, int clue){
		//System.out.println("IN: " + clue + ", state: " + curStateValue);
		//System.out.println("OUT mole: " + mole_guess + "(" + mole_pos + "), state: " + state_guess + "(" + curStateValue + ")");
		if (state_guess == curStateValue){
			//System.out.println("State - correct!");
			correctStateCount++;
		} else {
			//System.out.println("State - wrong!");
		}
		if (Math.abs(mole_guess - mole_pos) < acceptableError){
			acceptableCount++;
			if (mole_guess == mole_pos){
				//System.out.println("Mole: correct!");
				correctCount++;
			} else {
				//System.out.println("Mole: ok!");
			}
		} else {
			//System.out.println("==== Mole: WRONG by " + (mole_guess - mole_pos));
			if (clue < 5 || clue > 95)
				wrapError++;
		}
		//System.out.println("---------------------------------------->");
	}
	
}
