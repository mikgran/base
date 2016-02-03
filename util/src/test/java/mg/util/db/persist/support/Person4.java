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

    @OneToMany
    private List<Location4> locations = new ArrayList<>();

    public Person4() {
        super();
    }

    public Person4(Address address, String firstName, String lastName, List<Location4> locations) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.locations = locations;
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

    public List<Location4> getLocations() {
        return locations;
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

    public void setId(long id) {
        this.id = id;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setLocations(List<Location4> locations) {
        this.locations = locations;
    }

    @Override
    public String toString() {
        return format("%s('%s', '%s', '%s', '%s', '%s')", getClass().getSimpleName(), id, firstName, lastName, address, locations);
    }
}
