package eu.trentorise.smartcampus.ifame.model;

import java.util.List;

public class MenuDelMese {

	private List<MenuDellaSettimana> menuDellaSettimana;
	private int start_day;
	private int end_day;

	public List<MenuDellaSettimana> getMenuDellaSettimana() {
		return menuDellaSettimana;
	}

	public void setMenuDellaSettimana(
			List<MenuDellaSettimana> menuDellaSettimana) {
		this.menuDellaSettimana = menuDellaSettimana;
	}

	public int getStart_day() {
		return start_day;
	}

	public void setStart_day(int start_day) {
		this.start_day = start_day;
	}

	public int getEnd_day() {
		return end_day;
	}

	public void setEnd_day(int end_day) {
		this.end_day = end_day;
	}

}
