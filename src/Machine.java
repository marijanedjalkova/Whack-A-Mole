import org.encog.Encog;
import org.encog.mathutil.rbf.RBFEnum;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.train.MLTrain;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import org.encog.neural.networks.training.propagation.quick.QuickPropagation;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.neural.networks.training.propagation.scg.ScaledConjugateGradient;
import org.encog.neural.pattern.RadialBasisPattern;
import org.encog.neural.rbf.RBFNetwork;

public class Machine {
    public static double[][] INPUT;
    public static double[][] IDEAL;
    public static RBFNetwork network;
    public int outputNeurons;
    public boolean isKohonen;
    
    
 // 50, 0.9, 102, RBFEnum.Gaussian, TrainEnum.SVDTraining
    public void learn(int numNeuronsPerDimension, double vnwIndex, int outputNeurons, RBFEnum rbf, TrainEnum trainMethod, boolean isKohonen){
    	this.outputNeurons = outputNeurons;
    	this.isKohonen = isKohonen;
    	int dimensions = 2;
        
        double volumeNeuronWidth = vnwIndex / numNeuronsPerDimension;
       
        boolean includeEdgeRBFs = true;
        
        RadialBasisPattern pattern = new RadialBasisPattern();
        pattern.setInputNeurons(dimensions);
        pattern.setOutputNeurons(outputNeurons);
        
        int numNeurons = (int)Math.pow(numNeuronsPerDimension, dimensions);
        pattern.addHiddenLayer(numNeurons);
        network = (RBFNetwork)pattern.generate();
       
        network.setRBFCentersAndWidthsEqualSpacing(0, 1, rbf, volumeNeuronWidth, includeEdgeRBFs);

        create2DSmoothTainingDataGit();
      
        MLDataSet trainingSet = new BasicMLDataSet(INPUT, IDEAL);
        MLTrain train;
        
        switch (trainMethod) {
		case RESILIENTPROPAGATION:
			train = new ResilientPropagation(network, trainingSet);
			break;
		case BACKPROPAGATION:
			train = new Backpropagation(network, trainingSet);
			break;
		case QUICKPROPAGATION:
			train = new QuickPropagation(network, trainingSet);
			break;
		case SCALEDCONJUGATEGRADIENT:
			train = new ScaledConjugateGradient(network, trainingSet);
			break;
		default:
			train = new ResilientPropagation(network, trainingSet);
			break;
		}
        

        int epoch = 1;
        int maxEpoch = getMaxEpoch(trainMethod);
        do
        {
            train.iteration();
            if ( epoch % (maxEpoch / 100)==0)
            	System.out.println("Epoch #" + epoch + " Error:" + train.getError());
            epoch++;
        } while ((epoch < maxEpoch) && (train.getError() > 0.000001));
    }
    
    public int getMaxEpoch(TrainEnum trainMethod){
    	switch (trainMethod) {
		case RESILIENTPROPAGATION:
			return 10000;
		case BACKPROPAGATION:
			return 100000;
		case QUICKPROPAGATION:
			return 100000;
		case SCALEDCONJUGATEGRADIENT:
			return 1000;
		default:
			return 1000;
		}
    }
    
    public Machine(){
        //Specify the number of dimensions and the number of neurons per dimension
        int dimensions = 2;
        int numNeuronsPerDimension = 50;
        //Set the standard RBF neuron width. 
        //Literature seems to suggest this is a good default value.
        double volumeNeuronWidth = 0.9 / numNeuronsPerDimension;
        //RBF can struggle when it comes to flats at the edge of the sample space.
        //We have added the ability to include wider neurons on the sample space boundary which greatly
        //improves fitting to flats
        boolean includeEdgeRBFs = true;
        //General setup is the same as before
        RadialBasisPattern pattern = new RadialBasisPattern();
        pattern.setInputNeurons(dimensions);
        pattern.setOutputNeurons(2);
        //Total number of neurons required.
        //Total number of Edges is calculated possibly for future use but not used any further here
        int numNeurons = (int)Math.pow(numNeuronsPerDimension, dimensions);
        pattern.addHiddenLayer(numNeurons);
        network = (RBFNetwork)pattern.generate();
        //Position the multidimensional RBF neurons, with equal spacing, within the provided sample space from 0 to 1.
        network.setRBFCentersAndWidthsEqualSpacing(0, 1, RBFEnum.InverseMultiquadric, volumeNeuronWidth, includeEdgeRBFs);

        //Create some training data that can not easily be represented by gaussians
        //There are other training examples for both 1D and 2D
        //Degenerate training data only provides outputs as 1 or 0 (averaging over all outputs for a given set of inputs would produce something approaching the smooth training data).
        //Smooth training data provides true values for the provided input dimensions.
        create2DSmoothTainingDataGit();
        //Create the training set and train.
        MLDataSet trainingSet = new BasicMLDataSet(INPUT, IDEAL);
        MLTrain train = new ResilientPropagation(network, trainingSet);
        //SVD is a single step solve
        int epoch = 1;
        do
        {
            train.iteration();
            if (epoch  % 100 == 0)
            	System.out.println("Epoch #" + epoch + " Error:" + train.getError());
            epoch++;
        } while ((epoch < 9000) && (train.getError() > 0.000001));
    }

    
    public static int[] makeDecision(int clue, int stateValue){
 	   double[] inputArray = {scale(clue), scaleState(stateValue)};
 	   double[][] iArray = new double[1][2];
 	   iArray[0] = inputArray;
 	   BasicMLDataSet input_set = new BasicMLDataSet(iArray, null);
 	   MLData output = network.compute(input_set.get(0).getInput());
 	   return new int[] {inverseScale(output.getData(0)), inverseStateScale(output.getData(1))};
    }
    
    public static void shutDown(){
 	   Encog.getInstance().shutdown();
    }
     
    private static int calculate(int clue, int stateValue){
    	int result = 0;
 	  if (stateValue == 0){
 		  result = clue + 5;
 		  if (result >= 100){
 			  result = result - 100;
 		  }
 	  } else {
 		   result = clue - 5;
 		   if (result < 0){
 			   result = 100 + result;
 		   }
 	   }
 	  return result;
    }
    
    private static int calcState(int pos){
    	return pos % 2;
    }

    static void create2DSmoothTainingDataGit(){
        int iLimit = 99;
        int kLimit = 1;
        
        INPUT = new double[(iLimit + 1) * (kLimit + 1)][];
        IDEAL = new double[(iLimit + 1) * (kLimit + 1)][];
        for (int i = 0; i <= iLimit; i++)
        {
            for (int k = 0; k <= kLimit; k++)
            {
                INPUT[i * (kLimit + 1) + k] = new double[2];
                IDEAL[i * (kLimit + 1) + k] = new double[2];
                int res = calculate(i, k);
                INPUT[i * (kLimit + 1) + k][0] = scale(i);
                INPUT[i * (kLimit + 1) + k][1] = scaleState(k);
                IDEAL[i * (kLimit + 1) + k][0] = scale(res);
                IDEAL[i * (kLimit + 1) + k][1] = scaleState(calcState(res));
            }
        }
    }
    
    private static double scaleState(int value){
    	if (value == 0)
    		return 0.9;
    	return 0.1;
    }
    
    public static int inverseStateScale(double i){
    	return (int)Math.round(i);
    }
    
    private static double scale(double i){
    	return (double) i / 100;
    }
    
    public static int inverseScale(double i){
    	return (int)Math.round(((i ) * 100));
    }
 
}