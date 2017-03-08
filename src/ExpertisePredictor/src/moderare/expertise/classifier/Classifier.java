package moderare.expertise.classifier;

import moderare.expertise.model.Dataset;
import moderare.expertise.model.ModelSample;
import weka.classifiers.AbstractClassifier;

public abstract class Classifier {

	protected AbstractClassifier classifier;
	
	public void train(Dataset trainingDataset) {
		try {
			classifier.buildClassifier(trainingDataset.getInstances());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
