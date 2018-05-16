package moderare.expertise.classifier;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.SimpleLogistic;

public class LogisticRegression extends Classifier {

	public LogisticRegression() {
		
	}
	
	public LogisticRegression(String file) throws Exception {
		super(file);
	}
	
	@Override
	protected AbstractClassifier construct() {
		return new SimpleLogistic();
	}

}
