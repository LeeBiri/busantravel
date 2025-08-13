package busantravel.db;

/**
 * Copyright(c) 2001 iSavvix Corporation (http://www.isavvix.com/)
 *
 *                        All rights reserved
 *
 * Permission to use, copy, modify and distribute this material for
 * any purpose and without fee is hereby granted, provided that the
 * above copyright notice and this permission notice appear in all
 * copies, and that the name of iSavvix Corporation not be used in
 * advertising or publicity pertaining to this material without the
 * specific, prior written permission of an authorized representative of
 * iSavvix Corporation.
 *
 * ISAVVIX CORPORATION MAKES NO REPRESENTATIONS AND EXTENDS NO WARRANTIES,
 * EXPRESS OR IMPLIED, WITH RESPECT TO THE SOFTWARE, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR ANY PARTICULAR PURPOSE, AND THE WARRANTY AGAINST
 * INFRINGEMENT OF PATENTS OR OTHER INTELLECTUAL PROPERTY RIGHTS.  THE
 * SOFTWARE IS PROVIDED "AS IS", AND IN NO EVENT SHALL ISAVVIX CORPORATION OR
 * ANY OF ITS AFFILIATES BE LIABLE FOR ANY DAMAGES, INCLUDING ANY
 * LOST PROFITS OR OTHER INCIDENTAL OR CONSEQUENTIAL DAMAGES RELATING
 * TO THE SOFTWARE.
 *
 */


 
import java.sql.*;
import java.util.Properties;
import java.util.Vector;


/**
 * Manages a java.sql.Connection pool.
 *
 * @author  Anil Hemrajani
 */
public class DBConnectionMgr {
    private Vector connections = new Vector(10);
    private String _driver = "com.mysql.cj.jdbc.Driver",
    _url = "jdbc:mysql://113.198.238.95:3306/busan_travel?characterEncoding=UTF-8&serverTimezone=UTC",
    _user = "sangmin",
    _password = "1234";
    
    private boolean _traceOn = false;
    private boolean initialized = false;
    private int _openConnections = 50;
    private static DBConnectionMgr instance = null;
    private static boolean isDbConnected = false;

    private DBConnectionMgr() {}

    /**
     * DBConnectionMgr의 싱글턴 인스턴스를 반환합니다.
     * @return DBConnectionMgr 인스턴스
     */
    public static DBConnectionMgr getInstance() {
        if (instance == null) {
            synchronized (DBConnectionMgr.class) {
                if (instance == null) {
                    instance = new DBConnectionMgr();
                }
            }
        }

        return instance;
    }

    /**
     * 데이터베이스 연결 상태를 확인합니다.
     * @return 연결되었으면 true, 그렇지 않으면 false
     */
    public static boolean isDbConnected() {
        // 인스턴스를 통해 초기 연결 시도를 유도하고 상태를 반환
        if (instance == null) {
            getInstance(); // 초기화 시도
        }
        // 첫 연결 시도
        try (Connection conn = getInstance().getConnection()) {
            isDbConnected = conn != null && !conn.isClosed();
        } catch (Exception e) {
            isDbConnected = false;
        }
        return isDbConnected;
    }

    /**
     * 최대 유휴 연결 수를 설정합니다.
     * @param count 최대 유휴 연결 수
     */
    public void setOpenConnectionCount(int count) {
        _openConnections = count;
    }

    /**
     * 트레이스 메시지 출력을 활성화/비활성화합니다.
     * @param enable 활성화하려면 true
     */
    public void setEnableTrace(boolean enable) {
        _traceOn = enable;
    }


    /**
     * 현재 연결 풀의 모든 연결 객체를 담은 Vector를 반환합니다.
     * @return 연결 객체 Vector
     */
    public Vector getConnectionList() {
        return connections;
    }


    /**
     * 지정된 수만큼의 연결을 미리 생성하여 풀에 추가합니다.
     * @param count 생성할 연결 수
     * @throws SQLException SQL 예외 발생 시
     */
    public synchronized void setInitOpenConnections(int count)
            throws SQLException {
        Connection c = null;
        ConnectionObject co = null;

        for (int i = 0; i < count; i++) {
            c = createConnection();
            co = new ConnectionObject(c, false);

            connections.addElement(co);
            trace("ConnectionPoolManager: Adding new DB connection to pool (" + connections.size() + ")");
        }
    }


    /**
     * 현재 풀에 있는 총 연결 수를 반환합니다.
     * @return 총 연결 수
     */
    public int getConnectionCount() {
        return connections.size();
    }


    /**
     * 사용 가능한 연결을 풀에서 가져옵니다. 없으면 새로 생성합니다.
     * @return 사용 가능한 Connection 객체
     * @throws Exception 연결 생성 또는 가져오기 실패 시
     */
    public synchronized Connection getConnection()
            throws Exception {
        if (!initialized) {
            Class c = Class.forName(_driver);
            DriverManager.registerDriver((Driver) c.newInstance());

            initialized = true;
        }


        Connection c = null;
        ConnectionObject co = null;
        boolean badConnection = false;


        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);

            // If connection is not in use, test to ensure it's still valid!
            if (!co.inUse) {
                try {
                    badConnection = co.connection.isClosed();
                    if (!badConnection)
                        badConnection = (co.connection.getWarnings() != null);
                } catch (Exception e) {
                    badConnection = true;
                    e.printStackTrace();
                }

                // Connection is bad, remove from pool
                if (badConnection) {
                    connections.removeElementAt(i);
                    trace("ConnectionPoolManager: Remove disconnected DB connection #" + i);
                    continue;
                }

                c = co.connection;
                co.inUse = true;

                trace("ConnectionPoolManager: Using existing DB connection #" + (i + 1));
                break;
            }
        }

        if (c == null) {
            c = createConnection();
            co = new ConnectionObject(c, true);
            connections.addElement(co);

            trace("ConnectionPoolManager: Creating new DB connection #" + connections.size());
        }

        return c;
    }


    /**
     * 사용이 끝난 연결을 풀에 반환합니다.
     * @param c 반환할 Connection 객체
     */
    public synchronized void freeConnection(Connection c) {
        if (c == null)
            return;

        ConnectionObject co = null;

        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            if (c == co.connection) {
                co.inUse = false;
                break;
            }
        }

        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            if ((i + 1) > _openConnections && !co.inUse)
                removeConnection(co.connection);
        }
    }

    /**
     * Connection, PreparedStatement, ResultSet을 모두 닫고 연결을 풀에 반환합니다.
     * @param c Connection 객체
     * @param p PreparedStatement 객체
     * @param r ResultSet 객체
     */
    public void freeConnection(Connection c, PreparedStatement p, ResultSet r) {
        try {
            if (r != null) r.close();
            if (p != null) p.close();
            freeConnection(c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Connection, Statement, ResultSet을 모두 닫고 연결을 풀에 반환합니다.
     * @param c Connection 객체
     * @param s Statement 객체
     * @param r ResultSet 객체
     */
    public void freeConnection(Connection c, Statement s, ResultSet r) {
        try {
            if (r != null) r.close();
            if (s != null) s.close();
            freeConnection(c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Connection, PreparedStatement를 닫고 연결을 풀에 반환합니다.
     * @param c Connection 객체
     * @param p PreparedStatement 객체
     */
    public void freeConnection(Connection c, PreparedStatement p) {
        try {
            if (p != null) p.close();
            freeConnection(c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Connection, Statement를 닫고 연결을 풀에 반환합니다.
     * @param c Connection 객체
     * @param s Statement 객체
     */
    public void freeConnection(Connection c, Statement s) {
        try {
            if (s != null) s.close();
            freeConnection(c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * 연결을 풀에서 제거하고 실제로 닫습니다.
     * @param c 제거할 Connection 객체
     */
    public synchronized void removeConnection(Connection c) {
        if (c == null)
            return;

        ConnectionObject co = null;
        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            if (c == co.connection) {
                try {
                    c.close();
                    connections.removeElementAt(i);
                    trace("Removed " + c.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            }
        }
    }


    private Connection createConnection()
            throws SQLException {
        Connection con = null;

        try {
            if (_user == null)
                _user = "";
            if (_password == null)
                _password = "";

            Properties props = new Properties();
            props.put("user", _user);
            props.put("password", _password);

            con = DriverManager.getConnection(_url, props);
            isDbConnected = true; // 연결 성공
        } catch (Throwable t) {
            isDbConnected = false; // 연결 실패
        	System.err.println("DBConnectionMgr: DB 연결 실패");
        	System.err.println("URL: " + _url);
        	System.err.println("User: " + _user);
        	// t.printStackTrace(); // 실패 시 스택 트레이스 생략
            throw new SQLException(t.getMessage());
        }

        return con;
    }


    /** Closes all connections and clears out the connection pool */
    public void releaseFreeConnections() {
        trace("ConnectionPoolManager.releaseFreeConnections()");

        Connection c = null;
        ConnectionObject co = null;

        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            if (!co.inUse)
                removeConnection(co.connection);
        }
    }


    /** Closes all connections and clears out the connection pool */
    public void finalize() {
        trace("ConnectionPoolManager.finalize()");

        Connection c = null;
        ConnectionObject co = null;

        for (int i = 0; i < connections.size(); i++) {
            co = (ConnectionObject) connections.elementAt(i);
            try {
                co.connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            co = null;
        }

        connections.removeAllElements();
    }


    private void trace(String s) {
        if (_traceOn)
            System.err.println(s);
    }

}


class ConnectionObject {
    public java.sql.Connection connection = null;
    public boolean inUse = false;

    public ConnectionObject(Connection c, boolean useFlag) {
        connection = c;
        inUse = useFlag;
    }
}
