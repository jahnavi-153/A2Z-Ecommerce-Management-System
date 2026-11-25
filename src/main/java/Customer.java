import java.math.BigDecimal;
import java.sql.Timestamp;

public class Customer {

    private int userId;
    private String name;
    private String email;
    private String phone;
    private Timestamp createdAt;
    private int totalOrders;
    private int deliveredOrders;
    private BigDecimal totalSpent;

    public Customer(int userId, String name,
                    String email, String phone,
                    Timestamp createdAt,
                    int totalOrders,
                    int deliveredOrders,
                    BigDecimal totalSpent) {

        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.createdAt = createdAt;
        this.totalOrders = totalOrders;
        this.deliveredOrders = deliveredOrders;
        this.totalSpent = totalSpent;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public int getDeliveredOrders() {
        return deliveredOrders;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }
}