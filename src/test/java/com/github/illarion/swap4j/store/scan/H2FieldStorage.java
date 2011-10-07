package com.github.illarion.swap4j.store.scan;

import com.github.illarion.swap4j.store.StoreException;
import com.github.illarion.swap4j.swap.ProxyList;
import com.github.illarion.swap4j.swap.Swap;

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
public class H2FieldStorage implements FieldStorage {
    private static final String CREATE_TABLE_FIELDS
            = "CREATE TABLE IF NOT EXISTS Fields "
                + "(id VARCHAR(80), "
                + "path VARCHAR(100), "
                + "value VARCHAR(200), "
                + "type TINYINT, "
                + "class VARCHAR(100), "
                + "elementClass VARCHAR(100),"
                + " PRIMARY KEY(id, path));";

    private Connection connection;
    private Swap swap;

    public H2FieldStorage() throws ClassNotFoundException, SQLException {
        this(null);
    }

    public H2FieldStorage(Swap swap) throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        connection = DriverManager.getConnection("jdbc:h2:~/test");
        initDatabase();
        this.swap = swap;
    }

    @Override
    public void setSwap(Swap swap) {
        this.swap = swap;
    }

    private void initDatabase() throws SQLException {
        Statement statement = connection.createStatement();
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
        try {
            Statement statement = connection.createStatement();
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
        try {
            Statement statement = connection.createStatement();
            Locator locator = representation.getLocator();
            ResultSet rs = statement.executeQuery("select count(*) as cnt from fields "
                    + whereClause(locator));
            rs.next();
            if (rs.getInt(1) == 0) {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "INSERT INTO FIELDS VALUES (?, ?, ?, ?, ?, ?)");
                preparedStatement.setString(1, representation.getIdString());
                preparedStatement.setString(2, representation.getPath());
                preparedStatement.setString(3, representation.getValueString());
                preparedStatement.setInt(4, representation.getTypeOrdinal());
                preparedStatement.setString(5, representation.getClassName());
                preparedStatement.setString(6, representation.getElementClassName());
                preparedStatement.addBatch();
                int[] updateCounts = preparedStatement.executeBatch();
            } else {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE FIELDS SET value=?, type=?, class=?");
                preparedStatement.setString(1, representation.getValueString());
                preparedStatement.setInt(2, representation.getTypeOrdinal());
                preparedStatement.setString(3, representation.getClassName());
                preparedStatement.addBatch();
                int[] updateCounts = preparedStatement.executeBatch();
            }
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
//            log.error("", e);
            e.printStackTrace();
        }
    }

    @Override
    public FieldRecord read(Locator locator) {
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select * from FIELDS " + whereClause(locator));
//                    + " where id='" + locator.getId().toString() + "'"
//                    + " and path='" + locator.getPath() + "'");
            rs.next();
            return rsToSF(locator, rs);
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
        try {
            List<FieldRecord> fieldRecords = new ArrayList<FieldRecord>();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select id,path,value,class,type from FIELDS " + whereClause(uuid));
            while (!rs.isAfterLast()) {
                rs.next();
                if (!rs.isAfterLast()) {
                    fieldRecords.add(readSerializedFieldFromResultSet(rs));
                }
            }
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

    private FieldRecord rsToSF(Locator locator, ResultSet rs) throws SQLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, StoreException {
        Class<?> clazz = getClassFromResultSet(rs, "class");
        Class elementClass = getClassFromResultSet(rs, "elementClass");
        Object value = valueFromString(rs.getString("value"), clazz, elementClass);
        return new FieldRecord(locator, value, clazz, elementClass, RECORD_TYPE.values()[rs.getInt("type")]);
    }

    private Class<?> getClassFromResultSet(ResultSet rs, String fieldName) throws ClassNotFoundException, SQLException {
        String className = rs.getString(fieldName);
        return null == className ? null : Class.forName(className);
    }

    private FieldRecord readSerializedFieldFromResultSet(ResultSet rs) throws SQLException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, StoreException {
        Class<?> clazz = Class.forName(rs.getString("class"));
        return new FieldRecord(UUID.fromString(rs.getString("id")), rs.getString("path"), valueFromString(rs.getString("value"), clazz, null),
                clazz, RECORD_TYPE.values()[rs.getInt("type")]);
    }

    private Object valueFromString(String string, Class clazz, Class<Object> elementClass) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, StoreException {
        if (String.class.equals(clazz)) {
            return string;
        } if (ProxyList.class.isAssignableFrom(clazz)) {
            List proxyList = swap.newWrapList(elementClass);
            return proxyList;
        } else {
            Constructor constructor = clazz.getConstructor();
            return constructor.newInstance();
        }
    }

    @Override
    public Iterator<Locator> iterator() {
        try {
            Statement statement = connection.createStatement();
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
        connection.commit();
        connection.close();
        super.finalize();
    }

    public void cleanAll() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("DELETE from FIELDS");
    }
}
