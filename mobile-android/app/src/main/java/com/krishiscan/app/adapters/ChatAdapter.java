package com.krishiscan.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.krishiscan.app.R;
import com.krishiscan.app.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_USER = 1;
    private static final int TYPE_AI = 2;

    private final List<ChatMessage> items = new ArrayList<>();

    public void submit(List<ChatMessage> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    public void add(ChatMessage message) {
        items.add(message);
        notifyItemInserted(items.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        return "user".equals(items.get(position).sender) ? TYPE_USER : TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == TYPE_USER ? R.layout.item_chat_user : R.layout.item_chat_ai;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new MsgVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MsgVH vh = (MsgVH) holder;
        ChatMessage item = items.get(position);
        TextView tv = holder.getItemViewType() == TYPE_USER
                ? vh.itemView.findViewById(R.id.tvUserMsg)
                : vh.itemView.findViewById(R.id.tvAiMsg);
        tv.setText(item.text);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class MsgVH extends RecyclerView.ViewHolder {
        MsgVH(@NonNull View itemView) { super(itemView); }
    }
}
