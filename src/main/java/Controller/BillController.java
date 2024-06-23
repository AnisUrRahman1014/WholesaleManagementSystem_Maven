package Controller;

/**
 *
 * @author Anis Ur Rahman
 */
import Model.Bill;
import Model.BillItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
public class BillController {
    static Connection con;
    static PreparedStatement pst;
    static ResultSet rs;
    public static boolean recordBill(Bill bill){
        boolean success = false;
        try{
            con = connect.connectDB();
            pst = con.prepareStatement("insert into bills values(?,?,?,?,?,?,?,?,?)");
            pst.setString(1, bill.getBillId());
            pst.setString(2, bill.getCustomer().getName());
            pst.setDate(3, bill.getDate());
            pst.setInt(4, bill.getTotalBill());
            pst.setInt(5, bill.getTotalFinalBill());
            pst.setInt(6, bill.getDiscountOnTotal());
            pst.setInt(7,bill.getDeposit());
            pst.setInt(8,bill.getCredit());
            pst.setInt(9,bill.getTax());
            
            pst.execute();
            pst.close();
            pst = con.prepareStatement("insert into billItems values(?,?,?,?,?,?,?,?)");
            for(BillItem item: bill.getBillItems()){
                System.out.println("IDHR DEKH");
                pst.setString(1, bill.getBillId());
                pst.setString(2, item.getProduct().getProdID());
                pst.setInt(3,item.getQuantity());
                pst.setInt(4,item.getRatePerUnit());
                pst.setInt(5,item.getDiscount());
                pst.setInt(6,item.getTotal());
                pst.setString(7,item.getRateType());
                pst.setString(8,item.getUnitSize());
                pst.execute();
            }           
            success=true;
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
        return success;
    }
    
    public static int getTodaysBillCount(){
        int count = -1;
        try{
            con = connect.connectDB();
            pst = con.prepareStatement("""
                                       SELECT COUNT(billId) AS todaysBillCount 
                                       FROM bills 
                                       WHERE date = CURRENT_DATE""");
            rs = pst.executeQuery();
            if (rs.next()) {
                count = rs.getInt("todaysBillCount"); // Get the count from the ResultSet
                count+=1;
            }else{
                count=0;
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
        return count;
    }
}
