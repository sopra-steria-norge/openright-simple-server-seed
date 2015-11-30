package net.openright.infrastructure.db;

public class DBFunctions {
    private static DBFunctions _instance;

    public static DBFunctions instance() {
        if (_instance == null) {
            _instance = new DBFunctions();
        }
        return _instance;
    }

    public static void setInstance(DBFunctions instance) {
        DBFunctions._instance = instance;
    }

    public String nextValue(String sequence) {
        return "select nextval('" + sequence + "')";
    }

}
