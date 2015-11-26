import java.io.IOException;
import org.encog.Encog;
import org.encog.mathutil.rbf.RBFEnum;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.train.MLTrain;
import org.encog.neural.freeform.training.FreeformResilientPropagation;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.neural.pattern.RadialBasisPattern;
import org.encog.neural.rbf.RBFNetwork;
import org.encog.neural.rbf.training.SVDTraining;

public class Machine {
    public static double[][] INPUT;
    public static double[][] IDEAL;
    public static RBFNetwork network;
    
    public Machine() throws IOException{
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
        pattern.setOutputNeurons(1);
        //Total number of neurons required.
        //Total number of Edges is calculated possibly for future use but not used any further here
        int numNeurons = (int)Math.pow(numNeuronsPerDimension, dimensions);
        int numEdges = (int)(dimensions * Math.pow(2, dimensions - 1));
        pattern.addHiddenLayer(numNeurons);
        network = (RBFNetwork)pattern.generate();
        //Position the multidimensional RBF neurons, with equal spacing, within the provided sample space from 0 to 1.
        network.setRBFCentersAndWidthsEqualSpacing(0, 1, RBFEnum.Gaussian, volumeNeuronWidth, includeEdgeRBFs);

        //Create some training data that can not easily be represented by gaussians
        //There are other training examples for both 1D and 2D
        //Degenerate training data only provides outputs as 1 or 0 (averaging over all outputs for a given set of inputs would produce something approaching the smooth training data).
        //Smooth training data provides true values for the provided input dimensions.
        create2DSmoothTainingDataGit();
        //Create the training set and train.
        MLDataSet trainingSet = new BasicMLDataSet(INPUT, IDEAL);
        MLTrain train = new SVDTraining(network, trainingSet);
        //SVD is a single step solve
        int epoch = 1;
        do
        {
            train.iteration();
            if (epoch % 1000 == 0)
            	System.out.println("Epoch #" + epoch + " Error:" + train.getError());
            epoch++;
        } while ((epoch < 2) && (train.getError() > 0.000001));
    }

    
    public static int makeDecision(int clue, int stateValue){
 	   double[] inputArray = {scale(clue), scaleState(stateValue)};
 	   System.out.println(" input is " + scale(clue) + " and " + scaleState(stateValue));
 	   double[][] iArray = new double[1][2];
 	   iArray[0] = inputArray;
 	   BasicMLDataSet input_set = new BasicMLDataSet(iArray, null);
 	   MLData output = network.compute(input_set.get(0).getInput());
 	   System.out.println(" output data will be " + output.getData(0));
 	   return inverseScale(output.getData(0));
    }
    
    public static void shutDown(){
 	   Encog.getInstance().shutdown();
    }
     
    private static int calculate(int clue, int stateValue){
    	int result = 0;
 	  if (stateValue == 0){
 		  result = clue + 5;
 		  if (result > 100){
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

    static void create2DSmoothTainingDataGit()
    {
        int iLimit = 99;
        int kLimit = 1;
        //int jLimit = 100;
        INPUT = new double[(iLimit + 1) * (kLimit + 1)][];
        IDEAL = new double[(iLimit + 1) * (kLimit + 1)][];
        for (int i = 0; i <= iLimit; i++)
        {
            for (int k = 0; k <= kLimit; k++)
            {
                INPUT[i * (kLimit + 1) + k] = new double[2];
                IDEAL[i * (kLimit + 1) + k] = new double[1];
                int res = calculate(i, k);
                INPUT[i * (kLimit + 1) + k][0] = scale(i);
                INPUT[i * (kLimit + 1) + k][1] = scaleState(k);
                IDEAL[i * (kLimit + 1) + k][0] = scale(res);
            }
        }
    }
    
    private static double scaleState(int value){
    	if (value == 0)
    		return 0.9;
    	else
    	return 0.1;
    }
    
    private static double scale(double i){
    	return (double) i / 100;
    }
    
    public static int inverseScale(double i){
    	return (int)Math.round(((i ) * 100));
    }
 
}