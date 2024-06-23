package Components;

import Controller.BillController;
import Controller.ExcelInvoiceGenerator;
import Controller.ManagementSystemCPU;
import Controller.ProductController;
import Controller.WordInvoiceGenerator;
import Model.BillItem;
import Model.Customer;
import Model.Product;
import Model.Bill;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Box;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
/**
 *
 * @author Anis Ur Rahman
 */
public class BillingMenu extends javax.swing.JPanel {
    Date currentDate;
    ArrayList<Product> products;
    BillItem currentItem = null;
    int totalBill;
    Customer selectedCustomer = null;
    Bill currentBill = null;
    public BillingMenu() {
        initComponents();
        createRadioBtnGroups();
        defaultSettings();
        addTableMouseListener();
        addTableModelListener();
        addCustomerTableSelectionListener();
    }
    
    private void createRadioBtnGroups(){
        rateTypeBtnGroup.add(wholesaleRB);
        rateTypeBtnGroup.add(retailRB);
        
        unitSizeBtnGroup.add(boxRB);
        unitSizeBtnGroup.add(packageRB);
        unitSizeBtnGroup.add(pieceRB);
    }
    
    private void defaultSettings(){
        DefaultTableModel model=(DefaultTableModel) billTable.getModel();
        model.setRowCount(0);
        products = new ArrayList<>();
        currentDate = new Date(new java.util.Date().getTime());
        billDateField.setText(String.valueOf(currentDate));
        wholesaleRB.setSelected(true);
        boxRB.setSelected(true);
        quantityField.setText("1");
        prodDiscountField_Rs.setText("0");
        totalBill=0;
        totalBillField.setText(String.valueOf(totalBill));
        finalTotalBillField.setText("0");
        totalDiscountField.setText("0");
        selectedCtmLabel.setText("");
        depositField.setText("0");
        creditField.setText("");
        currentBill = null;
        selectedCustomer = null;
        updateBillId();
        updateTaxField();
        updateProductDropdown();
        updateCustomerTable();
        emptyBillTable();
    }
    
    private void updateTaxField(){
        taxField.setText("0");
    }

    private void addTableMouseListener() {
        billTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) { // Right-click
                    int row = billTable.rowAtPoint(e.getPoint());
                    int column = billTable.columnAtPoint(e.getPoint());
                    if (row != -1 && column != -1) {
                        billTable.setRowSelectionInterval(row, row);
                        if (column == 6) { // RateType column
                            showRateTypeContextMenu(e.getX(), e.getY(), row);
                        } else if (column == 4) { // UnitSize column
                            showUnitSizeContextMenu(e.getX(), e.getY(), row);
                        } else {
                            showContextMenu(e.getX(), e.getY(), row);
                        }
                    }
                }
            }
        });
    }
    
    private void addTableModelListener() {
        billTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = e.getFirstRow();
                int column = e.getColumn();

                if (column == 3 || column == 7 || column==5) { // Quantity or Discount columns or Rate Per Unit
                    updateRowTotal(row,column);
                }
            }
        });
    }
    
    private void showRateTypeContextMenu(int x, int y, int row) {
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem wholesaleItem = new JMenuItem("Wholesale");
        JMenuItem retailItem = new JMenuItem("Retail");
        wholesaleItem.addActionListener(e -> updateRateTypeCell(row, "Wholesale"));
        retailItem.addActionListener(e -> updateRateTypeCell(row, "Retail"));
        contextMenu.add(wholesaleItem);
        contextMenu.add(retailItem);
        contextMenu.show(billTable, x, y);
    }

    private void showUnitSizeContextMenu(int x, int y, int row) {
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem boxItem = new JMenuItem("Box");
        JMenuItem packageItem = new JMenuItem("Package");
        JMenuItem pieceItem = new JMenuItem("Piece");
        boxItem.addActionListener(e -> updateUnitSizeCell(row, "Box"));
        packageItem.addActionListener(e -> updateUnitSizeCell(row, "Package"));
        pieceItem.addActionListener(e -> updateUnitSizeCell(row, "Piece"));
        contextMenu.add(boxItem);
        contextMenu.add(packageItem);
        contextMenu.add(pieceItem);
        contextMenu.show(billTable, x, y);
    }

    private void updateRateTypeCell(int row, String rateType) {
        DefaultTableModel model = (DefaultTableModel) billTable.getModel();
        model.setValueAt(rateType, row, 6);
        updateRowTotal(row,6);
    }

    private void updateUnitSizeCell(int row, String unitSize) {
        DefaultTableModel model = (DefaultTableModel) billTable.getModel();
        model.setValueAt(unitSize, row, 4);
        updateRowTotal(row,4);
    }

    private void updateRowTotal(int row, int col) {
        DefaultTableModel model = (DefaultTableModel) billTable.getModel();
        String rateType = model.getValueAt(row, 6).toString();
        String unitSize = model.getValueAt(row, 4).toString();   
        Product product = products.get(row); // Assuming you have a way to map row to product
        
        int ratePerUnit = getRatePerUnit(product, unitSize, rateType);
        if(col == 5){
            ratePerUnit =(int) model.getValueAt(row, col);
        }else{
            model.setValueAt(ratePerUnit, row, 5);
        }        
        int discount = Integer.valueOf(model.getValueAt(row, 7).toString());
        int quantity = Integer.parseInt(model.getValueAt(row, 3).toString());
        int total = (quantity * ratePerUnit)-discount;
        model.setValueAt(total, row, 8);
        updateTotalBill();
    }

    private int getRatePerUnit(Product product, String unitSize, String rateType) {
        switch (rateType) {
            case "Wholesale" -> {
                switch (unitSize) {
                    case "Box" -> {
                        return product.getBoxWSR();
                    }
                    case "Package" -> {
                        return product.getPackageWSR();
                    }
                    case "Piece" -> {
                        return product.getPieceWSR();
                    }
                }
            }

            case "Retail" -> {
                switch (unitSize) {
                    case "Box" -> {
                        return product.getBoxRSR();
                    }
                    case "Package" -> {
                        return product.getPackageRSR();
                    }
                    case "Piece" -> {
                        return product.getPieceRSR();
                    }
                }
            }

        }
        return 0; // Default case
    }

    private void updateTotalBill() {
        int rowCount = billTable.getRowCount();
        int totalBill = 0;
        for (int i = 0; i < rowCount; i++) {
            totalBill += Integer.parseInt(billTable.getValueAt(i, 8).toString());
        }
        totalBillField.setText(String.valueOf(totalBill));
        finalTotalBillField.setText(String.valueOf(totalBill));
    }

    private void showContextMenu(int x, int y, int row) {
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> deleteRow(row));
        contextMenu.add(deleteItem);
        contextMenu.show(billTable, x, y);
    }

    private void deleteRow(int row) {
        DefaultTableModel model = (DefaultTableModel) billTable.getModel();
        model.removeRow(row);
        // Update total bill
        updateTotalBill();
    }
    
    private void emptyBillTable(){
        DefaultTableModel model=(DefaultTableModel) billTable.getModel();
        model.setRowCount(0);
    }
    
    private void updateBillId(){
        String billId;
        String subDateString = currentDate.toString().replace("-","");
        // Remove the first two characters of the year (assuming year is always first 4 characters)
        subDateString = subDateString.substring(2);
        billId = subDateString;
        int count = BillController.getTodaysBillCount();
        billId=billId.concat("-"+count);
        billIdField.setText(billId);
    }
    
    public void updateProductDropdown(){
        products = ProductController.getProductList();
        productCB.removeAllItems();
        for(Product prod: products){
            productCB.addItem(prod.getProdName());
        }
    }
    
    private void updateCustomerTable(){
        DefaultTableModel model = (DefaultTableModel) customerTable.getModel();
        model.setRowCount(0);
        ArrayList<Customer> customers = Customer.getCustomerList();
        int count = 0;
        for(Customer ctm: customers){
            Object row[]={++count,ctm.getName(),ctm.getContact(),ctm.getAddress()};
            model.addRow(row);
        }
    }
    
    private void addNewCustomer(){
        // Create the labels and text fields
        JTextField customerNameField = new JTextField(10);
        JTextField contactNumberField = new JTextField(10);
        JTextField addressField = new JTextField(10);

        // Set input verifier for contact number field
        contactNumberField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                JTextField textField = (JTextField) input;
                String text = textField.getText();
                return text.matches("\\d{0,11}");
            }
        });
        
        // Create a panel to hold the labels and text fields
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Customer Name:"));
        panel.add(customerNameField);
        panel.add(Box.createVerticalStrut(10)); // a spacer
        panel.add(new JLabel("Contact Number:"));
        panel.add(contactNumberField);
        panel.add(Box.createVerticalStrut(10)); // a spacer
        panel.add(new JLabel("Address:"));
        panel.add(addressField);
        
        

        // Show the dialog and get the user input
        int result = JOptionPane.showConfirmDialog(null, panel, "Enter Customer Details", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String customerName = customerNameField.getText();
            String contactNumber = contactNumberField.getText();
            String address = addressField.getText();
            
            Customer newUser= new Customer(customerName, contactNumber, address);
            boolean requestCheck = newUser.uploadToDB();
             if (!requestCheck) {
                JOptionPane.showMessageDialog(this, "An error occurred", "Request Failed", JOptionPane.ERROR_MESSAGE);
            } else {
                // Update customer table after successful addition
                updateCustomerTable();
            }
        } else {
            JOptionPane.showMessageDialog(this, "No user was added", "Request Cancelled", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    
    private void updateCurrentItem(){   
        if(currentItem==null){
            return;
        }
        if(boxRB.isSelected()){
            updateUnitSize(boxRB.getText());
        }else if(packageRB.isSelected()){
            updateUnitSize(packageRB.getText());
        }else if(pieceRB.isSelected()){
            updateUnitSize(pieceRB.getText());
        }
        
        if(wholesaleRB.isSelected()){
            updateRateType(wholesaleRB.getText());
        }else if(retailRB.isSelected()){
            updateRateType(retailRB.getText());
        }
        
        if(quantityField.getText().isBlank()){
            quantityField.setText("1");
            updateQuantity(quantityField.getText());
        }else{
            updateQuantity(quantityField.getText());
        }
        
        updateTotal();
        updateDiscount();
    }
    
    private void updateUnitSize(String unitSize){
        if(currentItem!=null){
            currentItem.setUnitSize(unitSize);
        }else{
            JOptionPane.showMessageDialog(this,"No product selected. Please choose a product first","Error",JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateRateType(String rateType){
        if(currentItem != null){
            currentItem.setRateType(rateType);            
            switch(rateType){
                case "Wholesale" -> {
                    System.out.println("HELLO BHAI");
                    switch(currentItem.getUnitSize()){
                        case "Box" -> currentItem.setRatePerUnit(currentItem.getProduct().getBoxWSR());
                        case "Package" -> currentItem.setRatePerUnit(currentItem.getProduct().getPackageWSR());
                        case "Piece" -> currentItem.setRatePerUnit(currentItem.getProduct().getPieceWSR());
                    }
                }
                case "Retail" -> {
                    switch(currentItem.getUnitSize()){
                        case "Box" -> currentItem.setRatePerUnit(currentItem.getProduct().getBoxRSR());
                        case "Package" -> currentItem.setRatePerUnit(currentItem.getProduct().getPackageRSR());
                        case "Piece" -> currentItem.setRatePerUnit(currentItem.getProduct().getPieceRSR());
                    }
                }
            }
        }else{
            JOptionPane.showMessageDialog(this,"No product selected. Please choose a product first","Error",JOptionPane.ERROR_MESSAGE);
        }        
    }
    
    private void updateQuantity(String quantity){
        if(currentItem!=null){
            currentItem.setQuantity(Integer.valueOf(quantity));
        }else{
            JOptionPane.showMessageDialog(this,"No product selected. Please choose a product first","Error",JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateTotal(){
        if(currentItem!=null){
            currentItem.setTotal(currentItem.getQuantity() * currentItem.getRatePerUnit());
        }else{
            JOptionPane.showMessageDialog(this,"No product selected. Please choose a product first","Error",JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateDiscount(){
        if(currentItem!=null){
            if(prodDiscountField_Percentage.getText().isBlank() && !prodDiscountField_Rs.getText().isBlank()){
                int tempTotal = currentItem.getTotal();
                int tempDiscount = Integer.valueOf(prodDiscountField_Rs.getText());
                int newTotal = tempTotal - tempDiscount;
                currentItem.setDiscount(tempDiscount);
                currentItem.setTotal(newTotal);
            }else if(prodDiscountField_Rs.getText().isBlank() && !prodDiscountField_Percentage.getText().isBlank()){
                int tempTotal = currentItem.getTotal();
                int tempDiscountPercentage = Integer.valueOf(prodDiscountField_Percentage.getText());
                int tempDiscount = tempTotal * tempDiscountPercentage / 100;
                int newTotal = tempTotal - tempDiscount;
                currentItem.setDiscount(tempDiscount);
                currentItem.setTotal(newTotal);
            }
        }else{
            JOptionPane.showMessageDialog(this,"No product selected. Please choose a product first","Error",JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addBillItemToBill(){
        if(currentItem != null){
            updateCurrentItem();
            DefaultTableModel model = (DefaultTableModel) billTable.getModel();
            for(int row=0;row<model.getRowCount();row++){
                String prodId = model.getValueAt(row, 1).toString();
                String prodName = model.getValueAt(row, 2).toString();
                String unitSize = model.getValueAt(row,4).toString();
                String rateType = model.getValueAt(row,6).toString();
                int discount = (int)model.getValueAt(row, 7);
                int ratePerUnit = (int)model.getValueAt(row,5);
                
                if(currentItem.getProduct().getProdID().equals(prodId) && currentItem.getProduct().getProdName().equals(prodName) && currentItem.getUnitSize().equals(unitSize) && currentItem.getRateType().equals(rateType) && currentItem.getRatePerUnit() == ratePerUnit && currentItem.getDiscount() == discount){
                    JOptionPane.showMessageDialog(this, "Product already exists in the table","Redundancy Error",JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            int count = model.getRowCount()+1;
            Object row[]={count,currentItem.getProduct().getProdID(), currentItem.getProduct().getProdName(),currentItem.getQuantity(),currentItem.getUnitSize(),currentItem.getRatePerUnit(),currentItem.getRateType(),currentItem.getDiscount(),currentItem.getTotal()};
            model.addRow(row);
            totalBill += currentItem.getTotal();
            totalBillField.setText(String.valueOf(totalBill));
            finalTotalBillField.setText(String.valueOf(totalBill));
        }else{
            JOptionPane.showMessageDialog(this,"No product selected. Please choose a product first","Error",JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateFinalBillField(){
        int totalDiscount = 0;
        if(totalDiscountField.getText().isBlank()){
            finalTotalBillField.setText(String.valueOf(totalBill));
            return;
        }
        totalDiscount= Integer.valueOf(totalDiscountField.getText());
        int finalBill = totalBill - totalDiscount;
        finalTotalBillField.setText(String.valueOf(finalBill));
    }
    
    private void filterCustomerTable() {
        String input = ctmIdField.getText().trim().toLowerCase();
        DefaultTableModel model = (DefaultTableModel) customerTable.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        customerTable.setRowSorter(sorter);

        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                String name = entry.getStringValue(1).toLowerCase();
                String contact = entry.getStringValue(2).toLowerCase();
                return name.contains(input) || contact.contains(input);
            }
        };
        sorter.setRowFilter(rf);
    }
    
    private void addCustomerTableSelectionListener() {
        customerTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) { // Ensure that this event is not fired multiple times
                    int selectedRow = customerTable.getSelectedRow();
                    if (selectedRow != -1) {
                        selectedRow = customerTable.convertRowIndexToModel(selectedRow); // Convert to model index if filtered
                        DefaultTableModel model = (DefaultTableModel) customerTable.getModel();
                        String name = model.getValueAt(selectedRow, 1).toString();
                        String contact = model.getValueAt(selectedRow, 2).toString();
                        String address = model.getValueAt(selectedRow, 3).toString();
                        handleSelectedCustomer(name, contact, address);
                    }
                }
            }
        });
    }

    private void handleSelectedCustomer(String name, String contact, String address) {
        // Handle the selected customer data as needed
        selectedCustomer = new Customer(name,contact,address);
        selectedCtmLabel.setText(selectedCustomer.getName());
        JOptionPane.showMessageDialog(null, "Selected Customer:\nName: " + name + "\nContact: " + contact + "\nAddress: " + address);
    }
    
    private void handleProceed(){
        DefaultTableModel model = (DefaultTableModel) billTable.getModel();
        if(model.getRowCount()==0 || selectedCustomer == null || depositField.getText().isBlank()){
            JOptionPane.showMessageDialog(this,"Please check the bill and customer.","Invalid Request",JOptionPane.ERROR_MESSAGE);
            return;
        }
        ArrayList<BillItem> billItems = new ArrayList<>();
            // Iterate through the rows of the bill table
        for (int i = 0; i < model.getRowCount(); i++) {
            System.out.println("KI HAL AY");
            String prodName = (String) model.getValueAt(i, 2);
            int quantity = (int) model.getValueAt(i, 3);
            String unitSize = (String) model.getValueAt(i, 4);
            int ratePerUnit = (int) model.getValueAt(i, 5);
            String rateType = (String) model.getValueAt(i, 6);
            int discount = (int) model.getValueAt(i, 7);
            int total = (int) model.getValueAt(i, 8);

            // Retrieve the Product object using prodID (Assuming a method to get Product by ID)
            Product product = ProductController.getProduct(prodName); // Implement this method to fetch product details

            // Create a BillItem object
            BillItem billItem = new BillItem(product);
            billItem.setQuantity(quantity);
            billItem.setDiscount(discount);
            billItem.setRatePerUnit(ratePerUnit);
            billItem.setRateType(rateType);
            billItem.setTotal(total);
            billItem.setUnitSize(unitSize);

            // Add the created BillItem to the list
            billItems.add(billItem);
        }
        // Create a new Bill object with the gathered data
        currentBill = new Bill(
            billIdField.getText(),
            currentDate,
            selectedCustomer,
            billItems,
            Integer.valueOf(totalBillField.getText()),
            Integer.valueOf(finalTotalBillField.getText())+Integer.valueOf(taxField.getText()),
            Integer.valueOf(totalDiscountField.getText()),
            Integer.valueOf(taxField.getText()),
            Integer.valueOf(depositField.getText())                
        );
        
        boolean success = BillController.recordBill(currentBill);
        if(!success){
            JOptionPane.showMessageDialog(this, "Failed to store the bill","Bill not stored",JOptionPane.ERROR_MESSAGE);
        }else{
            ExcelInvoiceGenerator.generateExcel(currentBill);
            JOptionPane.showMessageDialog(this,"Bill saved","Success",JOptionPane.INFORMATION_MESSAGE);
            currentBill = null;
            defaultSettings();
        }
    }
   
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rateTypeBtnGroup = new javax.swing.ButtonGroup();
        unitSizeBtnGroup = new javax.swing.ButtonGroup();
        jSeparator2 = new javax.swing.JSeparator();
        headerPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        billIdField = new javax.swing.JTextField();
        billDateField = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        taxField = new javax.swing.JFormattedTextField();
        ContentPanel = new javax.swing.JPanel();
        customerDetailsPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        customerTable = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        ctmIdField = new javax.swing.JTextField();
        deselectCustomerBtn = new javax.swing.JButton();
        newCustomerBtn = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        jLabel17 = new javax.swing.JLabel();
        selectedCtmLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        billTable = new javax.swing.JTable();
        billingDetailsPanel = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        addToBillBtn = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        boxRB = new javax.swing.JRadioButton();
        packageRB = new javax.swing.JRadioButton();
        jLabel8 = new javax.swing.JLabel();
        productCB = new javax.swing.JComboBox<>();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel9 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        pieceRB = new javax.swing.JRadioButton();
        jLabel12 = new javax.swing.JLabel();
        quantityField = new javax.swing.JFormattedTextField();
        prodDiscountField_Percentage = new javax.swing.JFormattedTextField();
        prodDiscountField_Rs = new javax.swing.JFormattedTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        addNewProductBtn = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        wholesaleRB = new javax.swing.JRadioButton();
        retailRB = new javax.swing.JRadioButton();
        footerPanel = new javax.swing.JPanel();
        proceedBtn = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        totalBillField = new javax.swing.JTextField();
        dropBillBtn = new javax.swing.JButton();
        finalTotalBillField = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        totalDiscountField = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        depositField = new javax.swing.JFormattedTextField();
        jLabel20 = new javax.swing.JLabel();
        creditField = new javax.swing.JFormattedTextField();

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Bill ID");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Date");

        billIdField.setEnabled(false);
        billIdField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                billIdFieldActionPerformed(evt);
            }
        });

        billDateField.setEnabled(false);
        billDateField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                billDateFieldActionPerformed(evt);
            }
        });

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel19.setText("Tax Rs.");

        taxField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        taxField.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        taxField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                taxFieldFocusLost(evt);
            }
        });

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addComponent(billIdField, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(billDateField, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(taxField, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40))
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(billIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3)
                        .addComponent(billDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(taxField)
                .addContainerGap())
        );

        customerTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "S/No.", "Customer ID", "Contact", "Address"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(customerTable);
        if (customerTable.getColumnModel().getColumnCount() > 0) {
            customerTable.getColumnModel().getColumn(0).setResizable(false);
            customerTable.getColumnModel().getColumn(0).setPreferredWidth(2);
        }

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Customer ID:");

        ctmIdField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ctmIdFieldActionPerformed(evt);
            }
        });
        ctmIdField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                ctmIdFieldKeyReleased(evt);
            }
        });

        deselectCustomerBtn.setText("X");
        deselectCustomerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deselectCustomerBtnActionPerformed(evt);
            }
        });

        newCustomerBtn.setText("New Customer");
        newCustomerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newCustomerBtnActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Customer Details");

        jLabel17.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel17.setText("Selected:");

        selectedCtmLabel.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        selectedCtmLabel.setForeground(new java.awt.Color(0, 204, 0));
        selectedCtmLabel.setText(" ");

        javax.swing.GroupLayout customerDetailsPanelLayout = new javax.swing.GroupLayout(customerDetailsPanel);
        customerDetailsPanel.setLayout(customerDetailsPanelLayout);
        customerDetailsPanelLayout.setHorizontalGroup(
            customerDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customerDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(customerDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator5)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, customerDetailsPanelLayout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(selectedCtmLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(newCustomerBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(customerDetailsPanelLayout.createSequentialGroup()
                        .addGroup(customerDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(customerDetailsPanelLayout.createSequentialGroup()
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(ctmIdField, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(deselectCustomerBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        customerDetailsPanelLayout.setVerticalGroup(
            customerDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, customerDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(customerDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(ctmIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deselectCustomerBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(customerDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(newCustomerBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17)
                    .addComponent(selectedCtmLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        billTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "S/No.", "Product ID", "Product", "Quantity", "Unit", "Rate / Unit", "Rate Type", "Discount", "Total"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Object.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, true, false, true, true, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        billTable.setRowHeight(30);
        billTable.setRowMargin(10);
        billTable.setShowGrid(true);
        billTable.setShowHorizontalLines(true);
        billTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(billTable);
        if (billTable.getColumnModel().getColumnCount() > 0) {
            billTable.getColumnModel().getColumn(0).setResizable(false);
            billTable.getColumnModel().getColumn(0).setPreferredWidth(5);
            billTable.getColumnModel().getColumn(3).setResizable(false);
            billTable.getColumnModel().getColumn(5).setResizable(false);
        }

        billingDetailsPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setText("Create Bill");

        addToBillBtn.setText("Add to bill");
        addToBillBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addToBillBtnActionPerformed(evt);
            }
        });

        boxRB.setText("Box");
        boxRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boxRBActionPerformed(evt);
            }
        });

        packageRB.setText("Package");
        packageRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                packageRBActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setText("Unit Size");

        productCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        productCB.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                productCBItemStateChanged(evt);
            }
        });
        productCB.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                productCBMouseClicked(evt);
            }
        });
        productCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                productCBActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel9.setText("Quantity");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Product:");

        pieceRB.setText("Piece");
        pieceRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pieceRBActionPerformed(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel12.setText("Prod. Discount");

        quantityField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        quantityField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                quantityFieldFocusLost(evt);
            }
        });
        quantityField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quantityFieldActionPerformed(evt);
            }
        });
        quantityField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                quantityFieldKeyReleased(evt);
            }
        });

        prodDiscountField_Percentage.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        prodDiscountField_Percentage.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                prodDiscountField_PercentageKeyReleased(evt);
            }
        });

        prodDiscountField_Rs.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        prodDiscountField_Rs.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                prodDiscountField_RsFocusLost(evt);
            }
        });
        prodDiscountField_Rs.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                prodDiscountField_RsKeyReleased(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel13.setText("Rs. /");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel14.setText("%");

        addNewProductBtn.setText("+");
        addNewProductBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewProductBtnActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel10.setText("Rate Type: ");

        wholesaleRB.setText("Wholesale");
        wholesaleRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wholesaleRBActionPerformed(evt);
            }
        });

        retailRB.setText("Retail");
        retailRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                retailRBActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout billingDetailsPanelLayout = new javax.swing.GroupLayout(billingDetailsPanel);
        billingDetailsPanel.setLayout(billingDetailsPanelLayout);
        billingDetailsPanelLayout.setHorizontalGroup(
            billingDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(billingDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(billingDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(billingDetailsPanelLayout.createSequentialGroup()
                        .addGroup(billingDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(addToBillBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(billingDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jSeparator4, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, billingDetailsPanelLayout.createSequentialGroup()
                                    .addGap(29, 29, 29)
                                    .addGroup(billingDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(billingDetailsPanelLayout.createSequentialGroup()
                                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(18, 18, 18)
                                            .addComponent(productCB, javax.swing.GroupLayout.PREFERRED_SIZE, 341, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(billingDetailsPanelLayout.createSequentialGroup()
                                            .addGroup(billingDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(billingDetailsPanelLayout.createSequentialGroup()
                                                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(boxRB)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(packageRB))
                                                .addGroup(billingDetailsPanelLayout.createSequentialGroup()
                                                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(quantityField, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(billingDetailsPanelLayout.createSequentialGroup()
                                                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(prodDiscountField_Rs, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGap(5, 5, 5)
                                                    .addComponent(jLabel13)))
                                            .addGap(18, 18, 18)
                                            .addGroup(billingDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(billingDetailsPanelLayout.createSequentialGroup()
                                                    .addComponent(prodDiscountField_Percentage, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(jLabel14))
                                                .addComponent(pieceRB)))
                                        .addGroup(billingDetailsPanelLayout.createSequentialGroup()
                                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(18, 18, 18)
                                            .addComponent(wholesaleRB)
                                            .addGap(18, 18, 18)
                                            .addComponent(retailRB))))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addNewProductBtn)))
                .addContainerGap(39, Short.MAX_VALUE))
        );
        billingDetailsPanelLayout.setVerticalGroup(
            billingDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(billingDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(billingDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(productCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addNewProductBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(billingDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addGroup(billingDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(boxRB)
                        .addComponent(packageRB)
                        .addComponent(pieceRB)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(billingDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(wholesaleRB)
                    .addComponent(retailRB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(billingDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(quantityField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(billingDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(prodDiscountField_Percentage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(prodDiscountField_Rs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addToBillBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout ContentPanelLayout = new javax.swing.GroupLayout(ContentPanel);
        ContentPanel.setLayout(ContentPanelLayout);
        ContentPanelLayout.setHorizontalGroup(
            ContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ContentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1050, Short.MAX_VALUE)
                    .addGroup(ContentPanelLayout.createSequentialGroup()
                        .addComponent(billingDetailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(customerDetailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        ContentPanelLayout.setVerticalGroup(
            ContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ContentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(customerDetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(billingDetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        proceedBtn.setText("Proceed");
        proceedBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                proceedBtnActionPerformed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel11.setText("Total Bill");

        totalBillField.setEditable(false);
        totalBillField.setFont(new java.awt.Font("MonospaceTypewriter", 0, 18)); // NOI18N
        totalBillField.setForeground(new java.awt.Color(0, 204, 0));
        totalBillField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                totalBillFieldActionPerformed(evt);
            }
        });

        dropBillBtn.setBackground(new java.awt.Color(255, 51, 0));
        dropBillBtn.setForeground(new java.awt.Color(242, 242, 242));
        dropBillBtn.setText("Drop bill");
        dropBillBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dropBillBtnActionPerformed(evt);
            }
        });

        finalTotalBillField.setEditable(false);
        finalTotalBillField.setFont(new java.awt.Font("Monospaced", 0, 36)); // NOI18N
        finalTotalBillField.setForeground(new java.awt.Color(0, 204, 51));
        finalTotalBillField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        finalTotalBillField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finalTotalBillFieldActionPerformed(evt);
            }
        });

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel16.setText("Final Bill");

        totalDiscountField.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        totalDiscountField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                totalDiscountFieldFocusLost(evt);
            }
        });
        totalDiscountField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                totalDiscountFieldActionPerformed(evt);
            }
        });
        totalDiscountField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                totalDiscountFieldKeyReleased(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel15.setText("Discount");

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel18.setText("Deposit");

        depositField.setForeground(new java.awt.Color(0, 204, 204));
        depositField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        depositField.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        depositField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                depositFieldFocusLost(evt);
            }
        });
        depositField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                depositFieldKeyReleased(evt);
            }
        });

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel20.setText("Credit");

        creditField.setEditable(false);
        creditField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        creditField.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        creditField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                creditFieldFocusLost(evt);
            }
        });

        javax.swing.GroupLayout footerPanelLayout = new javax.swing.GroupLayout(footerPanel);
        footerPanel.setLayout(footerPanelLayout);
        footerPanelLayout.setHorizontalGroup(
            footerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(footerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(footerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(footerPanelLayout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(totalBillField, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(footerPanelLayout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(totalDiscountField, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(finalTotalBillField, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(footerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(footerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(creditField)
                    .addComponent(depositField))
                .addGap(18, 18, 18)
                .addComponent(dropBillBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(proceedBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        footerPanelLayout.setVerticalGroup(
            footerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(finalTotalBillField)
            .addGroup(footerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(footerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(footerPanelLayout.createSequentialGroup()
                        .addGroup(footerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(proceedBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dropBillBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(8, Short.MAX_VALUE))
                    .addGroup(footerPanelLayout.createSequentialGroup()
                        .addGroup(footerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(totalBillField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(footerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(totalDiscountField, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))))
            .addGroup(footerPanelLayout.createSequentialGroup()
                .addGroup(footerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel18, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(depositField, javax.swing.GroupLayout.Alignment.LEADING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(footerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(creditField, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jSeparator2)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(headerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18))
                    .addComponent(footerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(footerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(52, 52, 52))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void billIdFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_billIdFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_billIdFieldActionPerformed

    private void billDateFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_billDateFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_billDateFieldActionPerformed

    private void ctmIdFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ctmIdFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ctmIdFieldActionPerformed

    private void deselectCustomerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deselectCustomerBtnActionPerformed
        // TODO add your handling code here:
        selectedCustomer = null;
        selectedCtmLabel.setText("");
    }//GEN-LAST:event_deselectCustomerBtnActionPerformed

    private void addToBillBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addToBillBtnActionPerformed
        // TODO add your handling code here:
        addBillItemToBill();
    }//GEN-LAST:event_addToBillBtnActionPerformed

    private void proceedBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_proceedBtnActionPerformed
        // TODO add your handling code here:
        handleProceed();
    }//GEN-LAST:event_proceedBtnActionPerformed

    private void dropBillBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dropBillBtnActionPerformed
        // TODO add your handling code here:
        defaultSettings();
    }//GEN-LAST:event_dropBillBtnActionPerformed

    private void totalBillFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_totalBillFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_totalBillFieldActionPerformed

    private void pieceRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pieceRBActionPerformed
        // TODO add your handling code here:
        if(pieceRB.isSelected()){
            updateUnitSize(pieceRB.getText());
        }
    }//GEN-LAST:event_pieceRBActionPerformed

    private void newCustomerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newCustomerBtnActionPerformed
        // TODO add your handling code here:
        addNewCustomer();
    }//GEN-LAST:event_newCustomerBtnActionPerformed

    private void addNewProductBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewProductBtnActionPerformed
        // TODO add your handling code here:
        AddNewProductDialog dialog = new AddNewProductDialog(null, true);
        dialog.setOnCloseListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateProductDropdown();
            }
        });
        dialog.setVisible(true);
    }//GEN-LAST:event_addNewProductBtnActionPerformed

    private void quantityFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quantityFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_quantityFieldActionPerformed

    private void productCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_productCBActionPerformed
        // TODO add your handling code here:
        if(productCB.getSelectedItem() !=null){
            String selectedProdName = productCB.getSelectedItem().toString();
            Product currentProduct = ProductController.getProduct(selectedProdName);
            currentItem = new BillItem(currentProduct);
            updateCurrentItem();
        }
    }//GEN-LAST:event_productCBActionPerformed

    private void productCBMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_productCBMouseClicked
        // TODO add your handling code here:
        
    }//GEN-LAST:event_productCBMouseClicked

    private void productCBItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_productCBItemStateChanged
        // TODO add your handling code here:
            
    }//GEN-LAST:event_productCBItemStateChanged

    private void boxRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boxRBActionPerformed
        // TODO add your handling code here:
        updateCurrentItem();
    }//GEN-LAST:event_boxRBActionPerformed

    private void packageRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_packageRBActionPerformed
        // TODO add your handling code here:
        updateCurrentItem();
    }//GEN-LAST:event_packageRBActionPerformed

    private void wholesaleRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wholesaleRBActionPerformed
        // TODO add your handling code here:
        updateCurrentItem();
    }//GEN-LAST:event_wholesaleRBActionPerformed

    private void retailRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_retailRBActionPerformed
        // TODO add your handling code here:        
        updateCurrentItem();
    }//GEN-LAST:event_retailRBActionPerformed

    private void quantityFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_quantityFieldKeyReleased
        // TODO add your handling code here:
        if(!quantityField.getText().isBlank())
        updateQuantity(quantityField.getText());
    }//GEN-LAST:event_quantityFieldKeyReleased

    private void finalTotalBillFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_finalTotalBillFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_finalTotalBillFieldActionPerformed

    private void totalDiscountFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_totalDiscountFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_totalDiscountFieldActionPerformed

    private void prodDiscountField_RsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_prodDiscountField_RsKeyReleased
        // TODO add your handling code here:
        if(!prodDiscountField_Percentage.getText().isBlank()){
            prodDiscountField_Percentage.setText("");
        }
        updateDiscount();
    }//GEN-LAST:event_prodDiscountField_RsKeyReleased

    private void prodDiscountField_PercentageKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_prodDiscountField_PercentageKeyReleased
        // TODO add your handling code here:
        if(!prodDiscountField_Rs.getText().isBlank()){
            prodDiscountField_Rs.setText("");
        }
        updateDiscount();
    }//GEN-LAST:event_prodDiscountField_PercentageKeyReleased

    private void totalDiscountFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_totalDiscountFieldFocusLost
        // TODO add your handling code here:
        if(totalDiscountField.getText().isBlank()){
            totalDiscountField.setText("0");
        }
    }//GEN-LAST:event_totalDiscountFieldFocusLost

    private void prodDiscountField_RsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_prodDiscountField_RsFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_prodDiscountField_RsFocusLost

    private void quantityFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_quantityFieldFocusLost
        // TODO add your handling code here:
        if(quantityField.getText().isBlank()){
            quantityField.setText("0");
        }
    }//GEN-LAST:event_quantityFieldFocusLost

    private void totalDiscountFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_totalDiscountFieldKeyReleased
        // TODO add your handling code here:
        if(totalBill > 0){
            updateFinalBillField();
        }
    }//GEN-LAST:event_totalDiscountFieldKeyReleased

    private void ctmIdFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ctmIdFieldKeyReleased
        // TODO add your handling code here:
        filterCustomerTable();
    }//GEN-LAST:event_ctmIdFieldKeyReleased

    private void depositFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_depositFieldFocusLost
        // TODO add your handling code here:
        if(depositField.getText().isBlank()){
            depositField.setText("0");
        }
    }//GEN-LAST:event_depositFieldFocusLost

    private void taxFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_taxFieldFocusLost
        // TODO add your handling code here:
        if(taxField.getText().isBlank()){
            taxField.setText("0");
        }
    }//GEN-LAST:event_taxFieldFocusLost

    private void creditFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_creditFieldFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_creditFieldFocusLost

    private void depositFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_depositFieldKeyReleased
        // TODO add your handling code here:
        if (Character.isDigit(evt.getKeyChar()) || evt.getKeyCode() == java.awt.event.KeyEvent.VK_BACK_SPACE) {
        // Ensure depositField is not blank
        if (!depositField.getText().isBlank()) {
            try {
                int credit = Integer.parseInt(finalTotalBillField.getText()) - Integer.parseInt(depositField.getText());
                creditField.setText(String.valueOf(credit));
            } catch (NumberFormatException e) {
                // Handle the case where the text fields do not contain valid integers
                System.out.println("Invalid number format in one of the fields");
            }
        }else{
            creditField.setText(finalTotalBillField.getText());
        }
    }
    }//GEN-LAST:event_depositFieldKeyReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ContentPanel;
    private javax.swing.JButton addNewProductBtn;
    private javax.swing.JButton addToBillBtn;
    private javax.swing.JTextField billDateField;
    private javax.swing.JTextField billIdField;
    private javax.swing.JTable billTable;
    private javax.swing.JPanel billingDetailsPanel;
    private javax.swing.JRadioButton boxRB;
    private javax.swing.JFormattedTextField creditField;
    private javax.swing.JTextField ctmIdField;
    private javax.swing.JPanel customerDetailsPanel;
    private javax.swing.JTable customerTable;
    private javax.swing.JFormattedTextField depositField;
    private javax.swing.JButton deselectCustomerBtn;
    private javax.swing.JButton dropBillBtn;
    private javax.swing.JTextField finalTotalBillField;
    private javax.swing.JPanel footerPanel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JButton newCustomerBtn;
    private javax.swing.JRadioButton packageRB;
    private javax.swing.JRadioButton pieceRB;
    private javax.swing.JButton proceedBtn;
    private javax.swing.JFormattedTextField prodDiscountField_Percentage;
    private javax.swing.JFormattedTextField prodDiscountField_Rs;
    private javax.swing.JComboBox<String> productCB;
    private javax.swing.JFormattedTextField quantityField;
    private javax.swing.ButtonGroup rateTypeBtnGroup;
    private javax.swing.JRadioButton retailRB;
    private javax.swing.JLabel selectedCtmLabel;
    private javax.swing.JFormattedTextField taxField;
    private javax.swing.JTextField totalBillField;
    private javax.swing.JTextField totalDiscountField;
    private javax.swing.ButtonGroup unitSizeBtnGroup;
    private javax.swing.JRadioButton wholesaleRB;
    // End of variables declaration//GEN-END:variables

}

