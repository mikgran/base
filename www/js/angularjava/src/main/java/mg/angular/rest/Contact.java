package mg.angular.rest;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.json.JSONException;
import org.json.JSONObject;

@XmlRootElement
public class Contact implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(name = "email")
    private String email = "e@mail.com";

    @XmlElement(name = "id")
    private Long id = 0L;

    @XmlElement(name = "name")
    private String name = "test";

    @XmlElement(name = "phone")
    private String phone = "1111";

    public Contact() {
    }

    public Contact(mg.angular.db.Contact contact) {
        super();

        this.name = contact.getName();
        this.email = contact.getEmail();
        this.phone = contact.getPhone();
        this.id = contact.getId();
    }

    public Contact(String name, String email, String phone, Long id) {
        super();
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.setId(id);
    }

    public String getEmail() {
        return email;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        try {
            // format: { "name": "test" }
            return new JSONObject().put("name", name)
                                   .put("email", email)
                                   .put("phone", phone)
                                   .put("id", id)
                                   .toString();
        } catch (JSONException e) {
            return null;
        }
    }

}