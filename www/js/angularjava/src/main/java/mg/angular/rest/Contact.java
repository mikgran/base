package mg.angular.rest;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.json.JSONException;
import org.json.JSONObject;

@XmlRootElement
public class Contact implements Serializable {

	@XmlElement(name="name")
	private String name = "test";
	
	@XmlElement(name="email")
	private String email = "e@mail.com";
	
	@XmlElement(name="number")
	private String number = "1111";
	
	public Contact(String name, String email, String number) {
		super();
		this.name = name;
		this.email = email;
		this.number = number;
	}
	
	private static final long serialVersionUID = 1L;
	
	public Contact() {
	}

	@Override
    public String toString() {
        try {
            // format: { "name":"test" }
            return new JSONObject().put("name", name).put("email", email).put("number", number).toString();
        } catch (JSONException e) {
            return null;
        }
    }
	
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

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}
	
}