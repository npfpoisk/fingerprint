package kap.fingerprint;

import java.sql.*;

public class FingerPrintSQL {

    private String instanceName = "serverName\\instanseName";
    private String databaseName = "test";
    private String userName = "Usr";
    private String pass = "**********";
    private String connectionUrl = "jdbc:sqlserver://%1$s;databaseName=%2$s;user=%3$s;password=%4$s;";//"jdbc:sqlserver://localhost:1433;databaseName=AdventureWorks;integratedSecurity=true;";
    private String connectionString = String.format(connectionUrl, instanceName, databaseName, userName, pass);

    private Connection con;

    //Конструктор
    FingerPrintSQL(String host_port,String dbName ,String login,String pass){
        instanceName=host_port;
        userName=login;
        this.pass =pass;
        databaseName=dbName;
        //
        connectionString=String.format(connectionUrl, instanceName, databaseName, userName, this.pass);
    }
    //==============================================================

    //��������� ������� ����������� � ��
    boolean connect() {

        try {
            con = DriverManager.getConnection(connectionString);
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    void initDB() throws SQLException {

        Statement stmt=null;
        //
        try {
            stmt = con.createStatement();
            String sql="CREATE TABLE tblUsers"
                    + "(id_user INTEGER not NULL,"
                    + " id_skud INTEGER not NULL,"
                    + " name VARCHAR(50),"
                    + " templ VARBINARY(556),"
                    + " PRIMARY KEY (id_user))";
            //
            stmt.executeUpdate(sql);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (stmt != null) stmt.close();
            if (con != null) con.close();
        }
    }

    void INSERT(int id_user,int id_skud,String name,byte[]template) throws SQLException {

        PreparedStatement stmt=null;
        //
        try {

            String sql="INSERT INTO tblUsers VALUES (?, ?, ?, ?)";
            //
            stmt=con.prepareStatement(sql);
            stmt.setInt(1,id_user);
            stmt.setInt(2,id_skud);
            stmt.setString(3,name);
            stmt.setBytes(4,template);
            //
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (stmt != null) stmt.close();
            if (con != null) con.close();
        }
    }

    ResultSet SELECT() throws SQLException {

        PreparedStatement stmt=null;
        ResultSet rs=null;
        //
        try {

            String sql="SELECT id_user, id_skud, name, templ"
                    + " FROM tblUsers";

            //
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            //if (stmt != null) stmt.close();
            //if (con != null) con.close();
        }
        //
        return rs;
    }

}
