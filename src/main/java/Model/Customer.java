package Model;

import java.sql.Connection;
import java.sql.SQLException;
import Controller.connect;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Anis Urrahman
 */
public class Customer {
    private String name;
    private String contact;
    private String address;
    private static Connection con;
    private static PreparedStatement pst;
    private static ResultSet rs;
    
    
    public Customer(String name, String contact, String address){
        this.name=name;
        this.contact=contact;
        this.address=address;
    }    

    public String getName() {
        return name;
    }

    public String getContact() {
        return contact;
    }

    public String getAddress() {
        return address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    
    public boolean uploadToDB(){
        boolean success = false;
        try{
            con = connect.connectDB();
            pst = con.prepareStatement("insert into customer values('"+this.name+"','"+this.contact+"','"+this.address+"')");
            pst.execute();
            success=true;
        }catch(SQLException e){
            e.printStackTrace();
        }finally{
            if(con!=null){
                try {
                    con.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Customer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return success;
    }
    
    public static ArrayList<Customer> getCustomerList(){
        ArrayList<Customer> customers= new ArrayList<>();
        try{
            con = connect.connectDB();
            pst= con.prepareStatement("select * from customer");
            rs = pst.executeQuery();
            while(rs.next()){
                String tempName = rs.getString("name");
                String tempContact = rs.getString("contact");
                String tempAddress = rs.getString("address");
                Customer temp = new Customer(tempName, tempContact, tempAddress);
                customers.add(temp);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                if(con!=null){
                    con.close();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return customers;
    }
    
}
