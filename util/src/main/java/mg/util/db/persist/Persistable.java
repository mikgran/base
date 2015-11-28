package mg.util.db.persist;

public abstract class Persistable {

    protected int id = 0;

    /**
     * The id of this data object. Any above zero ids mean that the object has
     * been loaded from the database.
     * 
     * @return the id corresponding this records primary key.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id of this record. Changing a loaded objects id causes another
     * record to be overridden via save().
     * 
     * @param id
     *            the new id for this object.
     */
    public void setId(int id) {
        this.id = id;
    }
}
