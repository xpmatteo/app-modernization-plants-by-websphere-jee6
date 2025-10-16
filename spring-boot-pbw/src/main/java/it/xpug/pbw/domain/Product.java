// ABOUTME: Domain model representing a product from the INVENTORY table
// ABOUTME: Simple POJO with product details like name, price, description, category
package it.xpug.pbw.domain;

public class Product {
    private String inventoryId;
    private String name;
    private String heading;
    private String description;
    private String pkginfo;
    private String image;
    private float price;
    private float cost;
    private int quantity;
    private int category;
    private String notes;
    private boolean isPublic;

    public Product() {
    }

    public Product(String inventoryId, String name, String heading, String description,
                   String pkginfo, String image, float price, float cost, int quantity,
                   int category, String notes, boolean isPublic) {
        this.inventoryId = inventoryId;
        this.name = name;
        this.heading = heading;
        this.description = description;
        this.pkginfo = pkginfo;
        this.image = image;
        this.price = price;
        this.cost = cost;
        this.quantity = quantity;
        this.category = category;
        this.notes = notes;
        this.isPublic = isPublic;
    }

    // Getters and Setters

    public String getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(String inventoryId) {
        this.inventoryId = inventoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPkginfo() {
        return pkginfo;
    }

    public void setPkginfo(String pkginfo) {
        this.pkginfo = pkginfo;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    /**
     * Get the category name as a string (Flowers, Trees, etc.)
     */
    public String getCategoryName() {
        switch (category) {
            case 0: return "Flowers";
            case 1: return "Fruits & Vegetables";
            case 2: return "Trees";
            case 3: return "Accessories";
            default: return "Unknown";
        }
    }

    /**
     * Get formatted price with currency symbol
     */
    public String getFormattedPrice() {
        return String.format("$%.2f", price);
    }
}
