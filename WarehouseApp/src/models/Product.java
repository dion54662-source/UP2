package models;

public class Product {
    private int id;
    private String sku;
    private String name;
    private String location;
    private String barcode;

    public Product() {}

    public Product(String sku, String name, String location, String barcode) {
        this.sku = sku;
        this.name = name;
        this.location = location;
        this.barcode = barcode;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    @Override
    public String toString() {
        return sku + " - " + name + " [" + location + "]";
    }
}