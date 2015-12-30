package mg.angular.db;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.Id;
import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.annotation.VarChar;

@Table(name = "contacts")
public class Contact extends Persistable {

    @VarChar(length="40")
    private String email = "";

    @Id
    private int id;

    @VarChar(length="40")
    private String name = "";

    @VarChar(length="20")
    private String phone = "";

    public Contact(int id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    @SuppressWarnings("unused")
    private Contact() {
    }

    public String getEmail() {
        return email;
    }

    @Override
    public int getId() {
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

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
