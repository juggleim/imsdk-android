package com.example.demo.customization.channellist

import android.app.Activity
import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.FileMessage
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.activities.ChannelListActivity
import com.jet.im.kit.activities.adapter.ChannelListAdapter
import com.jet.im.kit.activities.viewholder.BaseViewHolder
import com.jet.im.kit.interfaces.providers.ChannelListAdapterProvider
import com.jet.im.kit.providers.AdapterProviders
import com.example.demo.R
import com.example.demo.databinding.ViewChannelListItemPreviewBinding
import com.example.demo.utils.toDp
import com.jet.im.kit.utils.ChannelUtils
import com.jet.im.kit.utils.DateUtils
import com.jet.im.kit.utils.DrawableUtils

// 0~1000 are reserved for the UIKit's channel types.
const val NEW_CHANNEL_TYPE = 1001

/**
 * In this sample, only the first channel is set to new type to show a different view than before.
 *
 * step 1. Create a [CustomItemTypeChannelListAdapter] and set it to [AdapterProviders.channelList].
 * step 2. Start [ChannelListActivity].
 *
 * The settings for the custom Provider are set up here to show the steps in the sample,
 * but in your application it is recommended to set it up in the Application class.
 */
fun showNewChannelItemTypeSample(activity: Activity) {
    AdapterProviders.channelList = ChannelListAdapterProvider {
        CustomItemTypeChannelListAdapter()
    }

    val intent = ChannelListActivity.newIntent(activity.applicationContext)
    activity.startActivity(intent)
}

/**
 * This class is used to customize the first channel list item.
 *
 * step 1. Inherit [ChannelListAdapter] and override [onCreateViewHolder].
 * step 2. Override [getItemViewType] and return the custom type for the first item.
 * step 3. Create a custom view and return it in [onCreateViewHolder] for the custom type.
 */
class CustomItemTypeChannelListAdapter : ChannelListAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<GroupChannel> {
        return when (viewType) {
            NEW_CHANNEL_TYPE -> CustomItemTypeChannelPreviewHolder(
                ViewChannelListItemPreviewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) NEW_CHANNEL_TYPE
        else super.getItemViewType(position)
    }

    inner class CustomItemTypeChannelPreviewHolder(private val binding: ViewChannelListItemPreviewBinding) : BaseViewHolder<GroupChannel>(binding.getRoot()) {

        override fun bind(channel: GroupChannel) {
            val context = binding.root.context

            binding.tvTitle.text =
                if (channel.isChatNotification) channel.name.ifEmpty { context.getString(com.jet.im.kit.R.string.sb_text_channel_list_title_unknown) } else ChannelUtils.makeTitleText(
                    context, channel
                )

            // set first member's profile image as cover image
            channel.members.firstOrNull()?.let {
                Glide.with(context)
                    .load(it.profileUrl)
                    .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(8.toDp())))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.ivMediaSelector)
            }

            // icons
            binding.ivBroadcastIcon.visibility = if (channel.isBroadcast) View.VISIBLE else View.GONE
            binding.ivFrozenIcon.visibility = if (channel.isFrozen) View.VISIBLE else View.GONE

            // member count
            binding.tvMemberCount.visibility = if (channel.memberCount > 2) View.VISIBLE else View.GONE
            binding.tvMemberCount.text = ChannelUtils.makeMemberCountText(channel.memberCount)

            // channel's last updated at
            binding.tvUpdatedAt.text = DateUtils.formatDateTime(context, channel.lastMessage?.createdAt ?: channel.createdAt)

            // last message
            run {
                val typingUsers = channel.typingUsers
                if (typingUsers.isNotEmpty()) {
                    binding.tvLastMessage.ellipsize = TextUtils.TruncateAt.END
                    binding.tvLastMessage.text = makeTypingText(context, typingUsers)
                } else {
                    binding.tvLastMessage.ellipsize = if (channel.lastMessage is FileMessage) TextUtils.TruncateAt.MIDDLE else TextUtils.TruncateAt.END
                    binding.tvLastMessage.text = channel.lastMessage?.message ?: "[No Message]"
                }

                binding.ivLastMessageStatus.visibility = View.VISIBLE
            }

            // message receipt
            run {
                channel.lastMessage?.let { lastMessage ->
                    if (lastMessage.isMine() && !channel.isSuper && channel.isGroupChannel) {
                        binding.ivLastMessageStatus.visibility = View.VISIBLE
                        val unreadMemberCount = channel.getUnreadMemberCount(lastMessage)
                        val unDeliveredMemberCount = channel.getUndeliveredMemberCount(lastMessage)
                        when {
                            unreadMemberCount == 0 -> {
                                binding.ivLastMessageStatus.setImageDrawable(
                                    DrawableUtils.setTintList(
                                        context,
                                        com.jet.im.kit.R.drawable.icon_done_all,
                                        SendbirdUIKit.getDefaultThemeMode().secondaryTintResId
                                    )
                                )
                            }
                            unDeliveredMemberCount == 0 -> {
                                binding.ivLastMessageStatus.setImageDrawable(
                                    DrawableUtils.setTintList(
                                        context,
                                        com.jet.im.kit.R.drawable.icon_done_all,
                                        SendbirdUIKit.getDefaultThemeMode().monoTintResId
                                    )
                                )
                            }
                            else -> {
                                binding.ivLastMessageStatus.setImageDrawable(
                                    DrawableUtils.setTintList(
                                        context,
                                        com.jet.im.kit.R.drawable.icon_done,
                                        SendbirdUIKit.getDefaultThemeMode().monoTintResId
                                    )
                                )
                            }
                        }
                    } else {
                        binding.ivLastMessageStatus.visibility = View.GONE
                    }
                }
            }

            // unread counts
            run {
                val unreadMessageCount = channel.unreadMessageCount
                val unreadMentionCount = channel.unreadMentionCount
                binding.tvUnreadCount.run {
                    text = if (unreadMessageCount > 99) context.getString(com.jet.im.kit.R.string.sb_text_channel_list_unread_count_max) else unreadMessageCount.toString()
                    visibility = if (unreadMessageCount > 0) View.VISIBLE else View.GONE
                    setTextColor(Color.WHITE)
                    setBackgroundResource(if (SendbirdUIKit.isDarkMode()) com.jet.im.kit.R.drawable.sb_shape_unread_message_count_dark else com.jet.im.kit.R.drawable.sb_shape_unread_message_count)
                }
                binding.tvUnreadMentionCount.run {
                    text = SendbirdUIKit.getUserMentionConfig().trigger
                    visibility = if (unreadMentionCount > 0) View.VISIBLE else View.GONE
                }
            }

            val pushOff = channel.myPushTriggerOption == GroupChannel.PushTriggerOption.OFF
            binding.ivPushEnabledIcon.visibility = if (pushOff) View.VISIBLE else View.GONE
        }
    }
}
