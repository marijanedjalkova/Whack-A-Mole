import java.io.IOException;

public class Player {
	Machine learner;
	
	public Player(){
		try {
			learner = new Machine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public int makeGuess(int clue, State curState) {
		// TODO maybe need some conversion?
		int stateValue = clue;
		if (curState != null){
			stateValue = curState.getValue();
		}
		return Machine.makeDecision(clue, stateValue);
	}
	
	public void finish(){
		Machine.shutDown();
	}
}
