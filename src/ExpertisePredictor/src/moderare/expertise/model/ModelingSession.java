package moderare.expertise.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

import moderare.expertise.exceptions.PredictorException;
import moderare.expertise.exceptions.WrongValueType;

public class ModelingSession extends DatabaseDataset {

	private static final long serialVersionUID = 1466165208145201886L;
	private String model_id;
	
	public ModelingSession(Connection connection) {
		super(connection);
	}
	
	public void loadFromDatabase(String model_id) throws SQLException, PredictorException {
		this.model_id = model_id;
		clear();
		load(constructQuery(false, null, null, null, 0.0, Collections.singletonList("model_id = \"" + model_id + "\""), -1, "modeling_time ASC"));
	}
	
	public String getModelId() {
		return model_id;
	}
	
	public EXPERTISE getSampleClass() {
		if (!isEmpty()) {
			return getFirst().getSampleClass();
		}
		return null;
	}
	
	public String getTaskName() {
		if (!isEmpty()) {
			try {
				return getFirst().getString("task");
			} catch (WrongValueType e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
