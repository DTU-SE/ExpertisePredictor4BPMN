package moderare.expertise.classifier;

import weka.classifiers.AbstractClassifier;

public class RandomForest extends Classifier {

	public RandomForest() {
		
	}
	
	public RandomForest(String file) throws Exception {
		super(file);
	}
	
	@Override
	protected AbstractClassifier construct() {
		return new weka.classifiers.trees.RandomForest();
	}

}
