import org.encog.mathutil.rbf.RBFEnum;

public class Result {
	int correctCount;
	int gameLength;
	int acceptableCount;
	int correctStateCount;
	int wrapError;
	long trainTime;
	long oneRound;
	int numNeuronsPerDimension;
	double vnwIndex;
	RBFEnum rbf;
	TrainEnum train;
	boolean isKohonen;
	
	public Result(	int correctCount,
					int gameLength,
					int acceptableCount,
					int correctStateCount,
					int wrapError,
					int numNeuronsPerDimension,
					double vnwIndex,
					RBFEnum rbf,
					TrainEnum train,
					boolean isKohonen){
		this.correctCount = correctCount;
		this.gameLength = gameLength;
		this.acceptableCount = acceptableCount;
		this.correctStateCount = correctStateCount;
		this.wrapError = wrapError;
		this.isKohonen = isKohonen;
		this.rbf = rbf;
		this.train = train;
	}
	
	public Result(	int correctCount,
			int gameLength,
			int acceptableCount,
			int correctStateCount,
			int wrapError,
			int numNeuronsPerDimension){
		this.correctCount = correctCount;
		this.gameLength = gameLength;
		this.acceptableCount = acceptableCount;
		this.correctStateCount = correctStateCount;
		this.wrapError = wrapError;
	}
	
	public void setOneRoundTime(long time){
		this.oneRound = time;
	}
	
	public void setAllTime(long time){
		this.trainTime = time;
	}
	
	public void print(){
		if (rbf != null)
			System.out.println("RBF method: " + rbf);
		if (train != null)
			System.out.println("Train method: " + train);
		if (rbf != null)
			System.out.println("Kohonen: " + isKohonen);
		if (trainTime != 0)
			System.out.println("Train time: " + trainTime);
		if (oneRound != 0)
			System.out.println("One round time: " + oneRound);
		System.out.println("Correct count: " + correctCount);
		System.out.println("Acceptable Count: " + acceptableCount);
		System.out.println("Wrap Error: " + wrapError);
		System.out.println("Correct state count: " + correctStateCount);
		System.out.println("----------------------------------------------------------------------------");
	}
	
}
