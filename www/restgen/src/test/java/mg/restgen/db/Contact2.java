
package mg.restgen.db;

import java.sql.Connection;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.Id;
import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.annotation.VarChar;

@Table(name = "contacts2")
public class Contact2 extends Persistable {

    @VarChar
    private String email = "";

    @Id
    private Long id; // TOIMPROVE: interchangeability via Long vs long and nulls cases. -> add to field builder

    @VarChar
    private String name = "";

    @VarChar
    private String phone = "";

    public Contact2() {
    }

    public Contact2(Connection connection) {
        super(connection);
    }

    public Contact2(Long id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public Contact2 setEmail(String email) {
        this.email = email;
        return this;
    }

    public Contact2 setId(Long id) {
        this.id = id;
        return this;
    }

    public Contact2 setName(String name) {
        this.name = name;
        return this;
    }

    public Contact2 setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    @Override
    public String toString() {

        return new StringBuilder().append("Contact(id:'")
                                  .append(id)
                                  .append("', name:'")
                                  .append(name)
                                  .append("', email:'")
                                  .append(email)
                                  .append("', phone:'")
                                  .append(phone)
                                  .append("')")
                                  .toString();
    }
}
