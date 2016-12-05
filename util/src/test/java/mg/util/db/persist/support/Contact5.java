package mg.util.db.persist.support;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.Id;
import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.annotation.VarChar;

// A test class for DBOTest
@Table(name = "contacts5")
public class Contact5 extends Persistable {

    @VarChar
    private String email = "";

    @Id
    private Long id = null;

    @VarChar
    private String name = "";

    @VarChar(length = "20")
    private String phone = "";

    public Contact5() {
    }

    public Contact5(Long id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public long getId2() {
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

        return new StringBuilder().append("(")
                                  .append(id)
                                  .append(", ")
                                  .append(name)
                                  .append(", ")
                                  .append(email)
                                  .append(", ")
                                  .append(phone)
                                  .append(")")
                                  .toString();
    }
}
