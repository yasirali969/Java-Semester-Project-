import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

// ================= DATABASE CONNECTION =================
class DB {
    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/SmartShop", "root", "1234");
    }
}

// ================= INTERFACES & OOP CLASSES =================
interface Purchasable {
    boolean buy(int qty);
}

abstract class Product implements Purchasable {
    protected int id;
    protected String name;
    protected double price;
    protected int qty;

    public Product(int id, String name, double price, int qty) {
        this.id = id; this.name = name; this.price = price; this.qty = qty;
    }

    public int getId(){ 
        return id;
   }
    public String getName() { 
        return name; 
    }
    public double getPrice(){ 
        return price; 
    }
    public int getQty(){ 
        return qty; 
    }
    public void setQty(int qty) { 
        this.qty = qty;
    }

    public boolean buy(int amount) {
        if (amount <= this.qty) {
            this.qty -= amount;
            return true;
        }
        return false;
    }
}

class ProductDetail {
    private String brand, size, color, material;
    public ProductDetail(String brand, String size, String color, String material) {
        this.brand = brand; this.size = size; this.color = color; this.material = material;
    }
    public String getBrand(){ 
        return brand; 
    }
    public String getSize(){ 
        return size; 
    }
    public String getColor(){ 
        return color; 
    }
    public String getMaterial(){ 
        return material;
    }
}

class Cloth extends Product {
    private String type;
    ProductDetail details; 

    public Cloth(int id, String name, String type, double price, int qty, ProductDetail details) {
        super(id, name, price, qty);
        this.type = type;
        this.details = details;
    }
    public String getType(){ 
        return type;
    }
}

// ================= CART SYSTEM =================
class CartItem {
    Product product;
    int qty;
    CartItem(Product p, int q) { 
        product = p; 
        qty = q; 
    }
    double total(){ 
        return product.getPrice()*qty; 
    }
}

class Cart {
    // Replaced ArrayList with a standard fixed-size array
    CartItem[] items = new CartItem[100]; 
    int itemCount = 0;

    void add(Product p, int q) {
        if (itemCount < items.length) {
            items[itemCount]=new CartItem(p, q);
            itemCount++;
        } else {
            JOptionPane.showMessageDialog(null, "Cart is full! Cannot add more items.");
        }
    }

    void checkout(String username) {
        if (itemCount == 0) return;

        double total = 0;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
        Date date = new Date();  
        
        StringBuilder receipt = new StringBuilder();
        receipt.append("====================================\n");
        receipt.append("        SMART SHOP RECEIPT          \n");
        receipt.append("====================================\n");
        receipt.append("Customer: ").append(username).append("\n");
        receipt.append("Date: ").append(formatter.format(date)).append("\n");
        receipt.append("------------------------------------\n");
        receipt.append(String.format("%-20s %-5s %-10s\n", "Item", "Qty", "Price"));
        receipt.append("------------------------------------\n");

        // Loop using the item counter instead of an iterator
        for (int i = 0; i < itemCount; i++) {
            CartItem item = items[i];
            item.product.buy(item.qty);
            double lineTotal =item.total();
            total+=lineTotal;
            
            receipt.append(String.format("%-20s %-5d $%-9.2f\n", item.product.getName(), item.qty, lineTotal));
            
            // Sync stock deduction to DB
            try (Connection conn = DB.getConnection();
                 PreparedStatement pst = conn.prepareStatement("UPDATE Products SET qty = ? WHERE id = ?")) {
                pst.setInt(1, item.product.getQty());
                pst.setInt(2, item.product.getId());
                pst.executeUpdate();
            } catch (Exception e) { e.printStackTrace(); }
        }
        
        receipt.append("------------------------------------\n");
        receipt.append(String.format("GRAND TOTAL:                   $%.2f\n", total));
        receipt.append("====================================\n");
        receipt.append("   Thank you for shopping with us!  \n");

        // Save Order to DB
        try (Connection conn = DB.getConnection();
             PreparedStatement pst = conn.prepareStatement("INSERT INTO OrderHistory (username, total_amount) VALUES (?, ?)")) {
            pst.setString(1, username);
            pst.setDouble(2, total);
            pst.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }

        // Clear the cart by resetting the counter
        itemCount = 0; 
        
        // Show Receipt GUI
        JTextArea receiptArea = new JTextArea(receipt.toString());
        receiptArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        receiptArea.setEditable(false);
        receiptArea.setBackground(new Color(250, 250, 250)); 
        receiptArea.setForeground(Color.BLACK);
        JOptionPane.showMessageDialog(null, new JScrollPane(receiptArea), "Checkout Complete", JOptionPane.INFORMATION_MESSAGE);
    }
}

// ================= GUI APPLICATION =================
class SmartShopGUI {

    // --- COLOR PALETTE ---
    private static final Color BG_COLOR = new Color(15, 15, 25);         
    private static final Color PANEL_COLOR = new Color(30, 35, 55);      
    private static final Color TEXT_COLOR = Color.WHITE;                 
    private static final Color BLUE_ACCENT = new Color(0, 168, 255);     
    private static final Color RED_ACCENT = new Color(255, 71, 87);      
    private static final Color SUCCESS_GREEN = new Color(46, 213, 115);  

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SmartShopGUI::showLoginScreen);
    }

    // --- UTILITY STYLING METHODS ---
    private static void styleButton(JButton btn, Color bgColor) {
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 2),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private static void styleLabel(JLabel lbl, int size) {
        lbl.setForeground(TEXT_COLOR);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, size));
    }

    private static void styleTextField(JTextField tf) {
        tf.setBackground(new Color(40, 45, 65));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(BLUE_ACCENT);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BLUE_ACCENT, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private static void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(28);
        table.setBackground(new Color(25, 30, 45));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(50, 60, 80));
        table.setSelectionBackground(BLUE_ACCENT);
        table.setSelectionForeground(Color.WHITE);
        
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(15, 20, 35));
        header.setForeground(BLUE_ACCENT);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBorder(BorderFactory.createLineBorder(BLUE_ACCENT, 1));
    }

    private static void stylePanel(JPanel panel, String title) {
        panel.setBackground(PANEL_COLOR);
        javax.swing.border.TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BLUE_ACCENT, 2), title
        );
        border.setTitleColor(BLUE_ACCENT);
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.setBorder(border);
    }

    // --- 1. LOGIN SCREEN ---
    private static void showLoginScreen() {
        JFrame frame = new JFrame("Smart Shopping Hub - Login");
        frame.setSize(420, 320);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(BG_COLOR);
        frame.setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("SMART SHOP", SwingConstants.CENTER);
        title.setForeground(BLUE_ACCENT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setBorder(BorderFactory.createEmptyBorder(6, 2, 5, 0));
        frame.add(title, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 20));
        inputPanel.setBackground(BG_COLOR);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 6, 20));

        JTextField userField = new JTextField(); styleTextField(userField);
        JPasswordField passField = new JPasswordField(); styleTextField(passField);
        
        JLabel userLbl = new JLabel(" Username:"); styleLabel(userLbl, 14);
        JLabel passLbl = new JLabel(" Password:"); styleLabel(passLbl, 14);
        
        inputPanel.add(userLbl); inputPanel.add(userField);
        inputPanel.add(passLbl); inputPanel.add(passField);
        frame.add(inputPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        btnPanel.setBackground(BG_COLOR);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));

        JButton adminLoginBtn = new JButton("Admin"); styleButton(adminLoginBtn, RED_ACCENT);
        JButton userLoginBtn = new JButton("Login"); styleButton(userLoginBtn, BLUE_ACCENT);
        JButton registerBtn = new JButton("Create Account"); styleButton(registerBtn, new Color(100, 100, 150));

        btnPanel.add(adminLoginBtn);
        btnPanel.add(userLoginBtn);
        btnPanel.add(new JLabel("")); 
        btnPanel.add(registerBtn);
        frame.add(btnPanel, BorderLayout.SOUTH);
                // Registration
        adminLoginBtn.addActionListener(e -> {
            if (userField.getText().equals("Admin") && new String(passField.getPassword()).equals("123")) {
                frame.dispose(); showAdminScreen();
            } else { JOptionPane.showMessageDialog(frame, "Invalid Admin Credentials", "Error", JOptionPane.ERROR_MESSAGE); }
        });

        userLoginBtn.addActionListener(e -> {
            String u = userField.getText(); String p = new String(passField.getPassword());
            if (u.isEmpty() || p.isEmpty()) { JOptionPane.showMessageDialog(frame, "Please enter details."); return; }
            try (Connection conn = DB.getConnection()) {
                PreparedStatement pst = conn.prepareStatement("SELECT * FROM Users WHERE username=? AND password=?");
                pst.setString(1, u); pst.setString(2, p);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) { frame.dispose(); showUserScreen(u); } 
                else { JOptionPane.showMessageDialog(frame, "Account not found. Please click 'Create Account'.", "Error", JOptionPane.WARNING_MESSAGE); }
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "Database Error! Check Connection."); }
        });

        registerBtn.addActionListener(e -> showRegisterDialog(frame));
        frame.setVisible(true);
    }

    // --- REGISTRATION DIALOG ---
    private static void showRegisterDialog(JFrame parent) {
        JDialog dialog = new JDialog(parent, "Create Account", true);
        dialog.setSize(320, 220);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new GridLayout(3, 2, 10, 15));
        dialog.getContentPane().setBackground(PANEL_COLOR);
        ((JPanel)dialog.getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField regUser = new JTextField(); styleTextField(regUser);
        JPasswordField regPass = new JPasswordField(); styleTextField(regPass);
        JButton submitBtn = new JButton("Register"); styleButton(submitBtn, BLUE_ACCENT);

        JLabel l1 = new JLabel(" New Username:"); styleLabel(l1, 14);
        JLabel l2 = new JLabel(" New Password:"); styleLabel(l2, 14);

        dialog.add(l1); dialog.add(regUser);
        dialog.add(l2); dialog.add(regPass);
        dialog.add(new JLabel("")); dialog.add(submitBtn);

        submitBtn.addActionListener(e -> {
            String u = regUser.getText(); String p = new String(regPass.getPassword());
            if (u.isEmpty() || p.isEmpty()) { JOptionPane.showMessageDialog(dialog, "Fields cannot be empty."); return; }
            try (Connection conn = DB.getConnection()) {
                PreparedStatement check = conn.prepareStatement("SELECT * FROM Users WHERE username=?");
                check.setString(1, u);
                if (check.executeQuery().next()) { JOptionPane.showMessageDialog(dialog, "Username already exists!"); } 
                else {
                    PreparedStatement insert = conn.prepareStatement("INSERT INTO Users (username, password) VALUES (?, ?)");
                    insert.setString(1, u); insert.setString(2, p);
                    insert.executeUpdate();
                    JOptionPane.showMessageDialog(dialog, "Account Created! You can now login.");
                    dialog.dispose();
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        dialog.setVisible(true);
    }

    // --- 2. ADMIN SCREEN ---
    private static void showAdminScreen() {
        JFrame frame = new JFrame("Admin Dashboard");
        frame.setSize(1050, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(BG_COLOR);
        ((JPanel)frame.getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"ID", "Name", "Type", "Price", "Qty", "Brand", "Size", "Color", "Material"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model); styleTable(table);
        loadProductsFromDB(model);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(BLUE_ACCENT, 2));

        JPanel formPanel = new JPanel(new GridLayout(10, 2, 5, 10));
        stylePanel(formPanel, "Product Management");
        
        JTextField idF = new JTextField(), nameF = new JTextField(), typeF = new JTextField();
        JTextField priceF = new JTextField(), qtyF = new JTextField(), brandF = new JTextField();
        JTextField sizeF = new JTextField(), colorF = new JTextField(), materialF = new JTextField();
        
        JLabel[] labels = {new JLabel(" ID:"), new JLabel(" Name:"), new JLabel(" Type:"), new JLabel(" Price:"), 
                           new JLabel(" Qty:"), new JLabel(" Brand:"), new JLabel(" Size:"), new JLabel(" Color:"), new JLabel(" Material:")};
        JTextField[] fields = {idF, nameF, typeF, priceF, qtyF, brandF, sizeF, colorF, materialF};

        for (int i = 0; i < labels.length; i++) {
            styleLabel(labels[i], 13); styleTextField(fields[i]);
            formPanel.add(labels[i]); formPanel.add(fields[i]);
        }

        JPanel btnPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        btnPanel.setBackground(BG_COLOR);
        JButton addBtn = new JButton("Add Product"); 
        styleButton(addBtn, BLUE_ACCENT);
        JButton updateBtn = new JButton("Update Product"); 
        styleButton(updateBtn, new Color(156, 39, 176)); 
        JButton deleteBtn = new JButton("Delete Selected"); 
        styleButton(deleteBtn, RED_ACCENT);
        btnPanel.add(addBtn); btnPanel.add(updateBtn); 
        btnPanel.add(deleteBtn);

        frame.add(formPanel, BorderLayout.WEST);
        frame.add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BG_COLOR);
        bottomPanel.add(btnPanel, BorderLayout.WEST);
        JButton logoutBtn = new JButton("Logout"); 
        styleButton(logoutBtn, RED_ACCENT.darker());
        bottomPanel.add(logoutBtn, BorderLayout.EAST);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int r = table.getSelectedRow();
                for (int i=0; i<9; i++) { fields[i].setText(model.getValueAt(r, i) != null ? model.getValueAt(r, i).toString() : ""); }
            }
        });

        addBtn.addActionListener(e -> {
            try (Connection conn = DB.getConnection()) {
                PreparedStatement p1 = conn.prepareStatement("INSERT INTO Products VALUES (?, ?, ?, ?, ?)");
                p1.setInt(1, Integer.parseInt(idF.getText())); 
                p1.setString(2, nameF.getText());
                p1.setString(3, typeF.getText()); 
                p1.setDouble(4, Double.parseDouble(priceF.getText()));
                p1.setInt(5, Integer.parseInt(qtyF.getText())); 
                p1.executeUpdate();

                PreparedStatement p2 = conn.prepareStatement("INSERT INTO ProductDetails VALUES (?, ?, ?, ?, ?)");
                p2.setInt(1, Integer.parseInt(idF.getText()));
                p2.setString(2, brandF.getText());
                p2.setString(3, sizeF.getText());
                p2.setString(4, colorF.getText());
                p2.setString(5, materialF.getText());
                p2.executeUpdate();

                JOptionPane.showMessageDialog(frame, "Product Added!"); loadProductsFromDB(model); 
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage()); }
        });

        updateBtn.addActionListener(e -> {
            try (Connection conn = DB.getConnection()) {
                int id = Integer.parseInt(idF.getText());
                PreparedStatement p1 = conn.prepareStatement("UPDATE Products SET name=?, type=?, price=?, qty=? WHERE id=?");
                p1.setString(1, nameF.getText());
                p1.setString(2, typeF.getText());
                p1.setDouble(3, Double.parseDouble(priceF.getText()));
                p1.setInt(4, Integer.parseInt(qtyF.getText()));
                p1.setInt(5, id); p1.executeUpdate();

                PreparedStatement p2 = conn.prepareStatement("UPDATE ProductDetails SET brand=?, size=?, color=?, material=? WHERE product_id=?");
                p2.setString(1, brandF.getText());
                p2.setString(2, sizeF.getText());
                p2.setString(3, colorF.getText());
                p2.setString(4, materialF.getText());
                p2.setInt(5, id); p2.executeUpdate();

                JOptionPane.showMessageDialog(frame, "Product Updated!"); loadProductsFromDB(model);
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "Error updating: " + ex.getMessage()); }
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int id = (int) model.getValueAt(row, 0);
                try (Connection conn = DB.getConnection();
                     PreparedStatement pst = conn.prepareStatement("DELETE FROM Products WHERE id=?")) {
                    pst.setInt(1, id); pst.executeUpdate(); loadProductsFromDB(model);
                    JOptionPane.showMessageDialog(frame, "Product Deleted!");
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        logoutBtn.addActionListener(e -> { frame.dispose(); showLoginScreen(); });
        frame.setVisible(true);
    }

    // --- 3. USER SCREEN ---
    private static void showUserScreen(String username) {
        JFrame frame = new JFrame("Customer Portal - Welcome, " + username);
        frame.setSize(1150, 850);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridLayout(2, 1, 10, 10));
        frame.getContentPane().setBackground(BG_COLOR);
        ((JPanel)frame.getContentPane()).setBorder(BorderFactory.createEmptyBorder(13, 13, 10, 10));

        Cart userCart = new Cart();
        
        // Replaced ArrayList with a standard array for Available Products
        Product[] availableProducts = new Product[100];
        int[] productCount = {0}; // Using array hack to allow modification inside lambda if needed

        String[] pCols = {"ID", "Name", "Price", "Stock", "Details (Brand/Color)"};
        DefaultTableModel pModel = new DefaultTableModel(pCols, 0);
        JTable pTable = new JTable(pModel);
        styleTable(pTable);
        JScrollPane pScroll = new JScrollPane(pTable);
         pScroll.getViewport().setBackground(BG_COLOR);
        
        try (Connection conn = DB.getConnection(); Statement st = conn.createStatement(); 
             ResultSet rs = st.executeQuery("SELECT p.id, p.name, p.type, p.price, p.qty, d.brand, d.color FROM Products p LEFT JOIN ProductDetails d ON p.id = d.product_id")) {
            while (rs.next() && productCount[0] < availableProducts.length) {
                Product p = new Cloth(rs.getInt("id"), rs.getString("name"), rs.getString("type"), rs.getDouble("price"), rs.getInt("qty"), null);
                availableProducts[productCount[0]] = p;
                productCount[0]++;
                
                String details = rs.getString("brand") + " - " + rs.getString("color");
                pModel.addRow(new Object[]{p.getId(), p.getName(), "$" + p.getPrice(), p.getQty(), details});
            }
        } catch (Exception e) { e.printStackTrace(); }

        JPanel pPanel = new JPanel(new BorderLayout(5, 5));
         stylePanel(pPanel, "Available Store Items");
        pPanel.add(pScroll, BorderLayout.CENTER);
        JButton addCartBtn = new JButton("Add Selected to Cart");
         styleButton(addCartBtn, BLUE_ACCENT);
        pPanel.add(addCartBtn, BorderLayout.SOUTH);

        String[] cCols = {"Product", "Qty", "Total Price"};
        DefaultTableModel cModel = new DefaultTableModel(cCols, 0);
        JTable cTable = new JTable(cModel); styleTable(cTable);
        JScrollPane cScroll = new JScrollPane(cTable); cScroll.getViewport().setBackground(BG_COLOR);

        JPanel cPanel = new JPanel(new BorderLayout(5, 5)); stylePanel(cPanel, "Your Shopping Cart");
        cPanel.add(cScroll, BorderLayout.CENTER);

        JPanel cBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        cBottom.setBackground(PANEL_COLOR);
        JButton checkoutBtn = new JButton("Checkout All Items"); styleButton(checkoutBtn, SUCCESS_GREEN);
        JButton logoutBtn = new JButton("Logout"); 
        styleButton(logoutBtn, RED_ACCENT);
        cBottom.add(checkoutBtn); cBottom.add(logoutBtn);
        cPanel.add(cBottom, BorderLayout.SOUTH);

        addCartBtn.addActionListener(e -> {
            int row = pTable.getSelectedRow();
            if (row >= 0 && row < productCount[0]) {
                // Fetch from standard array
                Product selected = availableProducts[row];
                
                UIManager.put("OptionPane.background", BG_COLOR);
                UIManager.put("Panel.background", BG_COLOR);
                UIManager.put("OptionPane.messageForeground", Color.WHITE);
                
                String qtyStr = JOptionPane.showInputDialog(frame, "Enter Quantity to Buy:");
                if (qtyStr != null && !qtyStr.trim().isEmpty()) {
                    try {
                        int q = Integer.parseInt(qtyStr);
                        if (selected.getQty() >= q && q > 0) {
                            userCart.add(selected, q);
                            selected.setQty(selected.getQty() - q); 
                            pModel.setValueAt(selected.getQty(), row, 3); 
                            cModel.addRow(new Object[]{selected.getName(), q, "$" + (selected.getPrice() * q)});
                        } else { JOptionPane.showMessageDialog(frame, "Not enough stock available!"); }
                    } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(frame, "Invalid quantity number."); }
                }
            } else { JOptionPane.showMessageDialog(frame, "Please select a product from the list first."); }
        });

        checkoutBtn.addActionListener(e -> {
            if (userCart.itemCount == 0) { JOptionPane.showMessageDialog(frame, "Your cart is empty!"); return; }
            userCart.checkout(username);
            cModel.setRowCount(0); 
        });

        logoutBtn.addActionListener(e -> { frame.dispose(); showLoginScreen(); });

        frame.add(pPanel); frame.add(cPanel);
        frame.setVisible(true);
    }

    // --- HELPER METHOD ---
    private static void loadProductsFromDB(DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DB.getConnection(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT p.id, p.name, p.type, p.price, p.qty, d.brand, d.size, d.color, d.material FROM Products p LEFT JOIN ProductDetails d ON p.id = d.product_id")) {
            while (rs.next()) {
                model.addRow(new Object[]{ rs.getInt("id"), rs.getString("name"), rs.getString("type"), rs.getDouble("price"), rs.getInt("qty"), rs.getString("brand"), rs.getString("size"), rs.getString("color"), rs.getString("material") });
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }
}