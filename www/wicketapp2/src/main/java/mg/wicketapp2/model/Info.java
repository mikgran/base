package mg.wicketapp2.model;

import java.io.Serializable;
import java.util.Date;

public class Info implements Serializable {

	private static final long serialVersionUID = 2912952240485330110L;
	private String name = "";
	private String email = "";
	private String street = "";
	private String zipCode = "";
	private String town = "";
	private Date date;
	private Integer slider = 1;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getTown() {
		return town;
	}

	public void setTown(String town) {
		this.town = town;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Integer getSlider() {
		return slider;
	}

	public void setSlider(Integer slider) {
		this.slider = slider;
	}

}
