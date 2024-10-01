package com.example.test2;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    private FirebaseAuth mAuth;
    private CollectionReference invoicesRef;
    private RecyclerView recyclerView;
    private InvoiceAdapter invoiceAdapter;
    private List<Invoice> invoiceList;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private Button logoutButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mAuth = FirebaseAuth.getInstance();

        invoicesRef = FirebaseFirestore.getInstance().collection("invoices");
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyTextView = view.findViewById(R.id.emptyTextView);
        logoutButton = view.findViewById(R.id.logoutButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        invoiceList = new ArrayList<>();
        invoiceAdapter = new InvoiceAdapter(invoiceList);
        recyclerView.setAdapter(invoiceAdapter);

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(getContext(), LoginActivity.class));
            getActivity().finish();
        });

        loadInvoices();

        return view;
    }

    private void loadInvoices() {
        progressBar.setVisibility(View.VISIBLE);
        invoicesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                invoiceList.clear();
                for (DocumentSnapshot snapshot : task.getResult().getDocuments()) {
                    Invoice invoice = snapshot.toObject(Invoice.class);
                    if (invoice != null && invoice.getUserId().equals(mAuth.getCurrentUser().getUid())) {
                        invoiceList.add(invoice);
                    }
                }
                invoiceAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                emptyTextView.setVisibility(invoiceList.isEmpty() ? View.VISIBLE : View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load invoices: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
