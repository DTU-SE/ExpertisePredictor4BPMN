package moderare.expertise.classifier;

import moderare.expertise.model.Dataset;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.core.SerializationHelper;

public abstract class Classifier {

	protected AbstractClassifier classifier;
	
	public void save(String file) throws Exception {
		SerializationHelper.write(file, classifier);
	}
	
	public void load(String file) throws Exception {
		classifier = (AbstractClassifier) SerializationHelper.read(file);
	}
	
	public void train(Dataset trainingDataset) {
		try {
			classifier.buildClassifier(trainingDataset.getWekaInstances());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Evaluation evaluate(Dataset trainingDataset, Dataset testDataset) throws Exception {
		Evaluation eval = new Evaluation(trainingDataset.getWekaInstances());
		eval.evaluateModel(classifier, testDataset.getWekaInstances());
		return eval;
	}
	
	public String printEvaluation(Dataset trainingDataset, Dataset testDataset) throws Exception {
		Evaluation eval = evaluate(trainingDataset, testDataset);
		return eval.toSummaryString("Evaluation statistics:", false) + "\n" + eval.toMatrixString("Confusion matrix:");
	}
}
