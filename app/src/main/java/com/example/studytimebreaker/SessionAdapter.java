package com.example.studytimebreaker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * RecyclerView Adapter for displaying study sessions in a list.
 * Each item shows: subject, date, study duration, break duration, breaks count.
 * Provides edit and delete buttons via a callback interface.
 */
public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {

    private List<StudySession> sessions;
    private OnSessionActionListener listener;

    /**
     * Callback interface for edit and delete actions.
     * The activity implements this to handle the actual database operations.
     */
    public interface OnSessionActionListener {
        void onEdit(StudySession session, int position);
        void onDelete(StudySession session, int position);
    }

    public SessionAdapter(List<StudySession> sessions, OnSessionActionListener listener) {
        this.sessions = sessions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for each session card
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        StudySession session = sessions.get(position);

        // Bind data to views
        holder.tvSubject.setText(session.getSubject());
        holder.tvDate.setText(session.getDate());
        holder.tvStudyDuration.setText("Study: " + StudySession.formatDuration(session.getStudyDuration()));
        holder.tvBreakDuration.setText("Break: " + StudySession.formatDuration(session.getBreakDuration()));
        holder.tvBreaks.setText("Breaks: " + session.getBreaksTaken());

        // Edit button click
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(session, holder.getAdapterPosition());
            }
        });

        // Delete button click
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(session, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    /**
     * ViewHolder class - holds references to views in each list item.
     * This avoids calling findViewById repeatedly for better performance.
     */
    static class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubject, tvDate, tvStudyDuration, tvBreakDuration, tvBreaks;
        ImageView btnEdit, btnDelete;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStudyDuration = itemView.findViewById(R.id.tvStudyDuration);
            tvBreakDuration = itemView.findViewById(R.id.tvBreakDuration);
            tvBreaks = itemView.findViewById(R.id.tvBreaks);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
