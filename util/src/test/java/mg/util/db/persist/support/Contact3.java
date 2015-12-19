package mg.util.db.persist.support;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.annotation.VarChar;

// A test class for DBOTest
@Table(name = "contacts3")
public class Contact3 extends Persistable {

    @VarChar
    private String email = "";

    @VarChar
    private String name = "";

    @VarChar(length = "20")
    private String phone = "";

    public Contact3(int id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getEmail() {
        return email;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
