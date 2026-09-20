package za.ac.richfield.smartpantry;

public class AuditLogItem {
    public long id;
    public String actionType;
    public String itemName;
    public String details;
    public String timestamp;

    public AuditLogItem(long id, String actionType, String itemName, String details, String timestamp) {
        this.id = id;
        this.actionType = actionType;
        this.itemName = itemName;
        this.details = details;
        this.timestamp = timestamp;
    }
}
