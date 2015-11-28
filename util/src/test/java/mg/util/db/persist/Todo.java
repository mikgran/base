package mg.util.db.persist;

import mg.util.db.persist.annotation.Table;
import mg.util.db.persist.annotation.VarChar;

@Table(name = "todos")
public class Todo extends Persistable {

    // @ForeignKey(references="Person")
    // private int id = 0;

    public Todo(String todo) {
        this.todo = todo;
    }

    @VarChar
    private String todo = "";
}
