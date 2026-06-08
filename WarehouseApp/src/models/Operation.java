package models;

public class Operation {
    private int id;
    private String sku;
    private String fromLoc;
    private String toLoc;
    private String user;
    private String timestamp;
    private String operationType;

    public Operation() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getFromLoc() { return fromLoc; }
    public void setFromLoc(String fromLoc) { this.fromLoc = fromLoc; }

    public String getToLoc() { return toLoc; }
    public void setToLoc(String toLoc) { this.toLoc = toLoc; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
}