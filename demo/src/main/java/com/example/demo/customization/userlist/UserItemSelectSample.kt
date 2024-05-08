package com.example.demo.customization.userlist

import android.app.Activity
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.sendbird.android.SendbirdChat
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.activities.CreateChannelActivity
import com.jet.im.kit.activities.adapter.CreateChannelUserListAdapter
import com.jet.im.kit.activities.viewholder.BaseViewHolder
import com.jet.im.kit.consts.CreatableChannelType
import com.jet.im.kit.fragments.CreateChannelFragment
import com.jet.im.kit.interfaces.UserInfo
import com.jet.im.kit.interfaces.providers.CreateChannelFragmentProvider
import com.jet.im.kit.interfaces.providers.CreateChannelUserListAdapterProvider
import com.jet.im.kit.providers.AdapterProviders
import com.jet.im.kit.providers.FragmentProviders
import com.example.demo.R
import com.example.demo.databinding.ViewSelectUserBinding
import com.jet.im.kit.utils.UserUtils

/**
 * In this sample, you are limited to 1 user to select.
 *
 * step 1. Create a custom user list adapter by extending the adapter where you want to limit the number of users that can be selected.
 * step 2. Set [CreateChannelUserListAdapterProvider] to [AdapterProviders.createChannelUserList].
 *
 * The settings for the custom Provider are set up here to show the steps in the sample,
 * but in your application it is recommended to set it up in the Application class.
 */
fun showUserItemSelectSample(activity: Activity) {
    val adapter = CustomSelectionCreateChannelUserListAdapter(1)
    AdapterProviders.createChannelUserList = CreateChannelUserListAdapterProvider {
        adapter
    }

    /**
     * (Optional) Using default [CreateChannelFragment] to override click listener
     */
    FragmentProviders.createChannel = CreateChannelFragmentProvider { channelType, args ->
        CreateChannelFragment.Builder(channelType)
            .withArguments(args)
            .setOnHeaderRightButtonClickListener {
                // custom click listener
                Toast.makeText(
                    activity.applicationContext,
                    "Selected ${adapter.selectedUserIdList.count()} users!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .build()
    }

    val intent = CreateChannelActivity.newIntent(activity.applicationContext, CreatableChannelType.Normal)
    activity.startActivity(intent)
}

/**
 * This class demonstrates how to limit the number of users that can be selected in the user list.
 *
 * step 1. Inherit a adapter where you want to limit the number of users that can be selected.
 * step 2. Implement limiting user by user [selectedUserIdList] when the item is clicked and checked.
 */
class CustomSelectionCreateChannelUserListAdapter(private val maxSelectionCount: Int) : CreateChannelUserListAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<UserInfo> {
        return SelectUserViewHolder(ViewSelectUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BaseViewHolder<UserInfo>, position: Int) {
        val userInfo = getItem(position)
        holder.bind(userInfo)
    }

    inner class SelectUserViewHolder(internal val binding: ViewSelectUserBinding) : BaseViewHolder<UserInfo>(binding.root) {
        init {
            binding.root.setOnClickListener {
                val userPosition = bindingAdapterPosition
                if (userPosition != RecyclerView.NO_POSITION) {
                    if (onCheckChanged(userPosition)) {
                        binding.cbUserPreview.toggle()
                    }
                }
            }
            binding.cbUserPreview.visibility = FrameLayout.VISIBLE
            binding.tvNickname.ellipsize = TextUtils.TruncateAt.END
            binding.tvNickname.maxLines = 1

            binding.cbUserPreview.setOnClickListener {
                val userPosition = bindingAdapterPosition
                if (userPosition != RecyclerView.NO_POSITION) {
                    if (!onCheckChanged(userPosition)) {
                        binding.cbUserPreview.toggle()
                    }
                }
            }
        }

        private fun onCheckChanged(userPosition: Int): Boolean {
            val userInfo: UserInfo = getItem(userPosition)
            val isSelected: Boolean = isSelected(userInfo)

            if (!isSelected) {
                val totalSelectionCount = selectedUserIdList.count()
                if (totalSelectionCount == maxSelectionCount) {
                    if (maxSelectionCount == 1) {
                        // for single selection, replace previous selection
                        selectedUserIdList.removeFirst()?.let { userId ->
                            val removedIndex = userList.indexOfFirst { it.userId == userId }
                            if (removedIndex != RecyclerView.NO_POSITION) {
                                notifyItemChanged(removedIndex)
                            }
                        }
                    } else {
                        // for multiple selection, prevent additional selection
                        return false
                    }
                }
                selectedUserIdList.add(userInfo.userId)
            } else {
                selectedUserIdList.remove(userInfo.userId)
            }

            // listener to allow CreateChannelFragment to receive events when user selection is changed
            // so that the header's right button can be enabled/disabled accordingly
            onUserSelectChangedListener?.onUserSelectChanged(selectedUserIdList, isSelected)
            return true
        }

        override fun bind(item: UserInfo) {
            val context = binding.root.context
            Glide.with(context)
                .load(item.profileUrl)
                .apply(RequestOptions().transform(CircleCrop()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivUserCover)

            val nickname = UserUtils.getDisplayName(context, item)
            binding.tvNickname.text = nickname

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
