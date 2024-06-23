package Model;

import java.io.File;



/**
 *
 * @author Anis Ur Rahman
 */
public class Product {
    private String prodID;
    private String prodName;
    private String prodBarCodeURL;
    private File barcodeFile;
    private int boxWSR;
    private int packageWSR;
    private int pieceWSR;
    private int boxRSR;
    private int packageRSR;
    private int pieceRSR;

    public Product(String prodID, String prodName, String prodBarCodeURL, int boxWSR, int packageWSR, int pieceWSR, int boxRSR, int packageRSR, int pieceRSR) {
        this.prodID = prodID;
        this.prodName = prodName;
        this.boxWSR = boxWSR;
        this.packageWSR = packageWSR;
        this.pieceWSR = pieceWSR;
        this.boxRSR = boxRSR;
        this.packageRSR = packageRSR;
        this.pieceRSR = pieceRSR;
        this.prodBarCodeURL = prodBarCodeURL;
    }

    public Product(String prodID, String prodName, File barcodeFile, int boxWSR, int packageWSR, int pieceWSR, int boxRSR, int packageRSR, int pieceRSR) {
        this.prodID = prodID;
        this.prodName = prodName;
        this.barcodeFile = barcodeFile;
        this.boxWSR = boxWSR;
        this.packageWSR = packageWSR;
        this.pieceWSR = pieceWSR;
        this.boxRSR = boxRSR;
        this.packageRSR = packageRSR;
        this.pieceRSR = pieceRSR;
        this.prodBarCodeURL = returnFileName(barcodeFile);
    }
    
    private String returnFileName(File barcodeFile){
        if(barcodeFile != null){
            return barcodeFile.getName();
        }
        return null;
    }

    public String getProdID() {
        return prodID;
    }

    public String getProdName() {
        return prodName;
    }

    public String getProdBarCodeURL() {
        return prodBarCodeURL;
    }

    public int getBoxWSR() {
        return boxWSR;
    }

    public int getPackageWSR() {
        return packageWSR;
    }

    public int getPieceWSR() {
        return pieceWSR;
    }

    public int getBoxRSR() {
        return boxRSR;
    }

    public int getPackageRSR() {
        return packageRSR;
    }

    public int getPieceRSR() {
        return pieceRSR;
    }
    
    
    
    
    
    
}
