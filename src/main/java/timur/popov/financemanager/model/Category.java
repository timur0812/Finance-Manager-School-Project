package timur.popov.financemanager.model;

/**
 * Category class is used for keeping information about specific category
 */
public class Category {
    private int categoryId;
    private String type; 
    private String name;

    /**
     * Used for categories that are going to be inserted into database
     * Id doesn't need because, it will auto increment in database
     */
    public Category(String type, String name) {
        this.categoryId = -1;
        this.type = type;
        this.name = name;
    }
    //Used for categories that are loaded from database
    public Category(int categoryId, String type, String name) {
        this.categoryId = categoryId;
        this.type = type;
        this.name = name;
    }

    
    public String getType() {
        return type;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    
    public void setType(String type) { this.type = type; }
    public void setName(String name) { this.name = name; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
}