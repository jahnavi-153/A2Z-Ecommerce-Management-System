import java.math.BigDecimal;

public class CartItem {

    private int productId;
    private String productName;
    private BigDecimal price;
    private int quantity;
    private int stock;
    private boolean active;

    public CartItem(int productId, String productName,
                    BigDecimal price, int quantity,
                    int stock, boolean active) {

        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.stock = stock;
        this.active = active;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getStock() {
        return stock;
    }

    public boolean isActive() {
        return active;
    }

    public BigDecimal getSubtotal() {

        return price.multiply(
                BigDecimal.valueOf(quantity));
    }

    public String getStatus() {

        if (!active) {
            return "Unavailable";
        }

        if (quantity > stock) {
            return "Insufficient Stock";
        }

        return "Available";
    }
}