package mg.angular.rest;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.json.JSONException;
import org.json.JSONObject;

//@XmlRootElement
public class Contact {

    //@XmlElement
    String email = "e@mail.com";

    //@XmlElement
    Long id = 0L;

    //@XmlElement
    String name = "test";

    //@XmlElement
    String phone = "1111";

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
        this.id = id;
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