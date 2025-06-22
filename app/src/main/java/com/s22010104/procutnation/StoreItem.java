package com.s22010104.procutnation;

public class StoreItem {
    private String itemId;
    private String name;
    private int price;
    private int imageDrawableId;
    private String drawableName;

    public StoreItem(String itemId, String name, int price, int imageDrawableId, String drawableName) {
        this.itemId = itemId;
        this.name = name;
        this.price = price;
        this.imageDrawableId = imageDrawableId;
        this.drawableName = drawableName;
    }

    // Getters
    public String getItemId() { return itemId; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getImageDrawableId() { return imageDrawableId; }
    public String getDrawableName() { return drawableName; }
}

