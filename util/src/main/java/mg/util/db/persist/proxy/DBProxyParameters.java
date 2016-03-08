package mg.util.db.persist.proxy;

import mg.util.db.persist.DB;
import mg.util.db.persist.Persistable;

public class DBProxyParameters<T> {

    public final DB db;
    public final boolean fetched;
    public final String listPopulationSql;
    public final Persistable refPersistable;
    public final T type;

    public DBProxyParameters(DB db, T type, String populationSql, Persistable refPersistable) {
        this.db = db;
        this.type = type;
        this.listPopulationSql = populationSql;
        this.refPersistable = refPersistable;
        this.fetched = false;
    }

    public DBProxyParameters(DBProxyParameters<T> parameters) {
        this.db = parameters.db;
        this.fetched = parameters.fetched;
        this.type = parameters.type;
        this.listPopulationSql = parameters.listPopulationSql;
        this.refPersistable = parameters.refPersistable;
    }

    public DBProxyParameters(DBProxyParameters<T> parameters, boolean fetched) {
        this.db = parameters.db;
        this.fetched = fetched;
        this.type = parameters.type;
        this.listPopulationSql = parameters.listPopulationSql;
        this.refPersistable = parameters.refPersistable;
    }


    @SuppressWarnings("unused")
    private DBProxyParameters() {
        this.db = null;
        this.type = null;
        this.listPopulationSql = "";
        this.refPersistable = null;
        this.fetched = false;
    }
}
