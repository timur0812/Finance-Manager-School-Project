package timur.popov.financemanager.model;

/**
 * Transaction class with all corresponding fields.
 * Also, fields in this class are bound to columns in transaction table
 */
public class Transaction {
    private int id;
    private String type;
    private String category;
    private double amount;
    private String date;
    private String user;
    private String account; 
    private String description;

    /**
     * Used for transactions that are going to be inserted into database
     * Id doesn't need because, it will auto increment in database
     */
    public Transaction(String type, String category, double amount, String date, String user, String account, String description) {
        this.id = -1;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.user = user;
        this.account = account;
        this.description = description;
    }
    //Used for categories that are loaded from database
    public Transaction(int id, String type, String category, double amount, String date, String user, String account, String description) {
        this.id = id;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.user = user;
        this.account = account;
        this.description = description;
    }

    
    public String getType() { return type; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public String getUser() { return user; }
    public String getAccount() { return account; }
    public String getDescription() { return description; }
    public int getId() { return id; }

    
    public void setType(String type) { this.type = type; }
    public void setCategory(String category) { this.category = category; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDate(String date) { this.date = date; }
    public void setUser(String user) { this.user = user; }
    public void setAccount(String account) { this.account = account; }
    public void setDescription(String description) { this.description = description; }
    public void setId(int id) { this.id = id; }
}