public class InventoryTransaction {

    private int id;
    private String productName;
    private String type;
    private int quantity;
    private String reason;
    private String date;

    public InventoryTransaction(
            int id, String productName,
            String type, int quantity,
            String reason, String date) {

        this.id = id;
        this.productName = productName;
        this.type = type;
        this.quantity = quantity;
        this.reason = reason;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public String getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getReason() {
        return reason;
    }

    public String getDate() {
        return date;
    }
}