package mg.util.db.persist.support;

import static java.lang.String.format;
import static mg.util.validation.Validator.validateNotNull;

import java.util.ArrayList;
import java.util.List;

import mg.util.db.persist.Persistable;
import mg.util.db.persist.annotation.ForeignKey;
import mg.util.db.persist.annotation.Id;
import mg.util.db.persist.annotation.OneToMany;
import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.annotation.VarChar;

@Table(name = "todos3")
public class Todo3 extends Persistable {

    @Id
    private long id = 0;

    @OneToMany
    private List<Location3> locations = new ArrayList<>();

    @ForeignKey(references = "persons3", field = "id")
    private long personsId = 0;

    @VarChar
    private String todo = "";

    public Todo3() {
    }

    public Todo3(String todo) {
        this.todo = todo;
    }

    public Todo3(String todo, List<Location3> locations) {
        this.todo = todo;
        this.locations = locations;
    }

    public Todo3(String todo, long personsId) {
        this.todo = todo;
        this.personsId = personsId;
    }

    public long getId() {
        return id;
    }

    public List<Location3> getLocations() {
        return locations;
    }

    public long getPersonsId() {
        return personsId;
    }

    public String getTodo() {
        return todo;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTodo(String todo) {
        this.todo = validateNotNull("todo", todo);
    }

    @Override
    public String toString() {
        return format("%s(id: '%s', todo: '%s', locations: %s)", getClass().getSimpleName(), id, todo, locations);
    }

}
