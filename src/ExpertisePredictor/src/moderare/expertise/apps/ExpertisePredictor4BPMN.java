package moderare.expertise.apps;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.mysql.cj.jdbc.Driver;

public abstract class ExpertisePredictor4BPMN {

	protected static Connection connection;
	protected static String modelFilename = "trained-models/nn.model";
	
	protected static void startup() throws SQLException {
		String name = getCallerClassName();
		System.out.println(">>> " + name + "\n");
		System.out.println("STARTUP PROCEDURES");
		System.out.println("==================\n");
		System.out.print("Connecting to database... ");
		DriverManager.registerDriver(new Driver());
		connection = DriverManager.getConnection("jdbc:mysql://localhost/visual-data?useSSL=false", "test", "test");
		System.out.println("OK");
	}
	
	protected static void end() throws SQLException {
		System.out.print("Diconnecting from database... ");
		connection.close();
		System.out.println("OK");
		System.out.println("Finished, bye!");
	}
	
	public static String getCallerClassName() {
		StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
		String completeClassName = stElements[stElements.length - 1].getClassName();
		return completeClassName.substring(completeClassName.lastIndexOf(".") + 1);
	}
}
