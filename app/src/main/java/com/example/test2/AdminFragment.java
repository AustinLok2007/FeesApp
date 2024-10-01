package com.example.test2;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdminFragment extends Fragment {

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_STORAGE_PERMISSION = 201;
    private static final String TAG = "AdminFragment";

    private EditText emailEditText, itemNameEditText, priceEditText;
    private Button searchButton, sendInvoiceButton, takePictureButton;
    private ImageView imageView;
    private Uri imageUri;
    private FirebaseAuth mAuth;
    private CollectionReference usersRef, invoicesRef;
    private StorageReference storageRef;
    private User selectedUser;
    private ProgressBar progressBar;
    private String imagePath;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        emailEditText = view.findViewById(R.id.emailEditText);
        searchButton = view.findViewById(R.id.searchButton);
        sendInvoiceButton = view.findViewById(R.id.sendInvoiceButton);
        itemNameEditText = view.findViewById(R.id.itemName);
        priceEditText = view.findViewById(R.id.itemPrice);
        takePictureButton = view.findViewById(R.id.takePictureButton);
        imageView = view.findViewById(R.id.imageView);
        progressBar = view.findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseFirestore.getInstance().collection("users");
        invoicesRef = FirebaseFirestore.getInstance().collection("invoices");
        storageRef = FirebaseStorage.getInstance().getReference();

        searchButton.setOnClickListener(v -> searchUser());
        sendInvoiceButton.setOnClickListener(v -> sendInvoice());
        takePictureButton.setOnClickListener(v -> checkPermissionsAndTakePicture());

        return view;
    }

    private void checkPermissionsAndTakePicture() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, REQUEST_CAMERA_PERMISSION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA_PERMISSION && resultCode == getActivity().RESULT_OK) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);
                uploadImageToFirebase();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed to capture image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            progressBar.setVisibility(View.VISIBLE);
            imagePath = "images/" + System.currentTimeMillis() + ".jpg";
            StorageReference fileRef = storageRef.child(imagePath);
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(getContext()).load(uri).into(imageView);
                                    Toast.makeText(getContext(), "Image uploaded", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Upload failed: " + e.getMessage(), e);
                            Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private void searchUser() {
        String email = emailEditText.getText().toString().trim();
        usersRef.whereEqualTo("email", email).addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(getContext(), "Failed to search user: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (value.isEmpty()) {
                Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!value.isEmpty()) {
                selectedUser = value.getDocuments().get(0).toObject(User.class);
                Toast.makeText(getContext(), "User found", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendInvoice() {
        if (selectedUser == null) {
            Toast.makeText(getContext(), "No user selected", Toast.LENGTH_SHORT).show();
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String time = now.format(DateTimeFormatter.ofPattern("hh:mm a"));

        String itemName = itemNameEditText.getText().toString().trim();
        double price = priceEditText.getText().toString().trim().isEmpty() ? 0 : Double.parseDouble(priceEditText.getText().toString().trim());
        String photoUrl = imagePath != null ? imagePath : "default/image/path";

        Invoice invoice = new Invoice(date, time, itemName, price, photoUrl, selectedUser.getUid());
        invoicesRef.add(invoice).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Invoice sent", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to send invoice: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getContext(), "Camera and Storage permission are required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
