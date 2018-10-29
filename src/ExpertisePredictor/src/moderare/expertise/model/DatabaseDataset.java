package moderare.expertise.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import moderare.expertise.exceptions.PredictorException;
import moderare.expertise.utils.DatabaseUtils;

public class DatabaseDataset extends Dataset {

	private static final long serialVersionUID = 6658264672047146043L;
	private Connection connection;
	
	public DatabaseDataset(Connection connection) {
		this.connection = connection;
	}
	
	public void addFromDatabase(
			boolean attributesNotNull,
			Collection<String> specificTask,
			Collection<String> specificExperiment,
			Collection<EXPERTISE> specificExpertise,
			double minRelativeModelingTime,
			int numberOfCases,
			String orderBy) {
		try {
			load(constructQuery(attributesNotNull, specificTask, specificExperiment, specificExpertise, minRelativeModelingTime, null, numberOfCases, orderBy));
		} catch (SQLException | PredictorException e) {
			e.printStackTrace();
		}
	}
	
	public void addFromDatabase(
			boolean attributesNotNull,
			Collection<String> specificTask,
			Collection<String> specificExperiment,
			Collection<EXPERTISE> specificExpertise,
			Collection<String> additionalWhereConditions,
			double minRelativeModelingTime,
			int numberOfCases,
			String orderBy) {
		try {
			load(constructQuery(attributesNotNull, specificTask, specificExperiment, specificExpertise, minRelativeModelingTime, additionalWhereConditions, numberOfCases, orderBy));
		} catch (SQLException | PredictorException e) {
			e.printStackTrace();
		}
	}
	
	public static String constructQuery(Collection<String> attributes, Collection<String> whereConditions, int numberOfCases, String orderBy) {
		String query = "SELECT ";
		query += StringUtils.join(attributes, ", ");
		query += " FROM metrics_evolution WHERE 1=1";
		for(String condition : whereConditions) {
			query += " AND (" + condition + ")";
		}
		if (orderBy != null) {
			query += " ORDER BY " + orderBy;
		}
		if (numberOfCases >= 0) {
			query += " LIMIT " + numberOfCases;
		}
		return query;
	}
	
	protected String constructQuery(boolean attributesNotNull, Collection<String> specificTasks, Collection<String> specificExperiments, Collection<EXPERTISE> specificExpertises,
			double minRelativeModelingTime, Collection<String> additionaWhereConditions, int numberOfCases, String orderBy) {
		Set<String> whereConditions = new HashSet<String>();
		
		if (attributesNotNull) {
			for(String attribute : ModelSample.attributes.keySet()) {
				whereConditions.add(attribute + " IS NOT NULL");
			}
		}
		
		if (additionaWhereConditions != null) {
			whereConditions.addAll(additionaWhereConditions);
		}
		
		if (specificTasks != null) {
			String specificTaskQuery = "(1=0)";
			for (String task : specificTasks) {
				specificTaskQuery += " OR (task = \""+ task +"\")";
			}
			whereConditions.add(specificTaskQuery);
		}
		if (specificExperiments != null) {
			String specificExperimentQuery = "(1=0)";
			for (String experiment : specificExperiments) {
				specificExperimentQuery += " OR (experiment = \""+ experiment +"\")";
			}
			whereConditions.add(specificExperimentQuery);
		}
		if (specificExpertises != null) {
			String specificExpertiseQuery = "(1=0)";
			for (EXPERTISE expertise : specificExpertises) {
				specificExpertiseQuery += " OR (expertise = \""+ expertise.toString() +"\")";
			}
			whereConditions.add(specificExpertiseQuery);
		}
		if (minRelativeModelingTime >= 0) {
			whereConditions.add("relative_modeling_time >= "+ minRelativeModelingTime);
		}
		
		return constructQuery(ModelSample.attributeTypes.keySet(), whereConditions, numberOfCases, orderBy);
	}
	
	protected void load(String query) throws SQLException, PredictorException {
		ResultSet resultSet = connection.createStatement().executeQuery(query);
		while (resultSet.next()) {
			add(new ModelSample(DatabaseUtils.map(resultSet)));
		}
	}
}
