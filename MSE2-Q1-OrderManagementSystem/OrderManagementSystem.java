import java.sql.*;
import java.util.Scanner;

public class OrderManagementSystem {

    private static final String URL = "jdbc:mysql://localhost:3306/ecommerce_db";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // a) Insert at least three order records using PreparedStatement
    public static void insertOrders(Connection conn) throws SQLException {
        String sql = "INSERT INTO Orders (order_id, customer_name, product_name, quantity, unit_price, status) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);

        Object[][] orders = {
            {1, "Rahul Sharma", "Laptop", 3, 55000.00, "Pending"},
            {2, "Priya Verma", "Headphones", 5, 1500.00, "Shipped"},
            {3, "Amit Kumar", "Keyboard", 2, 2500.00, "Delivered"},
            {4, "Sneha Patil", "Mouse", 4, 800.00, "Cancelled"},
            {5, "Vikas Joshi", "Monitor", 1, 18000.00, "Pending"}
        };

        for (Object[] order : orders) {
            try {
                pstmt.setInt(1, (int) order[0]);
                pstmt.setString(2, (String) order[1]);
                pstmt.setString(3, (String) order[2]);
                pstmt.setInt(4, (int) order[3]);
                pstmt.setDouble(5, (double) order[4]);
                pstmt.setString(6, (String) order[5]);
                pstmt.executeUpdate();
                System.out.println("Inserted order ID: " + order[0]);
            } catch (SQLIntegrityConstraintViolationException e) {
                System.out.println("Duplicate order ID " + order[0] + " — skipping insertion.");
            }
        }
        pstmt.close();
        System.out.println("Order insertion completed.\n");
    }

    // b) Display orders by status with quantity > 2, sorted by unit_price DESC
    public static void displayOrdersByStatus(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Enter order status to search: ");
        String status = sc.nextLine();

        String sql = "SELECT * FROM Orders WHERE status = ? AND quantity > 2 ORDER BY unit_price DESC";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, status);
        ResultSet rs = pstmt.executeQuery();

        boolean found = false;
        System.out.println("\n--- Orders with status '" + status + "' and quantity > 2 ---");
        System.out.printf("%-10s %-20s %-20s %-10s %-12s %-15s%n",
                "Order ID", "Customer Name", "Product Name", "Quantity", "Unit Price", "Status");
        System.out.println("-".repeat(90));

        while (rs.next()) {
            found = true;
            System.out.printf("%-10d %-20s %-20s %-10d %-12.2f %-15s%n",
                    rs.getInt("order_id"),
                    rs.getString("customer_name"),
                    rs.getString("product_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("unit_price"),
                    rs.getString("status"));
        }
        if (!found) {
            System.out.println("No orders found with status '" + status + "' and quantity > 2.");
        }
        rs.close();
        pstmt.close();
        System.out.println();
    }

    // c) Increase quantity of a specific order by a given amount
    public static void increaseQuantity(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Enter order ID: ");
        int orderId = Integer.parseInt(sc.nextLine());
        System.out.print("Enter additional quantity to add: ");
        int addQty = Integer.parseInt(sc.nextLine());

        String sql = "UPDATE Orders SET quantity = quantity + ? WHERE order_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, addQty);
        pstmt.setInt(2, orderId);
        int rows = pstmt.executeUpdate();

        if (rows > 0) {
            System.out.println("Quantity updated successfully for order ID: " + orderId);
        } else {
            System.out.println("Order ID " + orderId + " does not exist.");
        }
        pstmt.close();
        System.out.println();
    }

    // d) Update status of an order if it exists and new status is valid
    public static void updateOrderStatus(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Enter order ID: ");
        int orderId = Integer.parseInt(sc.nextLine());
        System.out.print("Enter new status (Pending/Shipped/Delivered): ");
        String newStatus = sc.nextLine();

        if (!newStatus.equals("Pending") && !newStatus.equals("Shipped") && !newStatus.equals("Delivered")) {
            System.out.println("Invalid status! Allowed values: Pending, Shipped, Delivered.\n");
            return;
        }

        // Check if order exists
        String selectSql = "SELECT * FROM Orders WHERE order_id = ?";
        PreparedStatement selectStmt = conn.prepareStatement(selectSql);
        selectStmt.setInt(1, orderId);
        ResultSet rs = selectStmt.executeQuery();

        if (!rs.next()) {
            System.out.println("Order ID " + orderId + " does not exist.\n");
            rs.close();
            selectStmt.close();
            return;
        }
        rs.close();
        selectStmt.close();

        // Update status
        String updateSql = "UPDATE Orders SET status = ? WHERE order_id = ?";
        PreparedStatement updateStmt = conn.prepareStatement(updateSql);
        updateStmt.setString(1, newStatus);
        updateStmt.setInt(2, orderId);
        updateStmt.executeUpdate();
        updateStmt.close();

        // Display updated order details
        PreparedStatement dispStmt = conn.prepareStatement("SELECT * FROM Orders WHERE order_id = ?");
        dispStmt.setInt(1, orderId);
        ResultSet updatedRs = dispStmt.executeQuery();

        System.out.println("\n--- Updated Order Details ---");
        System.out.printf("%-10s %-20s %-20s %-10s %-12s %-15s%n",
                "Order ID", "Customer Name", "Product Name", "Quantity", "Unit Price", "Status");
        System.out.println("-".repeat(90));
        if (updatedRs.next()) {
            System.out.printf("%-10d %-20s %-20s %-10d %-12.2f %-15s%n",
                    updatedRs.getInt("order_id"),
                    updatedRs.getString("customer_name"),
                    updatedRs.getString("product_name"),
                    updatedRs.getInt("quantity"),
                    updatedRs.getDouble("unit_price"),
                    updatedRs.getString("status"));
        }
        updatedRs.close();
        dispStmt.close();
        System.out.println();
    }

    // e) Display total order value and delete order by ID if status is Cancelled
    public static void displayTotalAndDelete(Connection conn, Scanner sc) throws SQLException {
        // Display total order value
        String sumSql = "SELECT SUM(quantity * unit_price) AS total_value FROM Orders";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sumSql);
        if (rs.next()) {
            System.out.println("Total Order Value: Rs. " + rs.getDouble("total_value"));
        }
        rs.close();
        stmt.close();

        // Delete order if status is Cancelled
        System.out.print("\nEnter order ID to delete: ");
        int orderId = Integer.parseInt(sc.nextLine());

        String checkSql = "SELECT status FROM Orders WHERE order_id = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        checkStmt.setInt(1, orderId);
        ResultSet checkRs = checkStmt.executeQuery();

        if (!checkRs.next()) {
            System.out.println("Order ID " + orderId + " does not exist.");
        } else {
            String status = checkRs.getString("status");
            if (status.equals("Cancelled")) {
                String deleteSql = "DELETE FROM Orders WHERE order_id = ?";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                deleteStmt.setInt(1, orderId);
                deleteStmt.executeUpdate();
                deleteStmt.close();
                System.out.println("Order ID " + orderId + " deleted successfully.");
            } else {
                System.out.println("Order ID " + orderId + " cannot be deleted. Status is '" + status + "', not 'Cancelled'.");
            }
        }
        checkRs.close();
        checkStmt.close();
        System.out.println();
    }

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = getConnection();
            System.out.println("Connected to database successfully!\n");

            Scanner sc = new Scanner(System.in);
            int choice;

            do {
                System.out.println("===== Order Management System =====");
                System.out.println("1. Insert Orders");
                System.out.println("2. Display Orders by Status (quantity > 2)");
                System.out.println("3. Increase Order Quantity");
                System.out.println("4. Update Order Status");
                System.out.println("5. Display Total Value & Delete Cancelled Order");
                System.out.println("6. Exit");
                System.out.print("Enter your choice: ");
                choice = Integer.parseInt(sc.nextLine());
                System.out.println();

                switch (choice) {
                    case 1: insertOrders(conn); break;
                    case 2: displayOrdersByStatus(conn, sc); break;
                    case 3: increaseQuantity(conn, sc); break;
                    case 4: updateOrderStatus(conn, sc); break;
                    case 5: displayTotalAndDelete(conn, sc); break;
                    case 6: System.out.println("Exiting... Goodbye!"); break;
                    default: System.out.println("Invalid choice! Try again.\n");
                }
            } while (choice != 6);

            sc.close();
            conn.close();

        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
