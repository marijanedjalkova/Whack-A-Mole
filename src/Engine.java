import java.util.ArrayList;
import java.util.Scanner;

import org.encog.mathutil.rbf.RBFEnum;

public class Engine {
	static ArrayList<Result> results;
	static MoleGame g;

	public static void main(String[] args) {
		Scanner user_input = new Scanner( System.in );
		String input;
		System.out.println("Please enter 1 for part1 or 2 for part 2: ");
		System.out.println("Warning: part 1 runs a series of 32 test on RBF nets");
		System.out.println(" with differet settings. If you only want to see the game");
		System.out.println(" for the best network, please select 3");
		input = user_input.next();
		if (input.equals("1")){
			System.out.println("This part runs a series of tests for the game with different networks");
			System.out.println("The results will be output on the screen");
			runTests();
		} else if (input.equals("2")){
			g = new MoleGame(false);
			g.player.initialiseUnscriptedNet();
			g.startUnscriptedGame();
		}else if (input.equals("3")){
			results = new ArrayList<Result>();
			g = new MoleGame(true);
			play(RBFEnum.Gaussian, TrainEnum.RESILIENTPROPAGATION, true);
		}
		user_input.close();
		
	}
	
	public static void runTests(){
		results = new ArrayList<Result>();
		g = new MoleGame(true);
		do_experiments();
		analyze_results();
	}
	
	
	public static void analyze_results(){
		long minAllTime = 100000000000l;
		long minOneRoundTime = 1000000000000l;
		Result fastestAllTimesRes = null;
		Result fastestOneRoundRes = null;
		int maxCorrect = 0;
		Result best = null;
		int maxStatesGuessed = 0;
		Result bestAtStates = null;
		System.out.println("Number of results: " + results.size());
		for (Result r : results){
			if (r.trainTime < minAllTime){
				minAllTime = r.trainTime;
				fastestAllTimesRes = r;
			}
			if (r.oneRound < minOneRoundTime){
				minOneRoundTime = r.oneRound;
				fastestOneRoundRes = r;
			}
			if (r.correctCount > maxCorrect){
				maxCorrect = r.correctCount;
				best = r;
			}
			if (r.correctStateCount > maxStatesGuessed){
				maxStatesGuessed = r.correctStateCount;
				bestAtStates = r;
			}
		}
		if (fastestAllTimesRes != null){
			System.out.println("FASTEST IN TRAINING");
			fastestAllTimesRes.print();
		}
		if (best != null){
			System.out.println("BEST");
			best.print();
		}
		if (fastestOneRoundRes != null){
			System.out.println("FASTEST IN ONE ROUND");
			fastestOneRoundRes.print();
		}
		if (bestAtStates != null){
			System.out.println("BEST AT STATES");
			bestAtStates.print();
		}
	}
	
	public static void do_experiments(){
		
		for (RBFEnum rbf : RBFEnum.values()){
			for (TrainEnum trainMethod : TrainEnum.values()){
				System.out.println("RBF is " + rbf + " train enum is " + trainMethod + " KOHONEN");
				play(rbf, trainMethod, true);
				System.out.println("RBF is " + rbf + " train enum is " + trainMethod + " ANALOG");
				play(rbf, trainMethod, false);
			}
		}
	}
	
	public static void play(RBFEnum rbf, TrainEnum trainMethod, boolean isKohonen){
		long startTime = System.currentTimeMillis();
		g.player.inilialiseScriptedNet();
		g.player.learn(50, 0.9, rbf, trainMethod, isKohonen);
		long stopTime = System.currentTimeMillis();
		long trainTime = stopTime - startTime;
	    long oneRoundTime = (trainTime) / NeuralNet.getMaxEpoch(trainMethod);
	    g.resetGame();
	    
		g.startScriptedGame();
		Result r = g.gameResult;
		r.setOneRoundTime(oneRoundTime);
		r.setAllTime(trainTime);
		results.add(r);
		r.print();
	}

}
