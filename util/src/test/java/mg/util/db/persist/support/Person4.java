package mg.util.db.persist.support;

import static java.lang.String.format;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.Id;
import mg.util.db.persist.annotation.OneToOne;
import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.annotation.VarChar;

// very cut down persons
@Table(name = "persons4")
public class Person4 extends Persistable {

    @OneToOne
    private Address address = new Address();

    @VarChar
    private String firstName = "";

    @Id
    private long id = 0;

    @VarChar
    private String lastName = "";

    public Person4() {
        super();
    }

    public Person4(Address address, String firstName, String lastName) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
    }

    public Person4(String firstName, String lastName) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Address getAddress() {
        return address;
    }

    public String getFirstName() {
        return firstName;
    }

    public long getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return format("%s('%s', '%s', '%s', '%s')", getClass().getSimpleName(), id, firstName, lastName, address);
    }
}
