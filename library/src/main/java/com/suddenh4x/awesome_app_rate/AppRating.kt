package com.suddenh4x.awesome_app_rate

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.suddenh4x.awesome_app_rate.buttons.CustomFeedbackButton
import com.suddenh4x.awesome_app_rate.buttons.CustomFeedbackButtonClickListener
import com.suddenh4x.awesome_app_rate.buttons.RateButton
import com.suddenh4x.awesome_app_rate.buttons.RateDialogClickListener
import com.suddenh4x.awesome_app_rate.dialog.DialogOptions
import com.suddenh4x.awesome_app_rate.dialog.RateDialogFragment
import com.suddenh4x.awesome_app_rate.logging.RatingLogger
import com.suddenh4x.awesome_app_rate.preferences.ConditionsChecker
import com.suddenh4x.awesome_app_rate.preferences.PreferenceUtil
import com.suddenh4x.awesome_app_rate.preferences.RatingThreshold

object AppRating {

    fun reset(context: Context) {
        PreferenceUtil.reset(context)
        RatingLogger.warn("Settings were reset.")
    }

    data class Builder(var context: AppCompatActivity) {
        private var dialogOptions: DialogOptions = DialogOptions
        private var isDebug = false

        init {
            initializeRateNowButton()
        }

        fun setIconDrawable(iconDrawable: Drawable?) = apply {
            dialogOptions.iconDrawable = iconDrawable
            RatingLogger.debug("Use custom icon drawable.")
        }

        fun setRateLaterButton(@StringRes rateLaterButtonTextId: Int = R.string.rating_dialog_button_rate_later, onRateLaterButtonClickListener: RateDialogClickListener? = null) =
                apply {
                    dialogOptions.rateLaterButton = RateButton(rateLaterButtonTextId, onRateLaterButtonClickListener)
                    RatingLogger.debug("Show rate later button.")
                }

        fun showRateNeverButton(@StringRes rateNeverButtonTextId: Int = R.string.rating_dialog_button_rate_never, onRateNeverButtonClickListener: RateDialogClickListener? = null) =
                apply {
                    dialogOptions.rateNeverButton = RateButton(rateNeverButtonTextId, onRateNeverButtonClickListener)
                    RatingLogger.debug("Show rate never button.")
                }


        // rating dialog overview
        fun setTitleTextId(@StringRes titleTextId: Int) = apply { dialogOptions.titleTextId = titleTextId }

        fun setMessageTextId(@StringRes messageTextId: Int) = apply { dialogOptions.messageTextId = messageTextId }

        fun setConfirmButtonTextId(@StringRes confirmButtonTextId: Int) = apply { dialogOptions.confirmButtonTextId = confirmButtonTextId }


        // rating dialog store
        fun setStoreRatingTitleTextId(@StringRes storeRatingTitleTextId: Int) = apply { dialogOptions.storeRatingTitleTextId = storeRatingTitleTextId }

        fun setStoreRatingMessageTextId(@StringRes storeRatingMessageTextId: Int) = apply { dialogOptions.storeRatingMessageTextId = storeRatingMessageTextId }

        fun setRateNowButton(@StringRes rateNowButtonTextId: Int = R.string.rating_dialog_store_button_rate_now, rateNowButtonClickListener: RateDialogClickListener? = null) =
                apply { dialogOptions.rateButton = RateButton(rateNowButtonTextId, rateNowButtonClickListener) }


        // rating dialog feedback
        fun setFeedbackTitleTextId(@StringRes feedbackTitleTextId: Int) = apply { dialogOptions.feedbackTitleTextId = feedbackTitleTextId }

        fun setNoFeedbackButton(@StringRes noFeedbackButtonTextId: Int = R.string.rating_dialog_feedback_button_no, noFeedbackButtonClickListener: RateDialogClickListener) =
                apply { dialogOptions.noFeedbackButton = RateButton(noFeedbackButtonTextId, noFeedbackButtonClickListener) }


        // rating dialog mail feedback
        fun setMailFeedbackMessageTextId(@StringRes feedbackMailMessageTextId: Int) = apply { dialogOptions.feedbackMailMessageTextId = feedbackMailMessageTextId }

        fun setMailFeedbackButton(@StringRes mailFeedbackButtonTextId: Int = R.string.rating_dialog_feedback_mail_button_mail, mailFeedbackButtonClickListener: RateDialogClickListener) =
                apply { dialogOptions.mailFeedbackButton = RateButton(mailFeedbackButtonTextId, mailFeedbackButtonClickListener) }


        // rating dialog custom feedback
        fun setUseCustomFeedback(useCustomFeedback: Boolean) = apply {
            dialogOptions.useCustomFeedback = useCustomFeedback
            RatingLogger.debug("Use custom feedback instead of mail feedback: $useCustomFeedback.")
        }

        fun setCustomFeedbackMessageTextId(@StringRes feedbackCustomMessageTextId: Int) = apply { dialogOptions.feedbackCustomMessageTextId = feedbackCustomMessageTextId }

        fun setCustomFeedbackButton(@StringRes customFeedbackButtonTextId: Int = R.string.rating_dialog_feedback_custom_button_submit, customFeedbackButtonClickListener: CustomFeedbackButtonClickListener) =
                apply { dialogOptions.customFeedbackButton = CustomFeedbackButton(customFeedbackButtonTextId, customFeedbackButtonClickListener) }


        // other settings
        fun setRatingThreshold(ratingThreshold: RatingThreshold) = apply {
            dialogOptions.ratingThreshold = ratingThreshold
            RatingLogger.debug("Set rating threshold to ${ratingThreshold.ordinal / 2}.")
        }

        fun setCancelable(cancelable: Boolean) = apply {
            dialogOptions.cancelable = cancelable
            RatingLogger.debug("Set cancelable to $cancelable.")
        }

        fun setMinimumLaunchTimes(launchTimes: Int) = apply { PreferenceUtil.setMinimumLaunchTimes(context, launchTimes) }

        fun setMinimumLaunchTimesToShowAgain(launchTimesToShowAgain: Int) = apply { PreferenceUtil.setMinimumLaunchTimesToShowAgain(context, launchTimesToShowAgain) }

        fun setMinimumDays(minimumDays: Int) = apply { PreferenceUtil.setMinimumDays(context, minimumDays) }

        fun setMinimumDaysToShowAgain(minimumDaysToShowAgain: Int) = apply { PreferenceUtil.setMinimumDaysToShowAgain(context, minimumDaysToShowAgain) }

        fun setDebug(isDebug: Boolean) = apply {
            this.isDebug = isDebug
            RatingLogger.warn("Set debug to $isDebug. Don't use this for production.")
        }

        fun create(): DialogFragment {
            val rateDialogFragment = RateDialogFragment()
            rateDialogFragment.arguments = Bundle().apply { putSerializable(RateDialogFragment.ARG_DIALOG_OPTIONS, dialogOptions) }
            return rateDialogFragment
        }

        fun showNow() {
            val rateDialogFragment = RateDialogFragment()
            rateDialogFragment.arguments = Bundle().apply { putSerializable(RateDialogFragment.ARG_DIALOG_OPTIONS, dialogOptions) }
            rateDialogFragment.show(context.supportFragmentManager, TAG)
        }

        fun showIfMeetsConditions() {
            PreferenceUtil.increaseLaunchTimes(context)
            if (isDebug || ConditionsChecker.shouldShowDialog(context)) {
                RatingLogger.info("Show rating dialog now: Conditions met.")
                showNow()
            } else {
                RatingLogger.info("Don't show rating dialog: Conditions not met.")
            }
        }

        private fun initializeRateNowButton() {
            val onRateNowClickListener = object : RateDialogClickListener {
                override fun onClick() {
                    RatingLogger.info("Default rate now button click listener was called.")
                    val url = Uri.parse(GOOGLE_PLAY_URL + context.packageName)
                    RatingLogger.info("Open rating url: $url.")
                    val googlePlayIntent = Intent(Intent.ACTION_VIEW, url)
                    context.startActivity(googlePlayIntent)
                }
            }
            setRateNowButton(R.string.rating_dialog_store_button_rate_now, onRateNowClickListener)
            RatingLogger.debug("Default rate now button initialized.")
        }

        companion object {
            private val TAG = AppRating::class.java.simpleName
            private const val GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id="
        }
    }
}