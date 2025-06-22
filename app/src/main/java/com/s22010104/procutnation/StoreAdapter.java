package com.s22010104.procutnation;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

    private List<StoreItem> storeItemList;
    private Context context;
    private String[] backgroundColors = {"#FFC107", "#4CAF50", "#9C27B0", "#03A9F4", "#FF5722", "#009688"};

    public StoreAdapter(Context context, List<StoreItem> storeItemList) {
        this.context = context;
        this.storeItemList = storeItemList;
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_store, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        StoreItem item = storeItemList.get(position);
        holder.itemName.setText(item.getName());
        holder.itemPrice.setText(item.getPrice() + " Points");
        holder.itemImage.setImageResource(item.getImageDrawableId());

        holder.background.setBackgroundColor(Color.parseColor(backgroundColors[position % backgroundColors.length]));
        holder.buyButton.setOnClickListener(v -> purchaseItem(item));
    }

    @Override
    public int getItemCount() {
        return storeItemList.size();
    }

    private void purchaseItem(StoreItem item) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        db.runTransaction(transaction -> {
            long currentPoints = transaction.get(userRef).getLong("points");
            if (currentPoints < item.getPrice()) {
                throw new IllegalStateException("Not enough points.");
            }
            transaction.update(userRef, "points", FieldValue.increment(-item.getPrice()));

            Map<String, Object> inventoryItem = new HashMap<>();
            inventoryItem.put("itemId", item.getItemId());
            inventoryItem.put("itemName", item.getName());
            inventoryItem.put("drawableName", item.getDrawableName());
            inventoryItem.put("purchaseDate", FieldValue.serverTimestamp());
            transaction.set(userRef.collection("inventory").document(), inventoryItem);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(context, item.getName() + " purchased!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Purchase failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    static class StoreViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemName, itemPrice;
        Button buyButton;
        ConstraintLayout background;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImageView);
            itemName = itemView.findViewById(R.id.itemNameTextView);
            itemPrice = itemView.findViewById(R.id.itemPriceTextView);
            buyButton = itemView.findViewById(R.id.buyButton);
            background = itemView.findViewById(R.id.item_background);
        }
    }
}