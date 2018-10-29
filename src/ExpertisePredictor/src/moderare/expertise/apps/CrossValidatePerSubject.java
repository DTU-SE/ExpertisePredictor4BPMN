package moderare.expertise.apps;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import moderare.expertise.classifier.Classifier;
import moderare.expertise.classifier.DecisionTree;
import moderare.expertise.classifier.LogisticRegression;
import moderare.expertise.classifier.NeuralNetwork;
import moderare.expertise.classifier.RandomForest;
import moderare.expertise.classifier.SVM_Poly;
import moderare.expertise.classifier.SVM_RBF;
import moderare.expertise.model.DatabaseDataset;
import moderare.expertise.model.EXPERTISE;
import moderare.expertise.model.ModelSample;
import moderare.expertise.utils.DatabaseUtils;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;

public class CrossValidatePerSubject  extends ExpertisePredictor4BPMN {

	public static void main(String[] args) throws Exception {
		startup();
		
		for (String task : new String[]{
				"mortgage-1",
//				"pre-flight"
				}){
			Set<String> allExperts = new HashSet<String>();
			Set<String> allNovices = new HashSet<String>();
			
			ResultSet resultSet = connection.createStatement().executeQuery("SELECT DISTINCT model_id, expertise FROM metrics_evolution WHERE task = '" + task +"'");
			while (resultSet.next()) {
				Map<String, Object> result = DatabaseUtils.map(resultSet);
				if (result.get("expertise").equals("expert")) {
					allExperts.add(result.get("model_id").toString());
				} else {
					allNovices.add(result.get("model_id").toString());
				}
			}
			int totSubjects = allExperts.size() + allNovices.size();
			
			int folds = 3;
			
			List<List<String>> expertsPartitions = Lists.newArrayList(Iterables.partition(allExperts, allExperts.size() / folds));
			List<List<String>> novicesPartitions = Lists.newArrayList(Iterables.partition(allNovices, allNovices.size() / folds));
			for (int fold = 0; fold < folds; fold++) {
				Collection<String> testing = new HashSet<String>();
				testing.addAll(expertsPartitions.get(fold));
				testing.addAll(novicesPartitions.get(fold));
				
				System.out.println("=== FOLD " + (fold + 1) + " ===");
				System.out.println("> Using " + (allExperts.size() - expertsPartitions.get(fold).size()) + " expert subjects for training");
				System.out.println("> Using " + (allNovices.size() - novicesPartitions.get(fold).size()) + " novice subjects for training");
				System.out.println("> Using " + (totSubjects - testing.size()) + " subjects for training and " + testing.size() + " subjects for validation");
				
				DatabaseDataset trainingDataset = new DatabaseDataset(connection);
				trainingDataset.addFromDatabase(
						true,
						Arrays.asList(task),
						null,
						Arrays.asList(EXPERTISE.EXPERT),
						Arrays.asList("model_id NOT IN ('" + String.join("','", testing) + "')"),
						0.3,
						4000,
						"rand ASC");
				trainingDataset.addFromDatabase(
						true,
						Arrays.asList(task),
						null,
						Arrays.asList(EXPERTISE.NOVICE),
						Arrays.asList("model_id NOT IN ('" + String.join("','", testing) + "')"),
						0.3,
						4000,
						"rand ASC");
				DatabaseDataset testingDataset = new DatabaseDataset(connection);
				testingDataset.addFromDatabase(
						true,
						Arrays.asList(task),
						null,
						Arrays.asList(EXPERTISE.EXPERT),
						Arrays.asList("model_id IN ('" + String.join("','", testing) + "')"),
						0.3,
						4000,
						"rand ASC");
				testingDataset.addFromDatabase(
						true,
						Arrays.asList(task),
						null,
						Arrays.asList(EXPERTISE.NOVICE),
						Arrays.asList("model_id IN ('" + String.join("','", testing) + "')"),
						0.3,
						4000,
						"rand ASC");
				for (Classifier classifier : new Classifier[] {
						new NeuralNetwork(),
//						new SVM_Poly(),
//						new SVM_RBF(),
//						new DecisionTree(),
						new RandomForest(),
//						new LogisticRegression()
				}) {
					
					System.out.println("=== TESTING WITH " + classifier.getClass().getSimpleName().toUpperCase() + " ===");
					Evaluation eval = classifier.evaluate(trainingDataset, testingDataset);
					System.out.println(classifier.printEvaluation(eval));
				}
			}
//				for (EXPERTISE expertise : EXPERTISE.values()) {
//					dataset.addFromDatabase(true,
//							Arrays.asList(task),
//							null,
//							Arrays.asList(expertise), 0.3, size, "rand ASC");
//				}
		}
		
		end();
	}
}
