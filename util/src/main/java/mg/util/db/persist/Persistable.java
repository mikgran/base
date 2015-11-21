package mg.util.db.persist;

public abstract class Persistable {

    protected int id = 0;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
