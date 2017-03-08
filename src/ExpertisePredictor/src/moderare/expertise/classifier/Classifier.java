package moderare.expertise.classifier;

import moderare.expertise.model.Dataset;
import moderare.expertise.model.EXPERTISE;
import moderare.expertise.model.ModelSample;
import moderare.expertise.model.ModelingSession;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.core.Instance;
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
	
	public EXPERTISE classifyInstance(ModelSample sample) throws Exception {
		Instance i = sample.getWekaInstance();
		int classification = (int) classifier.classifyInstance(i);
		return EXPERTISE.fromString(EXPERTISE.names().get(classification));
	}
	
	public double classifyInstance(ModelingSession session, int windowSize, double minSupport) throws Exception {
		double totalClassfication = 0.0;
		double correctClassification = 0.0;
		for (int i = 0; i < session.size(); i++) {
			ModelSample sample = session.get(i);
			int correctInWindow = 0;
			if (i > windowSize) {
				for (int j = i; j > i - windowSize; j--) {
					if (classifyInstance(sample) == sample.getSampleClass()) {
						correctInWindow++;
					}
				}
				if (((double) correctInWindow / (double) windowSize) >= minSupport) {
					correctClassification++;
				}
				totalClassfication++;
			}
		}
		return correctClassification / totalClassfication;
	}
}
