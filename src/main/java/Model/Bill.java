package Model;

import java.sql.Date;
import java.util.ArrayList;

public class Bill {
    private String billId;
    private Date date;
    private Customer customer;
    private ArrayList<BillItem> billItems;
    private int totalBill;
    private int totalFinalBill;
    private int discountOnTotal;
    private int tax;
    private int deposit;
    private int credit;

    public Bill(String billId, Date date, Customer customer, ArrayList<BillItem> billItems, int totalBill, int totalFinalBill, int discountOnTotal, int tax, int deposit) {
        this.billId = billId;
        this.date = date;
        this.customer = customer;
        this.billItems = billItems;
        this.totalBill = totalBill;
        this.totalFinalBill = totalFinalBill;
        this.discountOnTotal = discountOnTotal;
        this.tax = tax;
        this.deposit = deposit;
        this.credit = this.totalFinalBill-this.deposit;
    }
    
    
    
    public String getBillId() {
        return billId;
    }

    public Date getDate() {
        return date;
    }

    public Customer getCustomer() {
        return customer;
    }

    public ArrayList<BillItem> getBillItems() {
        return billItems;
    }

    public int getTotalBill() {
        return totalBill;
    }

    public int getTotalFinalBill() {
        return totalFinalBill;
    }

    public int getDiscountOnTotal() {
        return discountOnTotal;
    }

    public int getTax() {
        return tax;
    }

    public int getDeposit() {
        return deposit;
    }

    public int getCredit() {
        return credit;
    }
    
    
}
