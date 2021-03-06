package eu.trentorise.smartcampus.ifame.model;

import java.io.Serializable;

public class Piatto implements Serializable {

	private Long piatto_id;

	private String piatto_nome;

	private String piatto_kcal;

	public Piatto() {
		super();
	}

	public Piatto(String piatto_nome, String piatto_kcal) {
		super();
		this.piatto_nome = piatto_nome;
		this.piatto_kcal = piatto_kcal;
	}

	public Long getPiatto_id() {
		return piatto_id;
	}

	public void setPiatto_id(Long piatto_id) {
		this.piatto_id = piatto_id;
	}

	public String getPiatto_nome() {
		return piatto_nome;
	}

	public void setPiatto_nome(String piatto_nome) {
		this.piatto_nome = piatto_nome;
	}

	public String getPiatto_kcal() {
		return piatto_kcal;
	}

	public void setPiatto_kcal(String piatto_kcal) {
		this.piatto_kcal = piatto_kcal;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((piatto_nome == null) ? 0 : piatto_nome.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Piatto other = (Piatto) obj;
		if (piatto_nome == null) {
			if (other.piatto_nome != null)
				return false;
		} else if (!piatto_nome.equals(other.piatto_nome))
			return false;
		return true;
	}

}
