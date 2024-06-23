package Components;

import Controller.ProductController;
import Model.Product;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Anis Ur Rahman
 */
public class AddNewProductDialog extends javax.swing.JDialog {
    File barcodeFile = null;
    private ActionListener onCloseListener;
    private ArrayList<Product> products;
    private JPopupMenu popupMenu;
    public AddNewProductDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        defaultSettings();
        addTableMouseListener();
    }
    
    private void defaultSettings(){
        chooseFileBtn.setEnabled(false);
        updateBtn.setEnabled(false);
        confirmBtn.setEnabled(true);
        prodIdField.setEnabled(true);
        prodNameField.setEnabled(true);
        products = ProductController.getProductList();
        DefaultTableModel model=(DefaultTableModel) productsTable.getModel();
        model.setRowCount(0);
        int count = model.getRowCount();
        for(Product prod: products){
            Object row[]={++count,prod.getProdID(), prod.getProdName()};
            model.addRow(row);
        }
        prodIdField.setText("");
        prodNameField.setText("");
        prodBarCodeCB.setSelected(false);
        barcodeFile=null;
        boxWSR.setText(String.valueOf(0));
        packageWSR.setText(String.valueOf(0));
        pieceWSR.setText(String.valueOf(0));
        boxRSR.setText(String.valueOf(0));
        packageRSR.setText(String.valueOf(0));
        pieceRSR.setText(String.valueOf(0));
        fileNameLabel.setText("");
        // Initialize the popup menu
        popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener((ActionEvent e) -> {
            deleteSelectedRow();
        });
        popupMenu.add(deleteItem);
    }
    
    private boolean validateFile(){
        return (barcodeFile !=null && prodBarCodeCB.isSelected()) || !prodBarCodeCB.isSelected() ;
    }
    
    private boolean validateFields(){
        boolean success = true;
        if(prodIdField.getText().isBlank() || prodNameField.getText().isBlank()){
            success=false;
        }
        return success;
    }
    
    private void createNewProduct(){
        if(!validateFields()){
            JOptionPane.showMessageDialog(this,"Please check the fields first.","Failure",JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(!validateFile()){
            JOptionPane.showMessageDialog(this,"Either choose a Barcode file or uncheck the box","File not found",JOptionPane.ERROR_MESSAGE);
            return;
        }
        Product prod = new Product(prodIdField.getText(),prodNameField.getText(),barcodeFile,Integer.valueOf(boxWSR.getText()),Integer.valueOf(packageWSR.getText()),Integer.valueOf(pieceWSR.getText()),Integer.valueOf(boxRSR.getText()),Integer.valueOf(packageRSR.getText()),Integer.valueOf(pieceRSR.getText()));
        boolean isUploaded = ProductController.storeProductToDB(prod);
        if(isUploaded){
            JOptionPane.showMessageDialog(this,"Product \""+prod.getProdName()+"\" uploaded successfully","Product added",JOptionPane.INFORMATION_MESSAGE);
            setVisible(false);
        }else{
            JOptionPane.showMessageDialog(this,"Product \""+prod.getProdName()+"\" was not uploaded","Failure",JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void plotFieldsWithSelectedProduct(){
        DefaultTableModel model = (DefaultTableModel)productsTable.getModel();
        int selectedRow = productsTable.getSelectedRow();
        String selectedProdName = model.getValueAt(selectedRow, 2).toString();
        Product selectedProd = ProductController.getProduct(selectedProdName);
        
        // PLOTTING FIELDS
        prodIdField.setText(selectedProd.getProdID());
        prodNameField.setText(selectedProd.getProdName());
        if(selectedProd.getProdBarCodeURL() != null){
            prodBarCodeCB.setSelected(true);
            chooseFileBtn.setEnabled(true);
            fileNameLabel.setText(selectedProd.getProdBarCodeURL());
        }else{
            prodBarCodeCB.setSelected(false);
            chooseFileBtn.setEnabled(false);
            fileNameLabel.setText("");
        }
        
        boxWSR.setText(String.valueOf(selectedProd.getBoxWSR()));
        packageWSR.setText(String.valueOf(selectedProd.getPackageWSR()));
        pieceWSR.setText(String.valueOf(selectedProd.getPieceWSR()));
        boxRSR.setText(String.valueOf(selectedProd.getBoxRSR()));
        packageRSR.setText(String.valueOf(selectedProd.getPackageRSR()));
        pieceRSR.setText(String.valueOf(selectedProd.getPieceRSR()));
        updateBtn.setEnabled(true);
        confirmBtn.setEnabled(false);
        prodIdField.setEnabled(false);
        prodNameField.setEnabled(false);
    }
    
    public void setOnCloseListener(ActionListener listener) {
        this.onCloseListener = listener;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible && onCloseListener != null) {
            onCloseListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        }
    }
    
    private void addTableMouseListener() {
        productsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            private void showPopup(MouseEvent e) {
                int row = productsTable.rowAtPoint(e.getPoint());
                if (row >= 0 && row < productsTable.getRowCount()) {
                    productsTable.setRowSelectionInterval(row, row);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
    
    private void deleteSelectedRow() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow >= 0) {
            DefaultTableModel model = (DefaultTableModel) productsTable.getModel();
            String prodName = model.getValueAt(selectedRow, 2).toString();
            boolean isDeleted = ProductController.deleteProduct(prodName);
            if (isDeleted) {
                model.removeRow(selectedRow);
                JOptionPane.showMessageDialog(this, "Product \"" + prodName + "\" deleted successfully", "Product deleted", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Product \"" + prodName + "\" could not be deleted", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void updateSelectedProduct(){
        if(!validateFile()){
            JOptionPane.showMessageDialog(this,"Either choose a Barcode file or uncheck the box","File not found",JOptionPane.ERROR_MESSAGE);
            return;
        }
        Product prod = new Product(prodIdField.getText(),prodNameField.getText(),barcodeFile,Integer.valueOf(boxWSR.getText()),Integer.valueOf(packageWSR.getText()),Integer.valueOf(pieceWSR.getText()),Integer.valueOf(boxRSR.getText()),Integer.valueOf(packageRSR.getText()),Integer.valueOf(pieceRSR.getText()));
        boolean isUploaded = ProductController.updateProduct(prod);
        if(isUploaded){
            JOptionPane.showMessageDialog(this,"Product \""+prod.getProdName()+"\" updated successfully","Product updated",JOptionPane.INFORMATION_MESSAGE);
            setVisible(false);
        }else{
            JOptionPane.showMessageDialog(this,"Product \""+prod.getProdName()+"\" was not updated","Failure",JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void promptUserToChooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JPEG & PNG Images", "jpeg", "jpg", "png");
        fileChooser.setFileFilter(filter);

        int returnValue = fileChooser.showDialog(this, "Select");
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                copyFileToAssets(selectedFile);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error copying file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void copyFileToAssets(File selectedFile) throws IOException {
        // Determine the destination directory within the project
        Path projectDir = new File(System.getProperty("user.dir")).toPath();
        Path assetsDir = projectDir.resolve("assets");

        // Create the assets directory if it doesn't exist
        if (!Files.exists(assetsDir)) {
            Files.createDirectory(assetsDir);
        }

        // Copy the file to the assets directory
        Path destinationPath = assetsDir.resolve(selectedFile.getName());
        Files.copy(selectedFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        // Set the barcodeFile to the new location
        barcodeFile = destinationPath.toFile();
        fileNameLabel.setText(barcodeFile.getName());
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        prodIdField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        prodNameField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        chooseFileBtn = new javax.swing.JButton();
        prodBarCodeCB = new javax.swing.JCheckBox();
        jLabel9 = new javax.swing.JLabel();
        boxWSR = new javax.swing.JFormattedTextField();
        jLabel10 = new javax.swing.JLabel();
        packageWSR = new javax.swing.JFormattedTextField();
        jLabel11 = new javax.swing.JLabel();
        pieceWSR = new javax.swing.JFormattedTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        pieceRSR = new javax.swing.JFormattedTextField();
        packageRSR = new javax.swing.JFormattedTextField();
        boxRSR = new javax.swing.JFormattedTextField();
        jSeparator2 = new javax.swing.JSeparator();
        confirmBtn = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        cancelBtn = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        productsTable = new javax.swing.JTable();
        clearBtn = new javax.swing.JButton();
        updateBtn = new javax.swing.JButton();
        fileNameLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Provide details of the new product");
        setAlwaysOnTop(true);
        setModal(true);
        setType(java.awt.Window.Type.POPUP);
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("Add New Product");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Product ID");

        prodIdField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prodIdFieldActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Product Name");

        prodNameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prodNameFieldActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Product Barcode");

        chooseFileBtn.setText("Choose file");
        chooseFileBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseFileBtnActionPerformed(evt);
            }
        });

        prodBarCodeCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prodBarCodeCBActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel9.setText("Box WS Rate");

        boxWSR.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        boxWSR.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                boxWSRFocusLost(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel10.setText("Package WS Rate");

        packageWSR.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        packageWSR.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                packageWSRFocusLost(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel11.setText("Piece WS Rate");

        pieceWSR.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        pieceWSR.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                pieceWSRFocusLost(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel12.setText("Box RS Rate");

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel13.setText("Package RS Rate");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel14.setText("Piece RS Rate");

        pieceRSR.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        pieceRSR.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                pieceRSRFocusLost(evt);
            }
        });

        packageRSR.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        packageRSR.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                packageRSRFocusLost(evt);
            }
        });

        boxRSR.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        boxRSR.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                boxRSRFocusLost(evt);
            }
        });

        confirmBtn.setText("Confirm");
        confirmBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmBtnActionPerformed(evt);
            }
        });

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        jLabel15.setText("Note: Wholesale (WS), Retail Sale (RS)");

        productsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "S/No.", "Prod. Id", "Product"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        productsTable.setRowHeight(30);
        productsTable.setRowMargin(10);
        productsTable.setShowGrid(true);
        productsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                productsTableMouseClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                productsTableMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(productsTable);
        if (productsTable.getColumnModel().getColumnCount() > 0) {
            productsTable.getColumnModel().getColumn(0).setResizable(false);
            productsTable.getColumnModel().getColumn(0).setPreferredWidth(5);
        }

        clearBtn.setText("Clear");
        clearBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearBtnActionPerformed(evt);
            }
        });

        updateBtn.setText("Update");
        updateBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateBtnActionPerformed(evt);
            }
        });

        fileNameLabel.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        fileNameLabel.setForeground(new java.awt.Color(51, 204, 0));
        fileNameLabel.setText(" ");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(prodNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(chooseFileBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(prodBarCodeCB)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fileNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(pieceWSR, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(boxWSR, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(packageWSR, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(35, 35, 35)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(pieceRSR, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(boxRSR, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(packageRSR, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(prodIdField, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                            .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(updateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(confirmBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jSeparator3)
                        .addComponent(jSeparator1)
                        .addComponent(jSeparator2)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 426, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                            .addComponent(clearBtn))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(clearBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(prodIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel6)
                                    .addComponent(prodNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(prodBarCodeCB, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel7)
                                        .addComponent(chooseFileBtn))))
                            .addComponent(fileNameLabel))
                        .addGap(18, 18, 18)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel9)
                                    .addComponent(boxWSR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel10)
                                    .addComponent(packageWSR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel11)
                                    .addComponent(pieceWSR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel12)
                                    .addComponent(boxRSR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel13)
                                    .addComponent(packageRSR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel14)
                                    .addComponent(pieceRSR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(confirmBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(updateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void prodIdFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prodIdFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_prodIdFieldActionPerformed

    private void prodNameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prodNameFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_prodNameFieldActionPerformed

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        // TODO add your handling code here:
        dispose();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void confirmBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmBtnActionPerformed
        // TODO add your handling code here:
        createNewProduct();
    }//GEN-LAST:event_confirmBtnActionPerformed

    private void prodBarCodeCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prodBarCodeCBActionPerformed
        // TODO add your handling code here:
        if(prodBarCodeCB.isSelected()){
            chooseFileBtn.setEnabled(true);
        }else{
            chooseFileBtn.setEnabled(false);
        }
    }//GEN-LAST:event_prodBarCodeCBActionPerformed

    private void productsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_productsTableMouseClicked
        // TODO add your handling code here:
        plotFieldsWithSelectedProduct();
    }//GEN-LAST:event_productsTableMouseClicked

    private void clearBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearBtnActionPerformed
        // TODO add your handling code here:
        defaultSettings();
    }//GEN-LAST:event_clearBtnActionPerformed

    private void productsTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_productsTableMouseReleased
        // TODO add your handling code here:
        
    }//GEN-LAST:event_productsTableMouseReleased

    private void updateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateBtnActionPerformed
        // TODO add your handling code here:
        updateSelectedProduct();
    }//GEN-LAST:event_updateBtnActionPerformed

    private void boxWSRFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_boxWSRFocusLost
        // TODO add your handling code here:
        if(boxWSR.getText().isBlank()){
            boxWSR.setText("0");
        }
    }//GEN-LAST:event_boxWSRFocusLost

    private void packageWSRFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_packageWSRFocusLost
        // TODO add your handling code here:
        if(packageWSR.getText().isBlank()){
            packageWSR.setText("0");
        }
    }//GEN-LAST:event_packageWSRFocusLost

    private void pieceWSRFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pieceWSRFocusLost
        // TODO add your handling code here:
        if(pieceWSR.getText().isBlank()){
            pieceWSR.setText("0");
        }
    }//GEN-LAST:event_pieceWSRFocusLost

    private void boxRSRFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_boxRSRFocusLost
        // TODO add your handling code here:
        if(boxRSR.getText().isBlank()){
            boxRSR.setText("0");
        }
    }//GEN-LAST:event_boxRSRFocusLost

    private void packageRSRFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_packageRSRFocusLost
        // TODO add your handling code here:
        if(packageRSR.getText().isBlank()){
            packageRSR.setText("0");
        }
    }//GEN-LAST:event_packageRSRFocusLost

    private void pieceRSRFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pieceRSRFocusLost
        // TODO add your handling code here:
        if(pieceRSR.getText().isBlank()){
            pieceRSR.setText("0");
        }
    }//GEN-LAST:event_pieceRSRFocusLost

    private void chooseFileBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseFileBtnActionPerformed
        // TODO add your handling code here:
        promptUserToChooseFile();
    }//GEN-LAST:event_chooseFileBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AddNewProductDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddNewProductDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddNewProductDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddNewProductDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AddNewProductDialog dialog = new AddNewProductDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField boxRSR;
    private javax.swing.JFormattedTextField boxWSR;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JButton chooseFileBtn;
    private javax.swing.JButton clearBtn;
    private javax.swing.JButton confirmBtn;
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JFormattedTextField packageRSR;
    private javax.swing.JFormattedTextField packageWSR;
    private javax.swing.JFormattedTextField pieceRSR;
    private javax.swing.JFormattedTextField pieceWSR;
    private javax.swing.JCheckBox prodBarCodeCB;
    private javax.swing.JTextField prodIdField;
    private javax.swing.JTextField prodNameField;
    private javax.swing.JTable productsTable;
    private javax.swing.JButton updateBtn;
    // End of variables declaration//GEN-END:variables
}
