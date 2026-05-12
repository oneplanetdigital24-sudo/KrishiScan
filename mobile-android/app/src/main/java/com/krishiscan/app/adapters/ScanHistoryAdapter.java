package com.krishiscan.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.krishiscan.app.R;
import com.krishiscan.app.models.Scan;

import java.util.ArrayList;
import java.util.List;

public class ScanHistoryAdapter extends RecyclerView.Adapter<ScanHistoryAdapter.VH> {
    public interface OnScanClickListener {
        void onScanClick(Scan scan);
    }

    private final List<Scan> items = new ArrayList<>();
    private final OnScanClickListener listener;

    public ScanHistoryAdapter() {
        this.listener = null;
    }

    public ScanHistoryAdapter(OnScanClickListener listener) {
        this.listener = listener;
    }

    public void submit(List<Scan> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scan_history, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Scan s = items.get(position);
        holder.tvDisease.setText(s.cropName + " " + s.diseaseName);
        holder.tvMeta.setText(Math.round(s.confidence * 100) + "%" + (s.createdAt == null ? "" : " • " + s.createdAt));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onScanClick(s);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDisease;
        TextView tvMeta;
        VH(@NonNull View itemView) {
            super(itemView);
            tvDisease = itemView.findViewById(R.id.tvDisease);
            tvMeta = itemView.findViewById(R.id.tvMeta);
        }
    }
}
