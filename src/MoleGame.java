import java.util.Random;

public class MoleGame {
	private final int GAME_LENGTH = 100;
	int time;
	Player player;
	FSM model;
	int correctCount;
	int acceptableCount;
	int correctStateCount;
	int acceptableError = 2;
	int wrapError;
	Result gameResult;
	boolean isScripted;
	
	
	public MoleGame(boolean scripted){
		this.isScripted = scripted;
		time = 0;
		player = new Player();
		if (this.isScripted)
			player.inilialiseScriptedNet();
		else
			player.initialiseUnscriptedNet();
		model = new FSM();
		correctCount = 0;
		acceptableCount = 0;
		correctStateCount = 0;
		wrapError = 0;
		
	}
	
	public void startUnscriptedGame(){
		while(!finished()){
			int move = getMove();
			int clue = getUnscriptedClueFromMove(move, model.currentState);
			State s = model.currentState == null ? model.getState(0) : model.currentState;
			int[] guess = player.makeUnscriptedGuess(clue, s, move, model.getState(move).getValue());
			int pos_guess = guess[0];
			int state_guess = guess[1];
			model.updateState(move);
			analyze_guess(pos_guess, state_guess, move, model.currentState.getValue(), clue);
			time++;
		}
		System.out.println("DONE-------------------------------------");
		gameResult = new Result(correctCount, 
				GAME_LENGTH, 
				acceptableCount, 
				correctStateCount, 
				wrapError, 
				50);
		gameResult.print();
	}
	
	public int h(){
		return time + 1;
	}
	
	public int getUnscriptedClueFromMove(int move, State currentState){
		int result = getClueFromMove(move, currentState);
		result += h();
		if (result >= 100)
			return result - 100;
		if (result < 0)
			return result + 100;
		return result;
	}
	
	public void resetGame(){
		time = 0;
		model = new FSM();
		correctCount = 0;
		acceptableCount = 0;
		correctStateCount = 0;
		gameResult = null;
		wrapError = 0;
	}
	


	public void startScriptedGame(){
		
		while(!finished()){
			int move = getMove();
			int clue = getClueFromMove(move, model.currentState);
			int[] guess = player.makeGuess(clue, model.currentState);
			int pos_guess = guess[0];
			int state_guess = guess[1];
			model.updateState(move);
			analyze_guess(pos_guess, state_guess, move, model.currentState.getValue(), clue);
			time++;
			if (time > 30 && ((double)correctCount/time < 0.6)){
				player.learn(50, 0.9, player.net.rbf, player.net.trainMethod, player.isKohonen);
			}
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
		
	}
	
	public int getMove(){
		Random r = new Random();
		int Low = 1;
		int High = 100;
		return r.nextInt(High-Low) + Low;
	}
	
	
	public boolean finished(){
		if (time < GAME_LENGTH)
			return false;
		return true;
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
		System.out.println(time + ". Clue: " + clue+ ", mole: " + mole_guess +"(" + mole_pos + ")" +  
				 ", state guess " + state_guess + "(" + model.currentState.value + ")");
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
