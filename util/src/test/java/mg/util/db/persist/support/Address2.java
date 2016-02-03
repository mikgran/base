package mg.util.db.persist.support;

import static java.lang.String.format;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.ForeignKey;
import mg.util.db.persist.annotation.Id;
import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.annotation.VarChar;

@Table(name = "addresses2")
public class Address2 extends Persistable {

    @VarChar
    private String address = "";

    @Id
    private long id = 0;

    @ForeignKey(references = "persons5", field = "id")
    private long personsId = 0;

    public Address2() {
    }

    public Address2(String address) {
        this.address = address;
    }

    public String getAddress2() {
        return address;
    }

    public long getId() {
        return id;
    }

    public long getPersonsId() {
        return personsId;
    }

    public void setAddress2(String address) {
        this.address = address;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setPersonsId(long personsId) {
        this.personsId = personsId;
    }

    @Override
    public String toString() {
        return format("%s('%s', '%s', '%s')", getClass().getSimpleName(), id, personsId, address);
    }
}
