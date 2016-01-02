package mg.util.db.persist.support;

import java.time.LocalDateTime;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.DateTime;
import mg.util.db.persist.annotation.Id;
import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.annotation.VarChar;

// A test class for DBOTest
@Table(name = "contacts4")
public class Contact4 extends Persistable {

    @DateTime
    private LocalDateTime dateOfBirth = LocalDateTime.now();

    @VarChar
    private String email = "";

    @Id
    private long id = 0;

    @Id
    private long id2 = 0;

    @VarChar
    private String name = "";

    @VarChar(length = "20")
    private String phone = "";

    public Contact4(int id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public LocalDateTime getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public int getId() {
        return (int)id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public void setDateOfBirth(LocalDateTime dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
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
