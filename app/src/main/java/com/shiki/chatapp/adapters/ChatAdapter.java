package com.shiki.chatapp.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.shiki.chatapp.databinding.ItemContainerReceivedMessageBinding;
import com.shiki.chatapp.databinding.ItemContainerSentMessageBinding;
import com.shiki.chatapp.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatMessage> chatMessages;
    private String receiverProfileImage;
    private final String senderId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public void setReceiverProfileImage(String receiverProfileImage) {
        this.receiverProfileImage = receiverProfileImage;
    }

    public ChatAdapter(List<ChatMessage> chatMessages, String receiverProfileImage, String senderId) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        } else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessage chatMessage) {
            if (chatMessage.messageType.equals("text")) {
                binding.imageMessage.setVisibility(View.GONE);
                binding.textMessage.setVisibility(View.VISIBLE);
                binding.textMessage.setText(chatMessage.message);
            } else if (chatMessage.messageType.equals("image")) {
                binding.imageMessage.setVisibility(View.VISIBLE);
                binding.textMessage.setVisibility(View.GONE);
                Glide.with(binding.getRoot()).load(chatMessage.message).override(-1).into(binding.imageMessage);
            }

            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, String receiverProfileImage) {
            if (chatMessage.messageType.equals("text")) {
                binding.imageMessage.setVisibility(View.GONE);
                binding.textMessage.setVisibility(View.VISIBLE);
                binding.textMessage.setText(chatMessage.message);
            } else if (chatMessage.messageType.equals("image")) {
                binding.imageMessage.setVisibility(View.VISIBLE);
                binding.textMessage.setVisibility(View.GONE);
                Log.d("ASd", "setData: " + Target.SIZE_ORIGINAL);
                Glide.with(binding.getRoot().getContext())
                        .load(chatMessage.message).apply(new RequestOptions().override(-1)).into(binding.imageMessage);
            }
            binding.textDateTime.setText(chatMessage.dateTime);
            if (receiverProfileImage != null) {
                Glide.with(binding.getRoot()).load(receiverProfileImage).apply(new RequestOptions().fitCenter()).into(binding.imageProfile);
            }
        }
    }
}
