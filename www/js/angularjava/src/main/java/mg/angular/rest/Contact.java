package mg.angular.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Contact {

    private String email = "e@mail.com";
    private Long id = 0L;
    private String name = "test";
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
        this.id = id;
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

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(this);

        } catch (JsonProcessingException e) {
            return null;
        }
    }

}