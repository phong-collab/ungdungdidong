package com.example.travelapp.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.travelapp.R;
import com.example.travelapp.adapters.AdminBookingAdapter;
import com.example.travelapp.models.BookingModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class AdminBookingsFragment extends Fragment {

    private RecyclerView rvBookings;
    private TextView txtEmptyBookings;
    private FirebaseFirestore db;
    private AdminBookingAdapter adapter;
    private List<BookingModel> bookingList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_bookings, container, false);

        db = FirebaseFirestore.getInstance();
        rvBookings = view.findViewById(R.id.rvAdminBookings);
        txtEmptyBookings = view.findViewById(R.id.txtEmptyAdminBookings);

        bookingList = new ArrayList<>();
        adapter = new AdminBookingAdapter(bookingList, new AdminBookingAdapter.OnBookingActionListener() {
            @Override
            public void onConfirmClick(BookingModel booking) {
                confirmPayment(booking);
            }

            @Override
            public void onCancelClick(BookingModel booking) {
                showCancelConfirmationDialog(booking);
            }
        });

        rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBookings.setAdapter(adapter);

        loadBookings();

        return view;
    }

    private void loadBookings() {
        db.collection("bookings")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Lỗi tải đơn đặt: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        bookingList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            BookingModel booking = doc.toObject(BookingModel.class);
                            if (booking != null) {
                                booking.setId(doc.getId());
                                bookingList.add(booking);
                            }
                        }
                        adapter.notifyDataSetChanged();

                        if (bookingList.isEmpty()) {
                            txtEmptyBookings.setVisibility(View.VISIBLE);
                        } else {
                            txtEmptyBookings.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void confirmPayment(BookingModel booking) {
        db.collection("bookings").document(booking.getId())
                .update("paymentStatus", "SUCCESS")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Duyệt đơn đặt tour thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi duyệt đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showCancelConfirmationDialog(BookingModel booking) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận hủy đơn")
                .setMessage("Bạn có chắc chắn muốn hủy đơn đặt tour '" + booking.getTourTitle() + "' của khách hàng này không?")
                .setPositiveButton("Hủy đơn đặt", (dialog, which) -> deleteBooking(booking))
                .setNegativeButton("Đóng lại", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteBooking(BookingModel booking) {
        db.collection("bookings").document(booking.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Hủy đơn đặt tour thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Không thể hủy đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
