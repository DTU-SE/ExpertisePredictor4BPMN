package moderare.expertise.classifier;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.trees.J48;

public class DecisionTree extends Classifier {

	public DecisionTree() {
		
	}
	
	public DecisionTree(String file) throws Exception {
		super(file);
	}
	
	@Override
	protected AbstractClassifier construct() {
		J48 tree = new J48();
		return tree;
	}

}
