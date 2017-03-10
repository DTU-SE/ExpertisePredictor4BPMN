package moderare.expertise.classifier;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.MultilayerPerceptron;

public class NeuralNetwork extends Classifier {

	public NeuralNetwork() {
		
	}
	
	public NeuralNetwork(String file) throws Exception {
		super(file);
	}

	@Override
	protected AbstractClassifier construct() {
		MultilayerPerceptron mlp = new MultilayerPerceptron();
		mlp.setTrainingTime(500);
		mlp.setLearningRate(0.3);
		mlp.setMomentum(0.2);
		mlp.setHiddenLayers("50"); // one hidden layer with 50 neurons
		return mlp;
	}
}
