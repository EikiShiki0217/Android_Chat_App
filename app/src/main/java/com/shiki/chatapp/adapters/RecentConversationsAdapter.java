package com.shiki.chatapp.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.shiki.chatapp.databinding.ItemContainerRecentConversionBinding;
import com.shiki.chatapp.listeners.ConversionListener;
import com.shiki.chatapp.models.ChatMessage;
import com.shiki.chatapp.models.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder> {

    private final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;

    public RecentConversationsAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener) {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRecentConversionBinding binding;

        ConversionViewHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding) {
            super(itemContainerRecentConversionBinding.getRoot());
            binding = itemContainerRecentConversionBinding;
        }

        void setData(ChatMessage chatMessage) {
            Glide.with(binding.getRoot()).load(chatMessage.conversionImage).apply(new RequestOptions().fitCenter()).into(binding.imageProfile);
            binding.textName.setText(chatMessage.conversionName);
            binding.textRecentMessage.setText(chatMessage.message);
            binding.textDateTime.setText(formatDate(chatMessage.dateObject));
            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.id = chatMessage.conversionId;
                user.name = chatMessage.conversionName;
                user.image = chatMessage.conversionImage;
                conversionListener.onConversionClicked(user);
            });
        }
    }

    public String formatDate(Date dateObject) {
        Date currentDate = new Date();
        long timeDifference = currentDate.getTime() - dateObject.getTime();

        if (timeDifference < 24 * 60 * 60 * 1000) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            return timeFormat.format(dateObject);
        } else if (timeDifference < 7 * 24 * 60 * 60 * 1000) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateObject);
        } else {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd");
            return dateFormat.format(dateObject);
        }
    }
}
