package mg.angular.db;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.Id;
import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.annotation.VarChar;

@Table(name = "contacts")
public class Contact extends Persistable {

    @VarChar
    private String email = "";

    @Id
    private Long id; // interchangeability via Long vs long and nulls cases. -> add to field builder

    @VarChar
    private String name = "";

    @VarChar
    private String phone = "";

    public Contact() {
    }

    public Contact(Long id, String name, String email, String phone) {
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

    public Contact setEmail(String email) {
        this.email = email;
        return this;
    }

    public Contact setId(Long id) {
        this.id = id;
        return this;
    }

    public Contact setName(String name) {
        this.name = name;
        return this;
    }

    public Contact setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    @Override
    public String toString() {

        return new StringBuilder().append("('")
                                  .append(id)
                                  .append("', '")
                                  .append(name)
                                  .append("', '")
                                  .append(email)
                                  .append("', '")
                                  .append(phone)
                                  .append("')")
                                  .toString();
    }
}
