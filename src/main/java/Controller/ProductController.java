package Controller;

import Model.Product;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
public class ProductController {
    static Connection con;
    static PreparedStatement pst;
    static ResultSet rs;
    
    public static boolean storeProductToDB(Product newProd){
        boolean success = false;
        try{
            con = connect.connectDB();
            pst = con.prepareStatement("insert into products values(?,?,?,?,?,?,?,?,?)");
            pst.setString(1, newProd.getProdID());
            pst.setString(2, newProd.getProdName());
            pst.setString(3,newProd.getProdBarCodeURL());
            pst.setInt(4, newProd.getBoxWSR());
            pst.setInt(5,newProd.getPackageWSR());
            pst.setInt(6,newProd.getPieceWSR());
            pst.setInt(7,newProd.getBoxRSR());
            pst.setInt(8,newProd.getPackageRSR());
            pst.setInt(9,newProd.getPieceRSR());
            pst.execute();
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
    
    public static boolean deleteProduct(String prodName){
        boolean success = false;
        try{
            con = connect.connectDB();
            pst = con.prepareStatement("delete from products where productName='"+prodName+"'");
            pst.execute();
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
    
    public static Product getProduct(String productName){
        Product prod = null;
        try{
            con = connect.connectDB();
            pst = con.prepareStatement("select * from products where productName='"+productName+"'");
            rs = pst.executeQuery();
            if(rs.next()){
                String id = rs.getString("prodID");
                String prodName = rs.getString("productName");
                String barcodeURL = rs.getString("prodBarCodeURL");
                int boxWSR = rs.getInt("boxWSR");
                int packageWSR = rs.getInt("packageWSR");
                int pieceWSR = rs.getInt("pieceWSR");
                int boxRSR = rs.getInt("boxRSR");
                int packageRSR = rs.getInt("packageRSR");
                int pieceRSR=rs.getInt("pieceRSR");
                prod = new Product(id,prodName,barcodeURL,boxWSR,packageWSR,pieceWSR,boxRSR,packageRSR,pieceRSR);
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
        return prod;
    }
    
    public static boolean updateProduct(Product prod){
        boolean success = false;
        try{
            con = connect.connectDB();
            pst = con.prepareStatement("update products set prodBarCodeURl='"+prod.getProdBarCodeURL()+"', boxWSR="+prod.getBoxWSR()+", packageWSR="+prod.getPackageWSR()+", pieceWSR="+prod.getPieceWSR()+", boxRSR="+prod.getBoxRSR()+", packageRSR="+prod.getPackageRSR()+", pieceRSR="+prod.getPieceRSR()+" where productName='"+prod.getProdName()+"'");
            pst.executeUpdate();
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
    
    public static ArrayList<Product> getProductList(){
        ArrayList<Product> products = new ArrayList<>();
        try{
            con = connect.connectDB();
            pst = con.prepareStatement("select * from products");
            rs = pst.executeQuery();
            while (rs.next()){
                String id = rs.getString("prodID");
                String prodName = rs.getString("productName");
                String barcodeURL = rs.getString("prodBarCodeURL");
                int boxWSR = rs.getInt("boxWSR");
                int packageWSR = rs.getInt("packageWSR");
                int pieceWSR = rs.getInt("pieceWSR");
                int boxRSR = rs.getInt("boxRSR");
                int packageRSR = rs.getInt("packageRSR");
                int pieceRSR=rs.getInt("pieceRSR");
                Product temp = new Product(id,prodName,barcodeURL,boxWSR,packageWSR,pieceWSR,boxRSR,packageRSR,pieceRSR);
                products.add(temp);
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
        return products;
    }
}
