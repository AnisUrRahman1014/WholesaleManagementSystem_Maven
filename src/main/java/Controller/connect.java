package Controller;

import java.sql.DriverManager;
import javax.swing.JOptionPane;

public class connect extends javax.swing.JFrame {
    static String currentDir=System.getProperty("user.dir");
    java.sql.Connection con=null;
    public static java.sql.Connection connectDB(){
        try{
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            java.sql.Connection con=DriverManager.getConnection("jdbc:derby:WholesaleMS;");
            con.setReadOnly(false);
            return con;
        }catch(Exception e){ 
            JOptionPane.showMessageDialog(null,"Error while connecting to the database");
            System.out.println("Error while establishing connection : Connection Class \n"+e);
            JOptionPane.showMessageDialog(null,e);
            return null;
        }
    }
    public static void main(String[] args) {
        connect obj=new connect();
        connectDB();
    }
}
