package com.numberbook.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PhoneListAdapter extends RecyclerView.Adapter<PhoneListAdapter.EntryRowHolder> {

    private List<PhoneEntry> displayedEntries;

    public PhoneListAdapter(List<PhoneEntry> displayedEntries) {
        this.displayedEntries = displayedEntries;
    }

    @NonNull
    @Override
    public EntryRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rowView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_phone_entry, parent, false);
        return new EntryRowHolder(rowView);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryRowHolder holder, int position) {
        PhoneEntry entry = displayedEntries.get(position);
        holder.nameLabel.setText(entry.getFullName());
        holder.numberLabel.setText(entry.getMobileNumber());
    }

    @Override
    public int getItemCount() {
        return displayedEntries.size();
    }

    public void refreshEntries(List<PhoneEntry> updatedEntries) {
        this.displayedEntries = updatedEntries;
        notifyDataSetChanged();
    }

    static class EntryRowHolder extends RecyclerView.ViewHolder {
        final TextView nameLabel;
        final TextView numberLabel;

        EntryRowHolder(@NonNull View itemView) {
            super(itemView);
            nameLabel = itemView.findViewById(R.id.tvEntryName);
            numberLabel = itemView.findViewById(R.id.tvEntryNumber);
        }
    }
}
