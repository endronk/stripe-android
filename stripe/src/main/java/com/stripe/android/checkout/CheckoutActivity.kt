package com.stripe.android.checkout

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.snackbar.Snackbar
import com.stripe.android.R
import com.stripe.android.checkout.CheckoutViewModel.Factory
import com.stripe.android.checkout.CheckoutViewModel.PaymentMode
import com.stripe.android.checkout.CheckoutViewModel.SelectedPaymentMethod
import com.stripe.android.checkout.CheckoutViewModel.TransitionTarget
import com.stripe.android.databinding.ActivityCheckoutBinding
import com.stripe.android.model.PaymentMethodCreateParams
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class CheckoutActivity : AppCompatActivity() {
    private val viewBinding by lazy {
        ActivityCheckoutBinding.inflate(layoutInflater)
    }
    private val bottomSheetBehavior by lazy {
        BottomSheetBehavior.from(viewBinding.bottomSheet)
    }
    private val viewModel by viewModels<CheckoutViewModel> {
        Factory(application)
    }

    private val fragmentContainerId: Int
        @IdRes
        get() = viewBinding.fragmentContainer.id

    private val selectedPaymentMethodObserver = Observer<SelectedPaymentMethod?> {
        // TODO: Show different button for Google Pay?
        viewBinding.buyButton.isEnabled = it != null
    }

    private val paymentMethodCreateParamsObserver = Observer<PaymentMethodCreateParams?> {
        viewBinding.buyButton.isEnabled = it != null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        // Handle taps outside of bottom sheet
        viewBinding.root.setOnClickListener {
            animateOut()
        }
        viewModel.error.observe(this) {
            // TODO: Communicate error to caller
            Snackbar.make(viewBinding.coordinator, "Received error: ${it.message}", Snackbar.LENGTH_LONG).show()
        }
        viewModel.paymentMode.observe(this) {
            viewModel.selectedPaymentMethod.removeObserver(selectedPaymentMethodObserver)
            viewModel.paymentMethodCreateParams.removeObserver(paymentMethodCreateParamsObserver)
            when (it) {
                PaymentMode.Existing -> {
                    viewModel.selectedPaymentMethod.observe(this, selectedPaymentMethodObserver)
                }
                PaymentMode.New -> {
                    viewModel.paymentMethodCreateParams.observe(this, paymentMethodCreateParamsObserver)
                }
            }
        }

        setupBottomSheet()

        // TODO: Add loading state
        supportFragmentManager.commit {
            replace(fragmentContainerId, CheckoutPaymentMethodsListFragment())
        }

        viewModel.transition.observe(this) {
            supportFragmentManager.commit {
                when (it) {
                    TransitionTarget.AddCard -> {
                        setCustomAnimations(
                            R.anim.stripe_checkout_transition_enter_from_right,
                            R.anim.stripe_checkout_transition_exit_to_left,
                            R.anim.stripe_checkout_transition_enter_from_left,
                            R.anim.stripe_checkout_transition_exit_to_right
                        )
                        addToBackStack(null)
                        replace(fragmentContainerId, CheckoutAddCardFragment())
                    }
                }
            }
        }
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
        bottomSheetBehavior.isHideable = true
        // Start hidden and then animate in after delay
        bottomSheetBehavior.state = STATE_HIDDEN

        lifecycleScope.launch {
            delay(ANIMATE_IN_DELAY)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheetBehavior.addBottomSheetCallback(
                object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    }

                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == STATE_HIDDEN) {
                            finish()
                        }
                    }
                }
            )
        }
    }

    private fun animateOut() {
        // When the bottom sheet finishes animating to its new state,
        // the callback will finish the activity
        bottomSheetBehavior.state = STATE_HIDDEN
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            super.onBackPressed()
        } else {
            animateOut()
        }
    }

    private companion object {
        private const val ANIMATE_IN_DELAY = 300L
    }
}
