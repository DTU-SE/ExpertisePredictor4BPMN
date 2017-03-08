package moderare.expertise.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

import moderare.expertise.exceptions.PredictorException;
import moderare.expertise.utils.DatabaseUtils;

public class DatabaseDataset extends Dataset {

	private static final long serialVersionUID = 6658264672047146043L;
	private Connection connection;
	
	public DatabaseDataset(Connection connection) {
		this.connection = connection;
	}
	
	public void addFromDatabase(boolean attributesNotNull, String specificTask, EXPERTISE specificExpertise, double minRelativeModelingTime, int numberOfCases, String orderBy) {
		try {
			load(constructQuery(attributesNotNull, specificTask, specificExpertise, minRelativeModelingTime, null, numberOfCases, orderBy));
		} catch (SQLException | PredictorException e) {
			e.printStackTrace();
		}
	}
	
	protected String constructQuery(boolean attributesNotNull, String specificTask, EXPERTISE specificExpertise,
			double minRelativeModelingTime, String additionaWhereConditions, int numberOfCases, String orderBy) {
		String query = "SELECT ";
		query += StringUtils.join(ModelSample.attributeTypes.keySet(), ", ");
		query += " FROM metrics_evolution WHERE 1=1";
		if (attributesNotNull) {
			query += " AND ((" + StringUtils.join(ModelSample.attributeTypes.keySet(), " IS NOT NULL) AND (") + " IS NOT NULL))";
		}
		if (specificTask != null) {
			query += " AND (task = \""+ specificTask +"\")";
		}
		if (specificExpertise != null) {
			query += " AND (expertise = \""+ specificExpertise.toString() +"\")";
		}
		if (minRelativeModelingTime >= 0) {
			query += " AND (relative_modeling_time >= "+ minRelativeModelingTime +")";
		}
		
		if (additionaWhereConditions != null) {
			query += " AND (" + additionaWhereConditions + ")";
		}
		
		if (orderBy != null) {
			query += " ORDER BY " + orderBy;
		}
		if (numberOfCases >= 0) {
			query += " LIMIT " + numberOfCases;
		}
		return query;
	}
	
	protected void load(String query) throws SQLException, PredictorException {
		ResultSet resultSet = connection.createStatement().executeQuery(query);
		while (resultSet.next()) {
			add(new ModelSample(DatabaseUtils.map(resultSet)));
		}
	}
}
