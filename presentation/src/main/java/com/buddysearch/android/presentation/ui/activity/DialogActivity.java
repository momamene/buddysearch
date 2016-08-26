package com.buddysearch.android.presentation.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.PopupMenu;

import com.buddysearch.android.presentation.R;
import com.buddysearch.android.presentation.databinding.ActivityDialogBinding;
import com.buddysearch.android.presentation.mvp.model.MessageModel;
import com.buddysearch.android.presentation.mvp.presenter.DialogPresenter;
import com.buddysearch.android.presentation.mvp.view.DialogView;
import com.buddysearch.android.presentation.mvp.view.impl.DialogViewImpl;
import com.buddysearch.android.presentation.ui.adapter.MessagesAdapter;

import java.util.List;

import javax.inject.Inject;

public class DialogActivity extends BaseDaggerActivity<DialogView, DialogPresenter, ActivityDialogBinding> {

    public static final String KEY_PEER_ID = "peer_id";

    @Inject
    DialogPresenter dialogPresenter;

    private MessagesAdapter messagesAdapter;

    public static void start(Context context, String peerId) {
        Intent intent = new Intent(context, DialogActivity.class);
        intent.putExtra(KEY_PEER_ID, peerId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPeerId();
        initMessagesRecyclerView();
        initSendMessageButton();
    }

    @Override
    protected DialogView initView() {
        return new DialogViewImpl(this) {
            @Override
            public void renderMessages(List<MessageModel> messages) {
                messagesAdapter.setItems(messages);
                if (messagesAdapter.getItemCount() > 0) {
                    binding.rvUsers.scrollToPosition(messagesAdapter.getItemCount() - 1);
                }
            }

            @Override
            public void setTitle(String title) {
                DialogActivity.this.setTitle(title);
            }

            @Override
            public void clearInput() {
                binding.tvInputMessage.getText().clear();
            }

            @Override
            public void showMessageMenu(MessageModel message, int position) {
                View item = binding.rvUsers.getChildAt(position).findViewById(R.id.tv_text);
                PopupMenu popupMenu = new PopupMenu(item.getContext(), item);
                boolean findItemVisibility = message.getSenderId().equals(presenter.getAuthManager().getCurrentUserId())
                        && position == messagesAdapter.getItemCount() - 1;
                popupMenu.inflate(R.menu.menu_message_item);
                popupMenu.getMenu().findItem(R.id.item_edit).setVisible(findItemVisibility);
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()) {
                        case R.id.item_delete: {
                            presenter.deleteMessage(message);
                        }
                        default:
                            return false;
                    }
                });
                popupMenu.show();
            }
        };
    }

    @Override
    protected DialogPresenter initPresenter() {
        getActivityComponent().inject(this);
        return dialogPresenter;
    }

    @Override
    protected ActivityDialogBinding initBinding() {
        return DataBindingUtil.setContentView(this, R.layout.activity_dialog);
    }

    private void initMessagesRecyclerView() {
        messagesAdapter = new MessagesAdapter(view, presenter.getAuthManager().getCurrentUserId());
        binding.rvUsers.setAdapter(messagesAdapter);
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initPeerId() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String peerId = extras.getString(KEY_PEER_ID);
            presenter.setPeerId(peerId);
        }
    }

    private void initSendMessageButton() {
        binding.btnSend.setOnClickListener(view1 -> {
            String message = binding.tvInputMessage.getText().toString();
            if (!TextUtils.isEmpty(message)) {
                presenter.sendMessage(message);
            }
        });
    }
}