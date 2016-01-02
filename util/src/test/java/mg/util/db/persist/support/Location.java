package mg.util.db.persist.support;

import static java.lang.String.format;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.Id;
import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.annotation.VarChar;

@Table(name = "locations")
public class Location extends Persistable {

    @Id
    private long id = 0;

    @VarChar
    private String location = "";

    public Location() {
    }

    public Location(String location) {
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
        return format("[id: '%s', todo: '%s']", id, location);
    }

}
