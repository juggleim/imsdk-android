package com.example.demo.customization.userlist

import android.app.Activity
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.sendbird.android.SendbirdChat
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.activities.CreateChannelActivity
import com.jet.im.kit.activities.adapter.CreateChannelUserListAdapter
import com.jet.im.kit.activities.viewholder.BaseViewHolder
import com.jet.im.kit.consts.CreatableChannelType
import com.jet.im.kit.interfaces.UserInfo
import com.jet.im.kit.interfaces.providers.CreateChannelUserListAdapterProvider
import com.jet.im.kit.providers.AdapterProviders
import com.example.demo.R
import com.example.demo.databinding.ViewSelectUserBinding
import com.example.demo.utils.toDp
import com.jet.im.kit.utils.UserUtils

/**
 * In this sample, all user item views are changed to a view with a square profile and different text appearances.
 *
 * step 1. Create a custom user list adapter and set it to [AdapterProviders].
 * step 2. Start an activity that uses the user list.
 *
 * The settings for the custom Provider are set up here to show the steps in the sample,
 * but in your application it is recommended to set it up in the Application class.
 */
fun showUserItemUISample(activity: Activity) {
    val adapter = CustomCreateChannelUserListAdapter()
    AdapterProviders.createChannelUserList = CreateChannelUserListAdapterProvider {
        adapter
    }

    val intent = CreateChannelActivity.newIntent(activity.applicationContext, CreatableChannelType.Normal)
    activity.startActivity(intent)
}

/**
 * This class is used to customize all user list items.
 *
 * step 1. Inherit an adapter that you want to customize and override [onCreateViewHolder].
 * step 2. Create a custom view and return it in [onCreateViewHolder].
 */
class CustomCreateChannelUserListAdapter : CreateChannelUserListAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<UserInfo> {
        return SelectUserViewHolder(ViewSelectUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BaseViewHolder<UserInfo>, position: Int) {
        val userInfo = getItem(position)
        holder.bind(userInfo)

        if (holder is SelectUserViewHolder) {
            val context = holder.binding.root.context
            val nickname = UserUtils.getDisplayName(context, userInfo)
            holder.binding.tvNickname.text =
                context.getString(R.string.text_list_item_nickname_format).format(position, nickname)
        }
    }

    inner class SelectUserViewHolder(internal val binding: ViewSelectUserBinding) : BaseViewHolder<UserInfo>(binding.root) {
        init {
            binding.root.setOnClickListener {
                val userPosition = bindingAdapterPosition
                if (userPosition != RecyclerView.NO_POSITION) {
                    binding.cbUserPreview.toggle()
                    onCheckChanged(userPosition)
                }
            }
            binding.cbUserPreview.visibility = FrameLayout.VISIBLE
            binding.tvNickname.ellipsize = TextUtils.TruncateAt.END
            binding.tvNickname.maxLines = 1

            binding.cbUserPreview.setOnClickListener {
                val userPosition = bindingAdapterPosition
                if (userPosition != RecyclerView.NO_POSITION) {
                    onCheckChanged(userPosition)
                }
            }
        }

        private fun onCheckChanged(userPosition: Int) {
            val userInfo: UserInfo = getItem(userPosition)
            val isSelected: Boolean = isSelected(userInfo)
            if (!isSelected) {
                selectedUserIdList.add(userInfo.userId)
            } else {
                selectedUserIdList.remove(userInfo.userId)
            }

            // listener to allow CreateChannelFragment to receive events when user selection is changed
            // so that the header's right button can be enabled/disabled accordingly
            onUserSelectChangedListener?.onUserSelectChanged(selectedUserIdList, isSelected)
        }

        override fun bind(item: UserInfo) {
            val context = binding.root.context
            Glide.with(context)
                .load(item.profileUrl)
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(8.toDp())))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivUserCover)

            if (item.userId == SendbirdChat.currentUser?.userId) {
                val meBadge = context.resources.getString(com.jet.im.kit.R.string.sb_text_user_list_badge_me)
                val spannable: Spannable = SpannableString(meBadge)
                val badgeAppearance =
                    if (SendbirdUIKit.isDarkMode()) com.jet.im.kit.R.style.SendbirdSubtitle2OnDark02 else com.jet.im.kit.R.style.SendbirdSubtitle2OnLight02
                spannable.setSpan(
                    TextAppearanceSpan(context, badgeAppearance),
                    0, meBadge.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                binding.tvNickname.append(spannable)
            }
            binding.cbUserPreview.isChecked = isSelected(item)
        }
    }
}
