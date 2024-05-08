package com.jet.im.kit.internal.ui.reactions

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.sendbird.android.message.Reaction
import com.sendbird.android.user.User
import com.jet.im.kit.R
import com.jet.im.kit.activities.adapter.EmojiReactionUserListAdapter
import com.jet.im.kit.consts.StringSet
import com.jet.im.kit.databinding.SbFragmentUserListBinding
import com.jet.im.kit.databinding.SbViewEmojiReactionUserListBinding
import com.jet.im.kit.interfaces.OnItemClickListener
import com.jet.im.kit.model.EmojiManager

internal class EmojiReactionUserListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.sb_widget_emoji_message
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: SbViewEmojiReactionUserListBinding

    var onProfileClickListener: OnItemClickListener<User>? = null

    init {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.EmojiReactionCountList,
            defStyleAttr,
            R.style.Widget_Sendbird_Emoji
        )
        try {
            binding = SbViewEmojiReactionUserListBinding.inflate(LayoutInflater.from(context), this, true)
            val tabLayoutBackgroundResId = a.getResourceId(
                R.styleable.EmojiReactionCountList_sb_emoji_reaction_count_tab_layout_background,
                R.drawable.sb_tab_layout_border_background_light
            )
            val indicatorColor = a.getColor(
                R.styleable.EmojiReactionCountList_sb_emoji_reaction_count_tab_indicator_color,
                ContextCompat.getColor(context, R.color.primary_300)
            )
            binding.tabLayoutPanel.setBackgroundResource(tabLayoutBackgroundResId)
            binding.tabLayout.setSelectedTabIndicatorColor(indicatorColor)
        } finally {
            a.recycle()
        }
    }

    fun setEmojiReactionUserData(
        fragment: Fragment,
        currentPosition: Int,
        reactionList: List<Reaction>,
        reactionUserInfo: Map<Reaction, List<User?>>
    ) {
        val pagerAdapter = UserListPagerAdapter(fragment, reactionList, reactionUserInfo).apply {
            this.onProfileClickListener = OnItemClickListener<User> { view, position, data ->
                this@EmojiReactionUserListView.onProfileClickListener?.onItemClick(view, position, data)
            }
        }
        binding.vpEmojiReactionUserList.adapter = pagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.vpEmojiReactionUserList) { tab: TabLayout.Tab, position: Int ->
            val view = EmojiReactionCountView(context)
            val reaction = reactionList[position]
            view.setCount(reaction.userIds.size)
            view.setEmojiUrl(EmojiManager.getEmojiUrl(reaction.key))
            tab.customView = view
        }.attach()
        val defaultTab = binding.tabLayout.getTabAt(currentPosition)
        defaultTab?.select()
    }

    private class UserListPagerAdapter(
        fragment: Fragment,
        reactionList: List<Reaction>,
        reactionUserInfo: Map<Reaction, List<User?>>
    ) : FragmentStateAdapter(fragment) {
        private val itemCount: Int
        private val fragmentList: MutableList<Fragment> = ArrayList()
        var onProfileClickListener: OnItemClickListener<User> = OnItemClickListener<User> { _, _, _ -> }

        override fun createFragment(position: Int): Fragment = fragmentList[position]
        override fun getItemCount(): Int = itemCount

        init {
            itemCount = reactionUserInfo.size
            reactionList.forEach {
                val userListFragment = UserListFragment().apply {
                    this.onProfileClickListener = OnItemClickListener<User> { view, position, data ->
                        this@UserListPagerAdapter.onProfileClickListener.onItemClick(view, position, data)
                    }
                }
                reactionUserInfo[it]?.let { userList ->
                    val bundle = Bundle()
                    bundle.putInt(StringSet.KEY_EMOJI_REACTION_USER_LIST_SIZE, userList.size)
                    userList.forEachIndexed { index, user ->
                        bundle.putByteArray(StringSet.KEY_EMOJI_REACTION_USER_ + index.toString(), user?.serialize())
                    }
                    userListFragment.arguments = bundle
                }
                fragmentList.add(userListFragment)
            }
        }
    }

    // A class that inherits the Fragment must be public static.
    // If user who was already reacted has been banned or was deactivated, the reacted user may be empty.
    internal class UserListFragment : Fragment() {
        private lateinit var binding: SbFragmentUserListBinding
        var onProfileClickListener: OnItemClickListener<User> = OnItemClickListener<User> { _, _, _ -> }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            val themeInflater = inflater.cloneInContext(context)
            binding = SbFragmentUserListBinding.inflate(themeInflater)
            binding.rvUserList.setUseDivider(false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val userList = mutableListOf<User?>()
            arguments?.let {
                val userListSize = it.getInt(StringSet.KEY_EMOJI_REACTION_USER_LIST_SIZE)
                for (i in 0 until userListSize) {
                    val user =
                        User.buildFromSerializedData(it.getByteArray(StringSet.KEY_EMOJI_REACTION_USER_ + i.toString()))
                    userList.add(user)
                }
            }
            val userListAdapter = EmojiReactionUserListAdapter(userList)
            userListAdapter.setOnEmojiReactionUserListProfileClickListener { v, position, item ->
                onProfileClickListener.onItemClick(
                    v,
                    position,
                    item
                )
            }
            binding.rvUserList.adapter = userListAdapter
            binding.rvUserList.setHasFixedSize(true)
        }
    }
}
