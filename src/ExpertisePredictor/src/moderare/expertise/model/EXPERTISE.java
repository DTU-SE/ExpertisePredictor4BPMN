package moderare.expertise.model;

import java.util.ArrayList;
import java.util.List;

public enum EXPERTISE {
	EXPERT("expert"),
	NOVICE("novice");

	private static List<String> names = null;
	private final String text;

	private EXPERTISE(String text) {
		this.text = text;
	}
	
	public static List<String> names() {
		if (names == null) {
			names = new ArrayList<String>();
			for(EXPERTISE e : values()) {
				names.add(e.toString());
			}
		}
		return names;
	}
	
	@Override
	public String toString() {
		return text;
	}
}
