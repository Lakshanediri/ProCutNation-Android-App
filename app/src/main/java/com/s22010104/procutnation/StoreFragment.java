package com.s22010104.procutnation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class StoreFragment extends Fragment {

    private RecyclerView storeRecyclerView;
    private StoreAdapter storeAdapter;
    private List<StoreItem> storeItemList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_store, container, false);

        storeRecyclerView = view.findViewById(R.id.storeRecyclerView);
        storeRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        loadStoreItems();

        storeAdapter = new StoreAdapter(getContext(), storeItemList);
        storeRecyclerView.setAdapter(storeAdapter);

        return view;
    }

    private void loadStoreItems() {
        storeItemList = new ArrayList<>();

        storeItemList.add(new StoreItem("dino001", "Dino Pet", 20, R.drawable.dino_pet_orange, "dino_pet_orange"));
        storeItemList.add(new StoreItem("dino002", "Dino Pet", 50, R.drawable.dino_pet_blue, "dino_pet_blue"));
        storeItemList.add(new StoreItem("dino003", "Dino Pet", 100, R.drawable.dino_pet_purple, "dino_pet_purple"));
        storeItemList.add(new StoreItem("dino004", "Dino Pet", 200, R.drawable.dino_pet_green_spiky, "dino_pet_green_spiky"));
        storeItemList.add(new StoreItem("dino005", "Dino Pet", 300, R.drawable.dino_pet_green_bunny, "dino_pet_green_bunny"));
        storeItemList.add(new StoreItem("dino006", "Dino Pet", 500, R.drawable.dino_pet_green_tall, "dino_pet_green_tall"));
    }
}
