package com.jet.im.kit.internal.ui.notifications

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sendbird.android.channel.FeedChannel
import com.jet.im.kit.internal.extensions.setTypeface
import com.jet.im.kit.internal.model.notifications.NotificationConfig
import com.jet.im.kit.internal.ui.components.HeaderView
import com.jet.im.kit.modules.components.HeaderComponent

/**
 * This class creates and performs a view corresponding the channel header area in Sendbird UIKit.
 *
 * since 3.5.0
 */
@JvmSuppressWildcards
internal class FeedNotificationHeaderComponent(
    private val uiConfig: NotificationConfig? = null
) : HeaderComponent(Params()) {
    /**
     * Returns a collection of parameters applied to this component.
     *
     * @return `Params` applied to this component
     * since 3.5.0
     */
    override fun getParams(): Params {
        return super.getParams() as Params
    }

    /**
     * Called after the component was created to make views.
     *
     * **If this function is used override, [.getRootView] must also be override.**
     *
     * @param context  The `Context` this component is currently associated with
     * @param inflater The LayoutInflater object that can be used to inflate any views in the component
     * @param parent   The ViewGroup into which the new View will be added
     * @param args     The arguments supplied when the component was instantiated, if any
     * @return Return the View for the UI.
     * since 3.5.0
     */
    override fun onCreateView(context: Context, inflater: LayoutInflater, parent: ViewGroup, args: Bundle?): View {
        val layout = super.onCreateView(context, inflater, parent, args)
        if (layout is HeaderView) {
            layout.descriptionTextView.visibility = View.GONE

            uiConfig?.let {
                val themeMode = it.themeMode
                it.theme.headerTheme.apply {
                    layout.setBackgroundColor(backgroundColor.getColor(themeMode))
                    layout.setDividerColor(lineColor.getColor(themeMode))
                    layout.titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
                    layout.titleTextView.setTextColor(textColor.getColor(themeMode))
                    layout.titleTextView.setTypeface(fontWeight.value)
                    layout.leftButton.imageTintList = ColorStateList.valueOf(buttonIconTintColor.getColor(themeMode))
                }
            }
        }
        return layout
    }

    /**Í
     * Notifies this component that the channel data has changed.
     *
     * @param channel The latest group channel
     * since 3.5.0
     */
    fun notifyChannelChanged(channel: FeedChannel) {
        val rootView = rootView as? HeaderView ?: return
        if (params.title == null) {
            rootView.titleTextView.text = channel.name
        }
    }

    /**
     * A collection of parameters, which can be applied to a default View. The values of params are not dynamically applied at runtime.
     * Params cannot be created directly, and it is automatically created together when components are created.
     *
     * **Since the onCreateView configuring View uses the values of the set Params, we recommend that you set up for Params before the onCreateView is called.**
     *
     * @see .getParams
     * since 3.5.0
     */
    class Params : HeaderComponent.Params()
}
