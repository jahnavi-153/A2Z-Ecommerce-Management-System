import java.math.BigDecimal;

public class Product {

    private int id;
    private String name;
    private int categoryId;
    private String category;
    private String description;
    private BigDecimal price;
    private int stock;
    private int minStock;
    private String imagePath;
    private boolean active;

    public Product(int id, String name, int categoryId,
                   String category, String description,
                   BigDecimal price, int stock,
                   int minStock, String imagePath,
                   boolean active) {

        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.category = category;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.minStock = minStock;
        this.imagePath = imagePath;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public int getMinStock() {
        return minStock;
    }

    public String getImagePath() {
        return imagePath;
    }

    public boolean isActive() {
        return active;
    }

    public String getStatus() {

        if (!active) {
            return "Inactive";
        }

        if (stock == 0) {
            return "Out of Stock";
        }

        if (stock <= minStock) {
            return "Low Stock";
        }

        return "Available";
    }
}