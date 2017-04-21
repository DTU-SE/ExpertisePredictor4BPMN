package moderare.expertise.classifier;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import moderare.expertise.exceptions.WrongValueType;
import moderare.expertise.model.Dataset;
import moderare.expertise.model.EXPERTISE;
import moderare.expertise.model.ModelSample;
import moderare.expertise.model.ModelingSession;
import moderare.expertise.utils.ChartUtils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

public abstract class Classifier {

	private AbstractClassifier classifier;
	
	public Classifier() {
		classifier = construct();
	}
	
	public Classifier(String file) throws Exception {
		load(file);
	}
	
	protected abstract AbstractClassifier construct();
	
	public void save(String file) throws Exception {
		SerializationHelper.write(file, classifier);
	}
	
	public void load(String file) throws Exception {
		classifier = (AbstractClassifier) SerializationHelper.read(file);
	}
	
	public void train(Dataset trainingDataset, boolean doSmote) {
		try {
			classifier.buildClassifier(trainingDataset.getWekaInstances(doSmote));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Evaluation crossValidation(Dataset dataset, int folds, boolean doSmote) throws Exception {
		Random rand = new Random();
		Instances randData = dataset.getWekaInstances(doSmote);
		randData.randomize(rand);
		if (randData.classAttribute().isNominal()) {
			randData.stratify(folds);
		}
		Evaluation eval = new Evaluation(randData);
		for (int n = 0; n < folds; n++) {
			System.out.print("Cross validation - fold " + (n + 1) + "/" + folds + "... ");
			Instances train = randData.trainCV(folds, n);
			Instances test = randData.testCV(folds, n);
			
			// build and evaluate classifier
			weka.classifiers.Classifier clsCopy = AbstractClassifier.makeCopy(classifier);
			clsCopy.buildClassifier(train);
			eval.evaluateModel(clsCopy, test);
			System.out.println("OK");
		}
		return eval;
	}
	
	public Evaluation evaluate(Dataset trainingDataset, Dataset testDataset) throws Exception {
		Evaluation eval = new Evaluation(trainingDataset.getWekaInstances());
		eval.evaluateModel(classifier, testDataset.getWekaInstances());
		return eval;
	}
	
	public String printEvaluation(Dataset dataset, int folds, boolean doSmote) throws Exception {
		return printEvaluation(crossValidation(dataset, folds, doSmote));
	}
	
	public String printEvaluation(Dataset trainingDataset, Dataset testDataset) throws Exception {
		return printEvaluation(evaluate(trainingDataset, testDataset));
	}
	
	public String printEvaluation(Evaluation evaluation) throws Exception {
		return evaluation.toSummaryString() + "\n" + evaluation.toClassDetailsString() + "\n" + evaluation.toMatrixString();
	}
	
	public EXPERTISE classifyInstance(ModelSample sample) throws Exception {
		Instance i = sample.getWekaInstance();
		int classification = (int) classifier.classifyInstance(i);
		return EXPERTISE.fromString(EXPERTISE.names().get(classification));
	}
	
	public List<EXPERTISE> classifyInstance(Dataset session, double minRelativeTime) throws WrongValueType {
		List<EXPERTISE> classifications = new ArrayList<EXPERTISE>();
		for(ModelSample sample : session) {
			if (minRelativeTime < 0 || sample.getNumeric("relative_modeling_time") >= minRelativeTime) {
				EXPERTISE expertise = null;
				try {
					expertise = classifyInstance(sample);
				} catch (Exception e) {
					e.printStackTrace();
				}
				classifications.add(expertise);
			}
		}
		return classifications;
	}
	
	public List<EXPERTISE> classifyInstance(Dataset session, int windowSize, double minSupport) {
		return null;
	}
	
	public double computeLocalAccuracy(ModelingSession session, int windowSize, double minSupport, double startRelativeTime, double endRelativeTime) throws WrongValueType {
		List<EXPERTISE> classifications = classifyInstance(session, 0);
		double totalClassfication = 0.0;
		double correctClassification = 0.0;
		for (int i = 0; i < classifications.size(); i++) {
			ModelSample sample = session.get(i);
			double relativeTime = sample.getNumeric("relative_modeling_time");
			int correctInWindow = 0;
			if (i > windowSize) {
				for (int j = i; j > i - windowSize; j--) {
					if (classifications.get(j) == sample.getSampleClass()) {
						correctInWindow++;
					}
				}
				if (relativeTime > startRelativeTime && relativeTime <= endRelativeTime) {
					if (((double) correctInWindow / (double) windowSize) >= minSupport) {
						correctClassification++;
					}
					totalClassfication++;
				}
			}
		}
		return correctClassification / totalClassfication;
	}
	
	public double computeAccuracy(ModelingSession session, int windowSize, double minSupport, double minRelativeTime) throws WrongValueType {
		List<EXPERTISE> classifications = classifyInstance(session, minRelativeTime);
		double totalClassfication = 0.0;
		double correctClassification = 0.0;
		for (int i = 0; i < classifications.size(); i++) {
			ModelSample sample = session.get(i);
			int correctInWindow = 0;
			if (i > windowSize) {
				for (int j = i; j > i - windowSize; j--) {
					if (classifications.get(j) == sample.getSampleClass()) {
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
	
//	public String computeSessionAccuracy(ModelingSession session, int windowSize, double minSupport) {
//		List<EXPERTISE> classifications = classifyInstance(session);
//		StringBuffer output = new StringBuffer();
//		for (int i = windowSize; i < session.size(); i++) {
////			ModelSample sample = session.get(i);
//			double classifiedAsExpert = 0;
//			double classifiedAsNovice = 0;
//			for (int j = i; j > i - windowSize; j--) {
//				if (classifications.get(j) == EXPERTISE.NOVICE) {
//					classifiedAsNovice++;
//				} else {
//					classifiedAsExpert++;
//				}
//			}
//			classifiedAsExpert = classifiedAsExpert / (double) windowSize;
//			classifiedAsNovice = classifiedAsNovice / (double) windowSize;
//			if (classifiedAsExpert >= minSupport || classifiedAsNovice >= minSupport) {
//				EXPERTISE predicted = (classifiedAsExpert > classifiedAsNovice) ? EXPERTISE.EXPERT : EXPERTISE.NOVICE;
//				output.append(predicted.name().substring(0, 1) + " ");
//			}
//		}
//		return output.toString();
//	}
	
	public void exportAccuracyChart(ModelingSession session, int[] windowSizes, String fileName) throws WrongValueType {
		List<EXPERTISE> classifications = classifyInstance(session, -1);
		EXPERTISE expectedExpertise = null;
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		for (int windowSize : windowSizes) {
			XYSeries series = new XYSeries("Window size " + windowSize, true);
			for (int i = 0; i < session.size(); i++) {
				ModelSample sample = session.get(i);
				if (expectedExpertise == null) {
					expectedExpertise = sample.getSampleClass();
				}
				if (i > windowSize) {
					int correctInWindow = 0;
					for (int j = i; j > i - windowSize; j--) {
						if (classifications.get(j) == expectedExpertise) {
							correctInWindow++;
						}
					}
					try {
						Double time = sample.getNumeric("relative_modeling_time");
						Double score = correctInWindow / (double) windowSize;
						series.add(time, score);
					} catch (WrongValueType e) {
						e.printStackTrace();
					}
				}
			}
			dataset.addSeries(series);
		}
		
		JFreeChart chart = ChartFactory.createScatterPlot(session.getModelId(),
				"Relative time (% modeling session)", // x axis label
				"Correctness ratio", // y axis label
				dataset, // data
				PlotOrientation.VERTICAL, //
				true, // include legend
				true, // tooltips
				false // urls
				);
		
		@SuppressWarnings("unchecked")
		List<Title> subTitles = chart.getSubtitles();
		subTitles.add(new TextTitle("Expertise: " + session.getSampleClass() + "          Task name: " + session.getTaskName()));
		chart.setSubtitles(subTitles);
		
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		for (int i = 0; i < windowSizes.length; i++) {
			renderer.setSeriesLinesVisible(i, true);
			renderer.setSeriesShapesVisible(i, false);
			renderer.setSeriesStroke(i, new BasicStroke(3f));
		}
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setRenderer(renderer);
		plot.getRangeAxis().setRange(0.0, 1.0);
		plot.getDomainAxis().setRange(0.0, 1.0);

		try {
			if (fileName.substring(fileName.length() - 3).equalsIgnoreCase("svg")) {
				ChartUtils.saveChartAsSVG(chart, new File(fileName), 800, 400);
			} else {
				ChartUtilities.saveChartAsPNG(new File(fileName), chart, 800, 400);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void exportCorrectClassificationChart(ModelingSession session, int[] windowSizes, String fileName, double minSupport) throws WrongValueType {
		List<EXPERTISE> classifications = classifyInstance(session, -1);
		EXPERTISE expectedExpertise = null;
		
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		
		List<Boolean> correctlyClassifiedInterval = new ArrayList<Boolean>();
		List<Integer> seriesToWindowSizes = new ArrayList<Integer>();
		for (int ws = 0; ws < windowSizes.length; ws++) {
			int windowSize = windowSizes[ws];
			Double previousTime = null;
			Boolean previousCorrectness = false;
			for (int i = 0; i < session.size(); i++) {
				ModelSample sample = session.get(i);
				if (expectedExpertise == null) {
					expectedExpertise = sample.getSampleClass();
				}
				if (i > windowSize) {
					int correctInWindow = 0;
					for (int j = i; j > i - windowSize; j--) {
						if (classifications.get(j) == expectedExpertise) {
							correctInWindow++;
						}
					}
					try {
						Double time = sample.getNumeric("relative_modeling_time");
						Boolean correct = false;
						if (((double) correctInWindow / (double) windowSize) >= minSupport) {
							correct = true;
						}
							if (previousCorrectness != correct || i == session.size() - 1) {
								if (i == session.size() - 1) {
									correct = !previousCorrectness;
								}
								dataset.addValue(
									(previousTime == null)? time : time - previousTime,
									windowSize + "-" + i,
									"ws = " + windowSize);
								correctlyClassifiedInterval.add(!correct);
								seriesToWindowSizes.add(ws);
								previousTime = time;
								previousCorrectness = correct;
						}
					} catch (WrongValueType e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		StackedBarRenderer renderer = new StackedBarRenderer();
		renderer.setBarPainter(new StandardBarPainter());
		renderer.setShadowVisible(false);
		for (int i = 0; i < correctlyClassifiedInterval.size(); i++) {
			renderer.setSeriesVisibleInLegend(i, false);
			if (correctlyClassifiedInterval.get(i)) {
				renderer.setSeriesPaint(i, DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE[seriesToWindowSizes.get(i) % DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE.length]);
			} else {
				renderer.setSeriesPaint(i, new Color(0, 0, 255, 0));
			}
		}
		
		JFreeChart chart = ChartFactory.createStackedBarChart(
				session.getModelId(),
				null, // domain axis label
				"Relative time (% modeling session)", // x axis label
				dataset, // data
				PlotOrientation.HORIZONTAL, // the plot orientation
				true, // include legend
				true, // tooltips
				false // urls
				);
		List<Title> subTitles = new ArrayList<Title>();
		subTitles.add(new TextTitle("Expertise: " + session.getSampleClass() + "          Task name: " + session.getTaskName() + "          Min support = " + minSupport));
		chart.setSubtitles(subTitles);

		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
		plot.setRenderer(renderer);
		plot.setDomainGridlinesVisible(true);
		
		plot.getRangeAxis().setRange(0.0, 1.0);
		
		try {
			if (fileName.substring(fileName.length() - 3).equalsIgnoreCase("svg")) {
				ChartUtils.saveChartAsSVG(chart, new File(fileName), 800, 400);
			} else {
				ChartUtilities.saveChartAsPNG(new File(fileName), chart, 800, 400);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
