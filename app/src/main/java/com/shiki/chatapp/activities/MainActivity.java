package com.shiki.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.GravityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.makeramen.roundedimageview.RoundedImageView;
import com.shiki.chatapp.R;
import com.shiki.chatapp.adapters.RecentConversationsAdapter;
import com.shiki.chatapp.databinding.ActivityMainBinding;
import com.shiki.chatapp.listeners.ConversionListener;
import com.shiki.chatapp.models.ChatMessage;
import com.shiki.chatapp.models.User;
import com.shiki.chatapp.utilities.Constants;
import com.shiki.chatapp.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversationsAdapter conversationsAdapter;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        loadUserDetails();
        getToken();
        setListeners();
        listenConversations();
    }

    private void init() {
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversations, this);
        binding.mainBody.conversationsRecyclerView.setAdapter(conversationsAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void setListeners() {
        binding.mainBody.imageProfile.setOnClickListener(v ->
                binding.drawerLayout.openDrawer(GravityCompat.START));
        binding.navView.setNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();
//            if (itemId == R.id.go_to_profile) {
//
//            } else
            if (itemId == R.id.log_out) {
                signOut();
            }
            binding.drawerLayout.closeDrawers();
            return true;
        });
        binding.mainBody.fabNewChat.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), UsersActivity.class)));
    }

    private void loadUserDetails() {
        String image = preferenceManager.getString(Constants.KEY_IMAGE);
        Glide.with(this).load(image).apply(new RequestOptions().fitCenter()).into(binding.mainBody.imageProfile);
        Glide.with(this).load(image).apply(new RequestOptions().fitCenter()).into((RoundedImageView) binding.navView.getHeaderView(0).findViewById(R.id.profile_picture));
        ((TextView) binding.navView.getHeaderView(0).findViewById(R.id.user_name)).setText(preferenceManager.getString(Constants.KEY_NAME));
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void listenConversations() {
        database.collection(Constants.KEY_COLLECTON_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTON_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    chatMessage.messageType = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE_TYPE);
                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        chatMessage.message = "You: " + documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    } else {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    }
                    if (chatMessage.messageType.equals("image")) {
                        if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                            chatMessage.message = "You sent a photo";
                        } else {
                            chatMessage.message = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME) + " sent a photo";
                        }
                    }
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversations.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversations.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)) {
                            if (conversations.get(i).messageType.equals("image")) {
                                if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                                    conversations.get(i).message = "You sent a photo";
                                } else {
                                    conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME) + " sent a photo";
                                }
                            } else if (conversations.get(i).messageType.equals("text")) {
                                if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                                    conversations.get(i).message = "You: " + documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                                } else {
                                    conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                                }
                            }
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        } else if (conversations.get(i).senderId.equals(receiverId) && conversations.get(i).receiverId.equals(senderId)) {
                            if (conversations.get(i).messageType.equals("image")) {
                                if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                                    conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME) + " sent a photo";
                                } else {
                                    conversations.get(i).message = "You sent a photo";
                                }
                            } else if (conversations.get(i).messageType.equals("text")) {
                                if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                                    conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                                } else {
                                    conversations.get(i).message = "You: " + documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                                }
                            }
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                } else if (documentChange.getType() == DocumentChange.Type.REMOVED) {
                    for (int i = 0; i < conversations.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)) {
                            conversations.remove(i);
                        }
                    }
                }
            }
            Collections.sort(conversations, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationsAdapter.notifyDataSetChanged();
            binding.mainBody.conversationsRecyclerView.smoothScrollToPosition(0);
            binding.mainBody.conversationsRecyclerView.setVisibility(View.VISIBLE);
            binding.mainBody.progressBar.setVisibility(View.GONE);
        }
    };

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN, token).addOnFailureListener(e -> showToast("Unnable to update token"));
    }

    private void signOut() {
        showToast("Signing out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Unable to sing out"));
    }

    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
}