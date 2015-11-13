package mg.util.db.dbo;

import mg.util.db.dbo.annotation.Table;
import mg.util.db.dbo.annotation.VarChar;

// A test class for DBOTest
@Table(name = "contacts")
public class Contact {

    @VarChar(length = "40")
    private String name = "";

    @VarChar(length = "40")
    private String email = "";

    @VarChar(length = "20")
    private String phone = "";

    public Contact(String name, String email, String phone) {
        super();
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
