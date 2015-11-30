package mg.util.db.persist;

import static java.lang.String.format;

import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.annotation.VarChar;

@Table(name = "locations")
public class Location extends Persistable {

    @VarChar
    private String location = "";

    public Location(String location) {
        this.setTodo(location);
    }

    public String getTodo() {
        return location;
    }

    public void setTodo(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return format("[id: '%s', todo: '%s']", id, location);
    }

}
