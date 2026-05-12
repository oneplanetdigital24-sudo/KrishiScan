package com.krishiscan.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.krishiscan.app.R;

import java.util.ArrayList;
import java.util.List;

public class QuickPromptsAdapter extends RecyclerView.Adapter<QuickPromptsAdapter.VH> {
    public interface OnPromptClickListener { void onClick(String prompt); }

    private final List<String> items = new ArrayList<>();
    private final OnPromptClickListener listener;

    public QuickPromptsAdapter(OnPromptClickListener listener) {
        this.listener = listener;
    }

    public void submit(List<String> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quick_prompt, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        String prompt = items.get(position);
        holder.tvPrompt.setText(prompt);
        holder.itemView.setOnClickListener(v -> listener.onClick(prompt));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvPrompt;
        VH(@NonNull View itemView) {
            super(itemView);
            tvPrompt = itemView.findViewById(R.id.tvPrompt);
        }
    }
}
