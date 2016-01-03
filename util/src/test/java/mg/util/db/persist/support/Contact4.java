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

    @Id(autoincrement = false)
    private long id2 = 0;

    @VarChar
    private String name = "";

    @VarChar(length = "20")
    private String phone = "";

    public Contact4() {
    }

    public Contact4(long id, long id2, String name, String email, String phone) {
        this.id = id;
        this.id2 = id2;
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

    public long getId() {
        return id;
    }

    public long getId2() {
        return id2;
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

    public void setId(long id) {
        this.id = id;
    }

    public void setId2(long id2) {
        this.id2 = id2;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
