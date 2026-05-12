package com.krishiscan.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.krishiscan.app.R;
import com.krishiscan.app.adapters.ChatAdapter;
import com.krishiscan.app.core.di.AppContainer;
import com.krishiscan.app.models.ChatMessage;
import com.krishiscan.app.viewmodels.ChatViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatFragment extends Fragment {
    private ChatViewModel vm;
    private ChatAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this, AppContainer.from(requireContext()).viewModelFactory()).get(ChatViewModel.class);

        RecyclerView rv = view.findViewById(R.id.rvChat);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ChatAdapter();
        rv.setAdapter(adapter);

        EditText et = view.findViewById(R.id.etMessage);
        MaterialButton btn = view.findViewById(R.id.btnSend);
        btn.setOnClickListener(v -> sendMessage(et, rv));

        setupQuickPrompts(view, et, rv);

        vm.history().observe(getViewLifecycleOwner(), h -> {
            if (h == null || h.items == null) return;
            List<ChatMessage> list = new ArrayList<>();
            for (com.krishiscan.app.data.remote.dto.ChatMessageDto d : h.items) {
                ChatMessage m = new ChatMessage();
                m.sender = d.sender;
                m.text = d.text;
                list.add(m);
            }
            adapter.submit(list);
            if (!list.isEmpty()) rv.scrollToPosition(list.size() - 1);
        });

        vm.reply().observe(getViewLifecycleOwner(), r -> {
            if (r == null) return;
            ChatMessage m = new ChatMessage();
            m.sender = "ai";
            m.text = r.reply;
            adapter.add(m);
            rv.scrollToPosition(adapter.getItemCount() - 1);
        });

        vm.error().observe(getViewLifecycleOwner(), err -> {
            if (err != null) Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
        });

        vm.loadHistory();
    }

    private void setupQuickPrompts(View root, EditText et, RecyclerView rv) {
        ViewGroup chipContainer = root.findViewById(R.id.chipContainer);
        List<String> prompts = Arrays.asList(
                "My tomato leaves are turning yellow",
                "Best time to plant rice in Assam?",
                "Government schemes for farmers",
                "How to make organic pesticide?",
                "Soil testing near me"
        );

        chipContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (String prompt : prompts) {
            TextView chip = (TextView) inflater.inflate(R.layout.item_quick_prompt, chipContainer, false);
            chip.setText(prompt);
            chip.setOnClickListener(v -> {
                et.setText(prompt);
                et.setSelection(et.getText().length());
                sendMessage(et, rv);
            });
            chipContainer.addView(chip);
        }
    }

    private void sendMessage(EditText et, RecyclerView rv) {
        String text = et.getText() == null ? "" : et.getText().toString().trim();
        if (text.isEmpty()) return;

        ChatMessage local = new ChatMessage();
        local.sender = "user";
        local.text = text;
        adapter.add(local);
        vm.sendMessage(text);
        et.setText("");
        rv.scrollToPosition(adapter.getItemCount() - 1);
    }
}
