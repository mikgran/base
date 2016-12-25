package mg.util.db.persist.support;

import java.sql.Connection;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.Id;
import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.annotation.VarChar;

// A test class for DBOTest
@Table(name = "contacts6")
public class Contact6 extends Persistable {

    @VarChar
    private String email = "";

    @Id
    private long id = 0;

    @VarChar
    private String name = "";

    @VarChar(length = "20")
    private String phone = "";

    public Contact6() {
    }

    public Contact6(Connection connection) {
        super(connection);
    }

    public Contact6(Connection connection, long id, String name, String email, String phone) {
        super(connection);
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public long getId() {
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

    public void setId(long id) {
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
