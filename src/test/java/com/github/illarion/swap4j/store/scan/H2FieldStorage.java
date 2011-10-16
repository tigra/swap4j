package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.swap.ProxyList;
import com.github.illarion.swap4j.swap.Swap;
import com.github.illarion.swap4j.swap.UUIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * TODO Describe class
 *
 * @author Alexey Tigarev tigra@agile-algorithms.com
 */
public class H2FieldStorage implements FieldStorage, UUIDGenerator {
    private static final String CREATE_TABLE_FIELDS
            = "CREATE TABLE IF NOT EXISTS Fields "
            + "(id VARCHAR(80), "
            + "path VARCHAR(100), "
            + "value VARCHAR(1000), "
            + "type TINYINT, "
            + "class VARCHAR(100), "
            + "elementClass VARCHAR(100),"
            + " PRIMARY KEY(id, path));";

    private Connection connection;
    private Swap swap;
    private int currentUuid = 0;
    
    private final static Logger log = LoggerFactory.getLogger("H2FieldStorage");

    public H2FieldStorage() throws ClassNotFoundException, SQLException {
        this(null);
    }

    public H2FieldStorage(Swap swap) throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        connection = DriverManager.getConnection("jdbc:h2:tcp://localhost/~/test");
        initDatabase();
        this.swap = swap;
    }

    @Override
    public long getRecordCount() {
        try {
            Statement statement = createStatement();
            ResultSet rs = statement.executeQuery("select count(*) as cnt from fields ");
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private Statement createStatement() throws SQLException {
        synchronized (connection) {
            checkIfConnectionAlive();
            return connection.createStatement();
        }
    }

    @Override
    public UUID createUUID() {
        try {
            while (uuidPresent(currentUuid)) {
                currentUuid++;
            }
            return new UUID(0, currentUuid);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean uuidPresent(int currentUuid) throws SQLException {
        Statement statement = createStatement();
        String uuidStr = new UUID(0, currentUuid).toString();
        ResultSet rs = statement.executeQuery("select id from FIELDS where id='" + uuidStr + "'");
        return rs.next();
//        return !rs.isAfterLast();
    }

    @Override
    public void setSwap(Swap swap) {
        this.swap = swap;
    }

    private void initDatabase() throws SQLException {
        Statement statement = createStatement();
        statement.executeUpdate(CREATE_TABLE_FIELDS);
    }

    /**
     * Remove all fields of object identified by given <code>uuid</code>
     *
     * @param uuid UUID of object to be deleted
     * @return <code>true</code> if something deleted, <code>false</code> otherwise
     */
    @Override
    public boolean clean(UUID uuid) {
        log.debug("clean(" + ID.shortRepresentation(uuid) + ")");
        try {
            Statement statement = createStatement();   // TODO log count of existing fields
            int result = statement.executeUpdate("delete from FIELDS " + whereClause(uuid));
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String whereClause(UUID uuid) {
        return "where id='" + uuid.toString() + "'";
    }

    private String whereClause(Locator locator) {
        return " where id='" + locator.getId().toString() + "'"
                + " and path='" + locator.getPath() + "'";
    }

    @Override
    public void serialize(FieldRecord representation) {
        log.debug("serialize(" + representation + ")");
        synchronized (connection) {
            checkIfConnectionAlive();
            try {
                Statement statement = createStatement();
                Locator locator = representation.getLocator();
                ResultSet rs = statement.executeQuery("select count(*) as cnt from fields "
                        + whereClause(locator));
                rs.next();
                if (rs.getInt(1) == 0) {
                    PreparedStatement preparedStatement = prepareStatement("INSERT INTO FIELDS VALUES (?, ?, ?, ?, ?, ?)");
                    preparedStatement.setString(1, representation.getIdString());
                    preparedStatement.setString(2, representation.getPath());
                    preparedStatement.setString(3, representation.getValueString());
                    preparedStatement.setInt(4, representation.getTypeOrdinal());
                    preparedStatement.setString(5, representation.getClassName());
                    preparedStatement.setString(6, representation.getElementClassName());
                    preparedStatement.addBatch();
                    int[] updateCounts = preparedStatement.executeBatch();
                } else {
                    PreparedStatement preparedStatement = prepareStatement(
                            "UPDATE FIELDS SET value=?, type=?, class=?, elementClass=? WHERE id='"
                                    + representation.getIdString() + "' and path='" + representation.getPath() + "'");
                    preparedStatement.setString(1, representation.getValueString());
                    preparedStatement.setInt(2, representation.getTypeOrdinal());
                    preparedStatement.setString(3, representation.getClassName());
                    preparedStatement.setString(4, representation.getElementClassName());
//                preparedStatement.setString(5, representation.getIdString());
//                preparedStatement.setString(5, representation.getPath());
                    preparedStatement.addBatch();
                    int[] updateCounts = preparedStatement.executeBatch();
                }
                commit();
            } catch (SQLException e) {
//            log.error("", e);
                e.printStackTrace();
            }
        }
    }

    private void commit() throws SQLException {
        synchronized (connection) {
            checkIfConnectionAlive();
            connection.commit();
            connection.setAutoCommit(true);
        }
    }

    private PreparedStatement prepareStatement(String query) throws SQLException {
        synchronized (connection) {
            checkIfConnectionAlive();
            return connection.prepareStatement(query);
        }
    }

    private void checkIfConnectionAlive() {
        synchronized (connection) {
            if (null == connection) {
                throw new IllegalStateException("Can't store anything, connection to database lost");
            }
        }
    }

    @Override
    public FieldRecord read(Locator locator) {
        log.debug("read(" + locator + ")");
        try {
            Statement statement = createStatement();
            ResultSet rs = statement.executeQuery("select * from FIELDS " + whereClause(locator));
//                    + " where id='" + locator.getId().toString() + "'"
//                    + " and path='" + locator.getPath() + "'");
            if (!rs.next()) {
                return null;
//                throw new StoreException("Not found");
            }
            return readSerializedFieldFromResultSet(rs);
//            return rsToSF(locator, rs);
        } catch (SQLException e) {
            e.printStackTrace();
            return null; // TODO own exception
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null; // TODO own exception
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null; // TODO own exception
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null; // TODO own exception
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null; // TODO own exception
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null; // TODO own exception
        } catch (StoreException e) {
            e.printStackTrace();
            return null; // TODO own exception
        }
    }

    /**
     * Read all fields of object identified by given <code>uuid</code>.
     * Fields are returned in such order that will allow to recreate it in that order.
     * I.e. "./fieldA" will always be before "./fieldA/field1".
     *
     * @param uuid UUID identifying object
     * @return all fields of object in order that allow to recreate it
     */
    @Override
    public List<FieldRecord> readAll(UUID uuid) {
        log.debug("readAll(" + ID.shortRepresentation(uuid) + ")");
        try {
            List<FieldRecord> fieldRecords = readFieldRecordsSelectedByQuery(
                    "select id,path,value,class,elementClass,type from FIELDS " + whereClause(uuid));
            log.debug("read: " + fieldRecords);
            return fieldRecords;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null; // TODO own exception
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null; // TODO own exception
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null; // TODO own exception
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null; // TODO own exception
        } catch (StoreException e) {
            e.printStackTrace();
            return null; // TODO own exception
        }
    }

    private List<FieldRecord> readFieldRecordsSelectedByQuery(String query) throws SQLException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, StoreException {
        Statement statement = createStatement();
        ResultSet rs = statement.executeQuery(query);
        List<FieldRecord> fieldRecords = readFieldRecordsFromResultSet(rs);
        return fieldRecords;
    }

    private List<FieldRecord> readFieldRecordsFromResultSet(ResultSet rs) throws SQLException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, StoreException {
        List<FieldRecord> fieldRecords = new ArrayList<FieldRecord>();
        while (rs.next()) {
            if (!rs.isAfterLast()) {
                fieldRecords.add(readSerializedFieldFromResultSet(rs));
            }
        }
        return fieldRecords;
    }

    private Class<?> getClassFromResultSet(ResultSet rs, String fieldName) throws ClassNotFoundException, SQLException {
        String className = rs.getString(fieldName);
        return null == className ? null : Class.forName(className);
    }

    private FieldRecord readSerializedFieldFromResultSet(ResultSet rs) throws SQLException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, StoreException {
        UUID uuid = UUID.fromString(rs.getString("id"));
        FieldRecordBuilder builder = new FieldRecordBuilder(uuid, rs.getString("path"));
        Class<?> clazz = getClassFromResultSet(rs, "class");
        RECORD_TYPE recordType = RECORD_TYPE.values()[rs.getInt("type")];
        Object value = valueFromString(rs.getString("value"), clazz, null, uuid, recordType);

        builder.setValue(value);
        builder.setClazz(clazz);
        builder.setElementClass(getClassFromResultSet(rs, "elementClass"));
        builder.setRecordType(recordType);
        return builder.create();
    }

    private Object valueFromString(String string, Class clazz, Class<Object> elementClass, UUID uuid, RECORD_TYPE recordType) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, StoreException {
        if (String.class.equals(clazz)) {
            return string;
        }
        if (ProxyList.class.isAssignableFrom(clazz)) {
////            List proxyList = swap.newWrapList(elementClass);
//            List proxyList = new ProxyList(swap, elementClass, uuid, Swap.DONT_UNLOAD); // TODO UUID???
//            return proxyList;
            return string; // TODO get this method out somewhere
        } else {
            if (RECORD_TYPE.LIST_ELEMENT.equals(recordType)) {
                return string;
            } else {
                Constructor constructor = clazz.getConstructor();
                return constructor.newInstance();
            }
        }
    }

    @Override
    public Iterator<Locator> iterator() {
        try {
            Statement statement = createStatement();
            final ResultSet rs = statement.executeQuery("SELECT id as id, path as path from FIELDS");
            List<Locator> locators = new ArrayList<Locator>();
            while (!rs.isAfterLast()) {
                rs.next();
                if (!rs.isAfterLast()) {
                    locators.add(new Locator(UUID.fromString(rs.getString("id")), rs.getString("path")));
                }
            }
            return locators.iterator();
//            return new Iterator<Locator>() {
//
//                @Override
//                public boolean hasNext() {
//                    try {
//                        return !rs.isAfterLast();
//                    } catch (SQLException e) {
//                        e.printStackTrace();
//                        return false;
//                    }
//                }
//
//                @Override
//                public Locator next() {
//                    try {
//                        if (rs.isAfterLast()) {
//                            return null;
//                        }
//                        rs.next();
//                        return new Locator(UUID.fromString(rs.getString("id")), rs.getString("path"));
//                    } catch (SQLException e) {
//                        e.printStackTrace();
//                        return null;
//                    }
//                }
//
//                @Override
//                public void remove() {
//                    throw new UnsupportedOperationException(""); // TODO Implement this method
//                }
//            };
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Remove particular field identified by a given <code>locator</code>
     *
     * @param locator which field to remove
     */
    @Override
    public void remove(Locator locator) {
        throw new UnsupportedOperationException(""); // TODO Implement this method

    }

    public void finalize() throws Throwable {
        synchronized (connection) {
            connection.commit();
            connection.close();
            connection = null;
        }
        super.finalize();
    }

    public void cleanAll() throws SQLException {
        Statement statement = createStatement();
        statement.executeUpdate("DELETE from FIELDS");
        commit();
    }

    @Override
    public <T> List<FieldRecord> readElementRecords(UUID uuid, Class<T> elementClass) {
        log.debug("readElementRecords(" + ID.shortRepresentation(uuid) + ", " + elementClass + ")");
        try {
            return readFieldRecordsSelectedByQuery(
                    "select id,path,value,class,elementClass,type from FIELDS "
                            + whereClause(uuid) + " and type=" + RECORD_TYPE.LIST_ELEMENT.ordinal());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (StoreException e) {
            e.printStackTrace();
            return null;
        }
    }
}
