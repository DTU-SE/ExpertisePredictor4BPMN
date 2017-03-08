package moderare.expertise.model;

import java.sql.Connection;
import java.sql.SQLException;

import moderare.expertise.exceptions.PredictorException;

public class ModelingSession extends DatabaseDataset {

	private static final long serialVersionUID = 1466165208145201886L;
	private String model_id;
	
	public ModelingSession(Connection connection) {
		super(connection);
	}
	
	public String getModelId() {
		return model_id;
	}
	
	public void loadFromDatabase(String model_id) throws SQLException, PredictorException {
		this.model_id = model_id;
		clear();
		load(constructQuery(false, null, null, 0.0, "model_id = \"" + model_id + "\"", -1, "modeling_time ASC"));
	}
}
