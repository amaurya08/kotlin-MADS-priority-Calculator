package com.ah.calculator.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ah.calculator.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class Login : AppCompatActivity() {
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private var TAG: String = "TAG"
    private var storedVerificationId: String? = ""
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(applicationContext);
        auth = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        //move directly to calc
        if (auth.currentUser != null) {
            var i = Intent(this, MainActivity::class.java);
            startActivity(i)
            finish()
        }

        binding.actionBtn.setOnClickListener {

            val mobile = binding.userid.text.toString()
            Log.d(TAG, "mobile :$mobile")
            if (storedVerificationId == null || storedVerificationId!!.isEmpty())
                if (isValid(mobile))
                    sendOTP(mobile)
                else {
                    Log.d(TAG, "INvalid")
                    binding.userLayout.error = "invalid mobile"
                }
            else {
                //Verify OTP
                val otp = binding.password.text.toString()
                if (otp.isNotEmpty() && otp.length == 6) {
                    verifyOTP(otp)
                } else {
                    binding.passwordLayout.error = "invalid otp"
                }
            }
        }
    }

    private fun isValid(mobile: String): Boolean {

        if (mobile.isBlank())
            return false
        else if (mobile.length != 10) return false
        return true
    }

    private fun sendOTP(mobile: String) {

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$mobile")       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        Log.d(TAG, "SEND OTP COmpleted")
        binding.actionBtn.isEnabled = false;
    }


    public fun verifyOTP(code: String) {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)
        signInWithPhoneAuthCredential(credential)
        binding.actionBtn.isEnabled = false;
    }


    var callbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")

                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)
                binding.actionBtn.isEnabled = true;
                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }
                showDialogWithError("Could not send OTP\n${e.message}")
                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token
                binding.actionBtn.isEnabled = true;
                binding.userLayout.isEnabled = false

                binding.actionBtn.text = "Verify"
                binding.passwordLayout.visibility = View.VISIBLE
                binding.password.visibility = View.VISIBLE
            }
        }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Log.d(TAG, "Signin ")
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                binding.actionBtn.isEnabled = true;
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = task.result?.user
                    val db: FirebaseDatabase = FirebaseDatabase.getInstance();
                    val ref = db.getReference("users")
                    ref.child(user?.uid.toString()).child("mobile").setValue(
                        binding
                            .userid.text.toString()
                    )
                    var i = Intent(this, MainActivity::class.java);
                    startActivity(i)
                    finish()

                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    showDialogWithError("SignInFailure from SigninWithCreds")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

    private fun showDialogWithError(msg: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this);
        builder.setTitle("Error").setMessage(msg)
            .setPositiveButton("Ok", DialogInterface.OnClickListener { dialogInterface, i ->
                run {
                    dialogInterface.dismiss()
                }
            }).show()
    }
}