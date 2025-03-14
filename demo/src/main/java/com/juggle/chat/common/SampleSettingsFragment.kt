package com.juggle.chat.common

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.juggle.chat.R
import com.juggle.chat.common.consts.StringSet
import com.juggle.chat.common.extensions.getDrawable
import com.juggle.chat.common.extensions.isUsingDarkTheme
import com.juggle.chat.common.preferences.PreferenceUtils
import com.juggle.chat.common.widgets.WaitingDialog
import com.juggle.chat.databinding.FragmentSampleSettingsBinding
import com.juggle.chat.databinding.ViewCustomMenuTextButtonBinding
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.consts.DialogEditTextParams
import com.jet.im.kit.interfaces.OnEditTextResultListener
import com.jet.im.kit.log.Logger
import com.jet.im.kit.model.DialogListItem
import com.jet.im.kit.modules.components.StateHeaderComponent
import com.jet.im.kit.utils.ContextUtils
import com.jet.im.kit.utils.DialogUtils
import com.jet.im.kit.utils.FileUtils
import com.jet.im.kit.utils.IntentUtils
import com.jet.im.kit.utils.PermissionUtils
import com.jet.im.kit.utils.TextUtils
import com.juggle.chat.settings.GlobalDisturbSettingActivity
import com.juggle.chat.settings.PushSettingActivity
import com.juggle.chat.settings.WebViewActivity
import com.juggle.im.JIM
import com.juggle.im.JIMConst
import com.juggle.im.interfaces.IConnectionManager
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.params.UserUpdateParams
import java.io.File
import java.util.Locale

/**
 * Displays a settings screen.
 */
class SampleSettingsFragment : Fragment(), IConnectionManager.IConnectionStatusListener {
    private lateinit var binding: FragmentSampleSettingsBinding
    private val headerComponent = StateHeaderComponent()
    private var mediaUri: Uri? = null
    private val appSettingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (context == null) return@registerForActivityResult
            val hasPermission =
                PermissionUtils.hasPermissions(requireContext(), *REQUIRED_PERMISSIONS)
            if (hasPermission) {
                showMediaSelectDialog()
            }
        }
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (PermissionUtils.getNotGrantedPermissions(it).isEmpty()) {
                showMediaSelectDialog()
            }
        }
    private val takeCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            if (resultCode != Activity.RESULT_OK || context == null) return@registerForActivityResult
            val mediaUri = mediaUri
            if (mediaUri != null) {
                val file = FileUtils.uriToFile(requireContext().applicationContext, mediaUri)
                updateUserProfileImage(file)
            }
        }
    private val getContentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val intent = result.data
            val resultCode = result.resultCode
            if (resultCode != Activity.RESULT_OK || intent == null || context == null) return@registerForActivityResult
            val mediaUri = intent.data
            if (mediaUri != null) {
                val file = FileUtils.uriToFile(requireContext().applicationContext, mediaUri)
                updateUserProfileImage(file)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        JIM.getInstance().connectionManager.addConnectionStatusListener("SampleSettingsFragment", this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSampleSettingsBinding.inflate(inflater, container, false)
        headerComponent.params.title = getString(R.string.text_settings_header_title)
        headerComponent.params.setUseLeftButton(false)
        val header =
            headerComponent.onCreateView(
                requireContext(),
                inflater,
                binding.headerComponent,
                savedInstanceState
            )
        binding.headerComponent.addView(header)
        initPage()
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.sample_settings_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        ViewCustomMenuTextButtonBinding.inflate(layoutInflater).apply {
            val isDark = PreferenceUtils.themeMode.isUsingDarkTheme()
            text.setText(R.string.text_settings_header_edit_button)
            text.setBackgroundResource(if (isDark) com.jet.im.kit.R.drawable.sb_button_uncontained_background_dark else com.jet.im.kit.R.drawable.sb_button_uncontained_background_light)
            text.setTextColor(
                ResourcesCompat.getColor(
                    resources,
                    if (isDark) com.jet.im.kit.R.color.primary_200 else com.jet.im.kit.R.color.primary_300,
                    null
                )
            )
            val editMenuItem = menu.findItem(R.id.action_edit_profile)
            root.setOnClickListener { onOptionsItemSelected(editMenuItem) }
            editMenuItem.actionView = root
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_edit_profile) {
            Logger.d("++ edit button clicked")
            showEditProfileDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        JIM.getInstance().connectionManager.removeConnectionStatusListener("SampleSettingsFragment")
    }

    private fun requestPermission(permissions: Array<String>) {
        if (context == null || activity == null) return
        // 1. check permission
        val hasPermission = PermissionUtils.hasPermissions(requireContext(), *permissions)
        if (hasPermission) {
            showMediaSelectDialog()
            return
        }

        // 2. determine whether rationale popup should show
        val deniedList =
            PermissionUtils.getExplicitDeniedPermissionList(requireActivity(), *permissions)
        if (deniedList.isNotEmpty()) {
            showPermissionRationalePopup(deniedList[0])
            return
        }
        // 3. request permission
        permissionLauncher.launch(permissions)
    }

    private fun showPermissionRationalePopup(permission: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(com.jet.im.kit.R.string.sb_text_dialog_permission_title))
        builder.setMessage(getPermissionGuideMessage(requireContext(), permission))
        builder.setPositiveButton(com.jet.im.kit.R.string.sb_text_go_to_settings) { _: DialogInterface?, _: Int ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse("package:" + requireContext().packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            appSettingLauncher.launch(intent)
        }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    com.jet.im.kit.R.color.secondary_300
                )
            )
    }

    private fun initPage() {
        if (context == null) {
            return
        }

        with(binding) {
            background.setBackgroundResource(com.jet.im.kit.R.color.background_50)
            tvNickname.setTextColor(
                ResourcesCompat.getColor(
                    resources,
                    com.jet.im.kit.R.color.onlight_01,
                    null
                )
            )

            profileDivider.setBackgroundResource(com.jet.im.kit.R.drawable.sb_line_divider_light)
            itemHome.setBackgroundResource(com.jet.im.kit.R.drawable.selector_rectangle_light)
            tvHomeName.setTextColor(
                ResourcesCompat.getColor(
                    resources,
                    com.jet.im.kit.R.color.onlight_01,
                    null
                )
            )
            signOutDivider.setBackgroundResource(com.jet.im.kit.R.drawable.sb_line_divider_light)

            val useHeader = arguments?.getBoolean(StringSet.SETTINGS_USE_HEADER, true) ?: true
            this@SampleSettingsFragment.headerComponent.rootView?.visibility =
                if (useHeader) View.VISIBLE else View.GONE
            this@SampleSettingsFragment.headerComponent.setOnRightButtonClickListener {
                Logger.d("++ edit button clicked")
                showEditProfileDialog()
            }

            loadUserProfileUrl(SendbirdUIKit.avatar)
            binding.tvNickname.text = SendbirdUIKit.nickname

            itemQrCode.setBackgroundResource(com.jet.im.kit.R.drawable.selector_rectangle_light)
            itemQrCode.setOnClickListener {
                Logger.d("++ qrcode clicked")
                qrcode()
            }

            itemPushSetting.setBackgroundResource(com.jet.im.kit.R.drawable.selector_rectangle_light)
            itemPushSetting.setOnClickListener {
                Logger.d("++ push setting clicked")
                pushSetting()
            }

            itemGlobalDisturb.setBackgroundResource(com.jet.im.kit.R.drawable.selector_rectangle_light)
            itemGlobalDisturb.setOnClickListener {
                Logger.d("++ global disturb clicked")
                globalDisturb()
            }

            itemPrivacyPolicy.setBackgroundResource(com.jet.im.kit.R.drawable.selector_rectangle_light)
            itemPrivacyPolicy.setOnClickListener {
                Logger.d("++ privacy policy clicked")
                privacyPolicy()
            }

            itemUserAgreement.setBackgroundResource(com.jet.im.kit.R.drawable.selector_rectangle_light)
            itemUserAgreement.setOnClickListener {
                Logger.d("++ user agreement clicked")
                userAgreement()
            }

            val iconTint =
                if (SendbirdUIKit.isDarkMode()) com.jet.im.kit.R.color.background_700 else com.jet.im.kit.R.color.background_50
            SendbirdUIKit.setDefaultThemeMode(SendbirdUIKit.ThemeMode.Light)
            val homeBackgroundTint = SendbirdUIKit.getDefaultThemeMode().errorColorResId
            binding.ivHomeIcon.setImageDrawable(
                requireContext().getDrawable(R.drawable.icon_leave, iconTint)
            )
            binding.ivHomeIcon.background =
                requireContext().getDrawable(R.drawable.shape_oval, homeBackgroundTint)
            binding.itemHome.setOnClickListener {
                Logger.d("++ home clicked")
                if (JIM.getInstance().connectionManager.connectionStatus == JIMConst.ConnectionStatus.DISCONNECTED) {
                    activity?.finish()
                } else {
                    JIM.getInstance().connectionManager.disconnect(false)
                }
            }
        }
    }

    private fun qrcode() {

    }

    private fun pushSetting() {
        context?.let {
            val intent = PushSettingActivity.newIntent(it)
            startActivity(intent)
        }
    }

    private fun globalDisturb() {
        context?.let {
            val intent = GlobalDisturbSettingActivity.newIntent(it)
            startActivity(intent)
        }
    }

    private fun privacyPolicy() {
        context?.let {
            val intent = WebViewActivity.newIntent(it, getString(R.string.text_privacy_policy), "https://www.juggle.im/jc/privacy.html")
            startActivity(intent)
        }

    }

    private fun userAgreement() {
        context?.let {
            val intent = WebViewActivity.newIntent(it, getString(R.string.text_user_agreement), "https://www.juggle.im/jc/user.html")
            startActivity(intent)
        }

    }

    private fun showEditProfileDialog() {
        if (context == null) return
        val items = arrayOf(
            DialogListItem(R.string.text_settings_change_user_nickname),
            DialogListItem(R.string.text_settings_change_user_profile_image)
        )
        DialogUtils.showListBottomDialog(
            requireContext(),
            items
        ) { _: View?, _: Int, item: DialogListItem ->
            val key = item.key
            if (key == R.string.text_settings_change_user_nickname) {
                Logger.dev("change user nickname")
                val listener = OnEditTextResultListener { result: String ->
                    if (TextUtils.isEmpty(result)) {
                        return@OnEditTextResultListener
                    }
                    updateUserNickname(result)
                }
                val params =
                    DialogEditTextParams(getString(com.jet.im.kit.R.string.sb_text_channel_settings_change_channel_name_hint))
                params.enableSingleLine = true
                DialogUtils.showInputDialog(
                    requireContext(),
                    getString(R.string.text_settings_change_user_nickname),
                    params, listener,
                    getString(com.jet.im.kit.R.string.sb_text_button_save), null,
                    getString(com.jet.im.kit.R.string.sb_text_button_cancel), null
                )
            } else if (key == R.string.text_settings_change_user_profile_image) {
                Logger.dev("change user profile")
                if (context == null) return@showListBottomDialog
                val hasPermission =
                    PermissionUtils.hasPermissions(requireContext(), *REQUIRED_PERMISSIONS)
                if (hasPermission) {
                    showMediaSelectDialog()
                    return@showListBottomDialog
                }
                requestPermission(REQUIRED_PERMISSIONS)
            }
        }
    }

    private fun updateUserNickname(nickname: String) {
        if (context == null) return
        WaitingDialog.show(requireContext())
        val params = UserUpdateParams()
        params.nickname = nickname
        SendbirdUIKit.updateUserInfo(params) { e: SendbirdException? ->
            WaitingDialog.dismiss()
            if (e != null) {
                Logger.e(e)
                ContextUtils.toastError(context, R.string.text_error_update_user_information)
                return@updateUserInfo
            }
            PreferenceUtils.nickname = nickname
            if (isActive) {
                binding.tvNickname.text = nickname
            }
        }
    }

    private fun updateUserProfileImage(profileImage: File) {
        if (context == null) return
        WaitingDialog.show(requireContext())
        val params = UserUpdateParams()
        params.profileImageFile = profileImage
//        updateCurrentUserInfo(params, CompletionHandler { e: SendbirdException? ->
//            WaitingDialog.dismiss()
//            if (e != null) {
//                Logger.e(e)
//                ContextUtils.toastError(context, R.string.text_error_update_user_information)
//                return@CompletionHandler
//            }
//            val currentUser = currentUser
//            if (currentUser != null) {
//                val profileUrl = currentUser.profileUrl
//                PreferenceUtils.profileUrl = profileUrl
//                loadUserProfileUrl(profileUrl)
//            }
//        })
    }

    private fun showMediaSelectDialog() {
        if (context == null) return
        val items = arrayOf(
            DialogListItem(com.jet.im.kit.R.string.sb_text_channel_settings_change_channel_image_camera),
            DialogListItem(com.jet.im.kit.R.string.sb_text_channel_settings_change_channel_image_gallery)
        )
        DialogUtils.showListDialog(
            requireContext(),
            getString(com.jet.im.kit.R.string.sb_text_channel_settings_change_channel_image),
            items
        ) { _: View?, _: Int, item: DialogListItem ->
            try {
                val key = item.key
                if (key == com.jet.im.kit.R.string.sb_text_channel_settings_change_channel_image_camera) {
                    takeCamera()
                } else if (key == com.jet.im.kit.R.string.sb_text_channel_settings_change_channel_image_gallery) {
                    takePhoto()
                }
            } catch (e: Exception) {
                Logger.e(e)
            }
        }
    }

    private fun takeCamera() {
        mediaUri = FileUtils.createImageFileUri(requireContext())
        val uri = mediaUri ?: return
        val intent = IntentUtils.getCameraIntent(requireContext(), uri)
        if (IntentUtils.hasIntent(requireContext(), intent)) {
            takeCameraLauncher.launch(intent)
        }
    }

    private fun takePhoto() {
        val intent = IntentUtils.getImageGalleryIntent()
        getContentLauncher.launch(intent)
    }

    private val isActive: Boolean
        get() {
            val isDeactivated = isRemoving || isDetached || context == null
            return !isDeactivated
        }

    private fun loadUserProfileUrl(url: String) {
        if (isActive) {
            Glide.with(requireContext())
                .load(url)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(
                    requireContext().getDrawable(
                        com.jet.im.kit.R.drawable.icon_user,
                        SendbirdUIKit.getDefaultThemeMode().monoTintResId
                    )
                )
                .into(binding.ivProfileView)
        }
    }

    companion object {
        private val REQUIRED_PERMISSIONS = PermissionUtils.CAMERA_PERMISSION
        private fun getPermissionGuideMessage(context: Context, permission: String): String {
            val textResId: Int = if (Manifest.permission.CAMERA == permission) {
                com.jet.im.kit.R.string.sb_text_need_to_allow_permission_camera
            } else {
                com.jet.im.kit.R.string.sb_text_need_to_allow_permission_storage
            }
            return String.format(
                Locale.US,
                context.getString(textResId),
                ContextUtils.getApplicationName(context)
            )
        }
    }

    override fun onStatusChange(status: JIMConst.ConnectionStatus?, code: Int, extra: String?) {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            if (status == JIMConst.ConnectionStatus.DISCONNECTED) {
                activity?.finish()
            }
        }, 200)
    }

    override fun onDbOpen() {
    }

    override fun onDbClose() {
    }
}
