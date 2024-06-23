package Model;
public class BillItem {
    private final Product product;
    private int quantity;
    private String unitSize;
    private int ratePerUnit;
    private String rateType;
    private int total;
    private int discount = 0;

    public BillItem(Product product, int quantity,String unitSize, int ratePerUnit,int discount, String rateType) {
        this.product = product;
        this.quantity = quantity;
        this.unitSize = unitSize;
        this.ratePerUnit = ratePerUnit;
        this.rateType = rateType;
        this.discount = discount;
        this.total = quantity * ratePerUnit - this.discount;
    }
    
    public BillItem(Product product) {
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getUnitSize() {
        return unitSize;
    }

    public int getRatePerUnit() {
        return ratePerUnit;
    }

    public String getRateType() {
        return rateType;
    }

    public int getTotal() {
        return total;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setUnitSize(String unitSize) {
        this.unitSize = unitSize;
    }

    public void setRatePerUnit(int ratePerUnit) {
        this.ratePerUnit = ratePerUnit;
    }

    public void setRateType(String rateType) {
        this.rateType = rateType;
    }

    public void setTotal(int total) {
        this.total = total;
    }    

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }
    
    
}
