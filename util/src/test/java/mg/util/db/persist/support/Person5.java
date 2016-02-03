package mg.util.db.persist.support;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.Id;
import mg.util.db.persist.annotation.OneToMany;
import mg.util.db.persist.annotation.OneToOne;
import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.annotation.VarChar;

// very cut down persons
@Table(name = "persons5")
public class Person5 extends Persistable {

    @OneToOne
    private Address2 address;

    @VarChar
    private String firstName = "";

    @Id
    private long id = 0;

    @VarChar
    private String lastName = "";

    @OneToMany
    private List<Location5> locations = new ArrayList<>();

    public Person5() {
        super();
    }

    public Person5(Address2 address, String firstName, String lastName, List<Location5> locations) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.locations = locations;
    }

    public Person5(String firstName, String lastName) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Address2 getAddress() {
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

    public List<Location5> getLocations() {
        return locations;
    }

    public void setAddress(Address2 address) {
        this.address = address;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setLocations(List<Location5> locations) {
        this.locations = locations;
    }

    @Override
    public String toString() {
        return format("%s('%s', '%s', '%s', '%s', '%s')", getClass().getSimpleName(), id, firstName, lastName, address != null ? address : "", locations);
    }
}
