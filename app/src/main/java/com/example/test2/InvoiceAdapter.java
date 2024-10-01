package com.example.test2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder> {

    private List<Invoice> invoiceList;
    private FirebaseStorage firebaseStorage;

    public InvoiceAdapter(List<Invoice> invoiceList) {
        this.invoiceList = invoiceList;
        this.firebaseStorage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice, parent, false);
        return new InvoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        Invoice invoice = invoiceList.get(position);
        holder.dateTextView.setText(invoice.getDate());
        holder.timeTextView.setText(invoice.getTime());
        holder.itemNameTextView.setText(invoice.getItemName());
        holder.priceTextView.setText("$" + String.valueOf(invoice.getPrice()) + "0");

        StorageReference imageRef = firebaseStorage.getReference().child(invoice.getImagePath());
        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(holder.itemView.getContext())
                    .load(uri)
                    .into(holder.itemImageView);
        }).addOnFailureListener(exception -> {
        });
    }

    @Override
    public int getItemCount() {
        return invoiceList.size();
    }

    public static class InvoiceViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, timeTextView, itemNameTextView, priceTextView;
        ImageView itemImageView;

        public InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            itemImageView = itemView.findViewById(R.id.itemImageView);
        }
    }
}
