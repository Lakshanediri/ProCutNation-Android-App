package com.s22010104.procutnation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

// NOTE: No changes needed for the class declaration or interfaces
public class ProfileFragment extends Fragment implements AchievementsAdapter.AchievementInteractionListener {

    private TextView textName, textEmail, textPoints, textXpLevel;
    private Button btnLogout, btnDeleteAccount;
    private CircleImageView profileImageView, analyticsButton;
    private ImageView editProfileImage, editNameIcon, editEmailIcon, editPasswordIcon;
    private RecyclerView achievementsRecyclerView;
    private AchievementsAdapter achievementsAdapter;
    private List<DocumentSnapshot> inventoryList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    // NOTE: We are no longer using FirebaseStorage for this, but the variable is kept as requested.
    // private FirebaseStorage storage;
    private ActivityResultLauncher<String> imagePickerLauncher;
    // NOTE: We no longer need the imageUri variable for uploads.
    // private Uri imageUri;
    private SwitchMaterial darkModeSwitch;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // --- CHANGED: The image picker now converts the image to Base64 and saves to Firestore ---
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            // Show a progress dialog while processing
                            ProgressDialog progressDialog = new ProgressDialog(getContext());
                            progressDialog.setTitle("Updating...");
                            progressDialog.show();

                            // Get the image as a Bitmap
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
                            // Convert the Bitmap to a compressed Base64 string
                            String base64Image = encodeBitmapToBase64(bitmap);
                            // Save the string to Firestore
                            saveBase64ImageToFirestore(base64Image, progressDialog);

                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // NOTE: Firebase Storage is not initialized here anymore for this feature.
        // storage = FirebaseStorage.getInstance();

        initializeViews(view);
        setupListeners();

        loadUserProfile();
        loadInventory();

        return view;
    }

    // --- NO CHANGES to initializeViews() or setupListeners() ---
    private void initializeViews(View view) {
        textName = view.findViewById(R.id.textName);
        textEmail = view.findViewById(R.id.textEmail);
        textPoints = view.findViewById(R.id.textPoints);
        textXpLevel = view.findViewById(R.id.textXpLevel);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);
        profileImageView = view.findViewById(R.id.profileImageView);
        editProfileImage = view.findViewById(R.id.editProfileImage);
        editNameIcon = view.findViewById(R.id.editNameIcon);
        editEmailIcon = view.findViewById(R.id.editEmailIcon);
        editPasswordIcon = view.findViewById(R.id.editPasswordIcon);
        analyticsButton = view.findViewById(R.id.analyticsButton);
        darkModeSwitch = view.findViewById(R.id.darkModeSwitch);

        achievementsRecyclerView = view.findViewById(R.id.achievementsRecyclerView);
        inventoryList = new ArrayList<>();
        achievementsAdapter = new AchievementsAdapter(getContext(), inventoryList, this);
        achievementsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        achievementsRecyclerView.setAdapter(achievementsAdapter);
    }
    private void setupListeners() {
        btnLogout.setOnClickListener(v -> logoutUser());
        btnDeleteAccount.setOnClickListener(v -> showDeleteConfirmationDialog());
        editProfileImage.setOnClickListener(v -> pickImageFromGallery());
        editNameIcon.setOnClickListener(v -> showUpdateDialog("Update Name", textName.getText().toString(), this::updateName));
        editPasswordIcon.setOnClickListener(v -> showUpdatePasswordDialog());
        editEmailIcon.setOnClickListener(v -> showReauthDialog(this::showUpdateEmailDialog));

        analyticsButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AnalyticsFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        darkModeSwitch.setChecked(sharedPreferences.getBoolean("isDarkMode", false));
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putBoolean("isDarkMode", true);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putBoolean("isDarkMode", false);
            }
            editor.apply();
        });
    }
    // --- END of unchanged section ---

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(doc -> {
                if (doc.exists() && getContext() != null) {
                    textName.setText(doc.getString("name"));
                    textEmail.setText(doc.getString("email"));
                    textPoints.setText("Points - " + doc.getLong("points"));
                    textXpLevel.setText("XP Level - " + doc.getLong("xpLevel"));

                    // --- CHANGED: Load the Base64 string and decode it ---
                    String imageBase64 = doc.getString("profileImageBase64");
                    if (imageBase64 != null && !imageBase64.isEmpty()) {
                        Bitmap bitmap = decodeBase64ToBitmap(imageBase64);
                        profileImageView.setImageBitmap(bitmap);
                    }
                }
            });
        }
    }

    // --- NO CHANGES to loadInventory() or onPetSelected() ---
    private void loadInventory() {
        if (mAuth.getCurrentUser() != null) {
            db.collection("users").document(mAuth.getUid()).collection("inventory")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            inventoryList.clear();
                            inventoryList.addAll(queryDocumentSnapshots.getDocuments());
                            achievementsAdapter.notifyDataSetChanged();
                        }
                    });
        }
    }
    @Override
    public void onPetSelected(String drawableName) {
        if (mAuth.getCurrentUser() != null) {
            db.collection("users").document(mAuth.getUid())
                    .update("activePetDrawableName", drawableName)
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Active pet updated!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to set active pet.", Toast.LENGTH_SHORT).show());
        }
    }
    // --- END of unchanged section ---


    private void pickImageFromGallery() {
        imagePickerLauncher.launch("image/*");
    }

    // --- DELETED: The old uploadProfilePicture() method is gone. ---

    // --- DELETED: The old saveProfileImageUrlToFirestore() method is gone. ---

    // --- NEW METHOD: Converts an image to a compressed Base64 string ---
    private String encodeBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Compress the image to JPEG format with 80% quality to reduce size
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] byteArray = baos.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // --- NEW METHOD: Decodes a Base64 string back into an image (Bitmap) ---
    private Bitmap decodeBase64ToBitmap(String base64Str) {
        byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    // --- NEW METHOD: Saves the Base64 string to Firestore ---
    private void saveBase64ImageToFirestore(String base64Image, ProgressDialog progressDialog) {
        if (mAuth.getCurrentUser() == null) {
            progressDialog.dismiss();
            return;
        }
        String userId = mAuth.getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        // Update the 'profileImageBase64' field in the user's document
        userRef.update("profileImageBase64", base64Image)
                .addOnSuccessListener(aVoid -> {
                    // Also update the image view immediately
                    Bitmap bitmap = decodeBase64ToBitmap(base64Image);
                    profileImageView.setImageBitmap(bitmap);
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Profile Picture Updated.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Failed to save image.", Toast.LENGTH_SHORT).show();
                });
    }


    // --- NO CHANGES to any of the methods below this line ---
    private void showUpdateDialog(String title, String currentValue, final UpdateCallback callback) {
        final EditText input = new EditText(getContext());
        input.setText(currentValue);
        new AlertDialog.Builder(getContext()).setTitle(title).setView(input)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newValue = input.getText().toString().trim();
                    if (!TextUtils.isEmpty(newValue)) callback.onUpdate(newValue);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel()).show();
    }
    private void updateName(String newName) {
        db.collection("users").document(mAuth.getUid()).update("name", newName)
                .addOnSuccessListener(aVoid -> loadUserProfile());
    }
    private void showReauthDialog(Runnable onReauthSuccess) {
        final EditText passwordInput = new EditText(getContext());
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new AlertDialog.Builder(getContext()).setTitle("Re-authentication Required").setMessage("Please enter your password.").setView(passwordInput)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String password = passwordInput.getText().toString();
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null && !password.isEmpty()) {
                        user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), password))
                                .addOnSuccessListener(aVoid -> onReauthSuccess.run())
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Re-authentication failed.", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Cancel", null).show();
    }
    private void showUpdateEmailDialog() {
        showUpdateDialog("Update Email", textEmail.getText().toString(), this::updateEmail);
    }
    private void updateEmail(String newEmail) {
        mAuth.getCurrentUser().verifyBeforeUpdateEmail(newEmail).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                db.collection("users").document(mAuth.getUid()).update("email", newEmail);
                Toast.makeText(getContext(), "Verification email sent.", Toast.LENGTH_LONG).show();
                logoutUser();
            }
        });
    }
    private void showUpdatePasswordDialog() {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText newPasswordInput = new EditText(getContext());
        newPasswordInput.setHint("New Password");
        newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPasswordInput);
        final EditText confirmPasswordInput = new EditText(getContext());
        confirmPasswordInput.setHint("Confirm New Password");
        confirmPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(confirmPasswordInput);
        new AlertDialog.Builder(getContext()).setTitle("Update Password").setView(layout)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newPassword = newPasswordInput.getText().toString();
                    String confirmPassword = confirmPasswordInput.getText().toString();
                    if (newPassword.equals(confirmPassword) && newPassword.length() >= 6) {
                        showReauthDialog(() -> updatePassword(newPassword));
                    } else {
                        Toast.makeText(getContext(), "Passwords do not match or are too short.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null).show();
    }
    private void updatePassword(String newPassword) {
        mAuth.getCurrentUser().updatePassword(newPassword).addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Password updated.", Toast.LENGTH_SHORT).show());
    }
    private void logoutUser() {
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Account")
                .setMessage("This is a sensitive action. Are you sure you want to permanently delete your account?")
                .setPositiveButton("Delete", (dialog, which) -> showReauthDialog(this::deleteUserAccount))
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void deleteUserAccount() {
        String userId = mAuth.getUid();
        db.collection("users").document(userId).delete()
                .addOnSuccessListener(aVoid -> mAuth.getCurrentUser().delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                        logoutUser();
                    } else {
                        Toast.makeText(getContext(), "Failed to delete account.", Toast.LENGTH_LONG).show();
                    }
                }));
    }
    interface UpdateCallback { void onUpdate(String value); }
}

