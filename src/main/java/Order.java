import java.math.BigDecimal;
import java.sql.Timestamp;

public class Order {

    private int orderId;
    private int userId;
    private String customerName;
    private Timestamp orderDate;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private String status;
    private String paymentMethod;
    private String paymentStatus;

    public Order(int orderId, int userId,
                 String customerName, Timestamp orderDate,
                 BigDecimal totalAmount,
                 String deliveryAddress, String status,
                 String paymentMethod, String paymentStatus) {

        this.orderId = orderId;
        this.userId = userId;
        this.customerName = customerName;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getUserId() {
        return userId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Timestamp getOrderDate() {
        return orderDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }
}