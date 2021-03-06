package mg.util.db.persist.support;

import static java.lang.String.format;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.ForeignKey;
import mg.util.db.persist.annotation.Id;
import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.annotation.VarChar;

@Table(name = "locations4")
public class Location4 extends Persistable {

    @Id
    private long id = 0;

    @VarChar
    private String location = "";

    @ForeignKey(references = "persons4", field = "id")
    private long personsId = 0;

    public Location4() {
    }

    public Location4(String location) {
        this.setTodo(location);
    }

    public long getId() {
        return id;
    }

    public String getTodo() {
        return location;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTodo(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return format("%s('%s', '%s', '%s')", getClass().getSimpleName(), id, location, personsId);
    }

}
