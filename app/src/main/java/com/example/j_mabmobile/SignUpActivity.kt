package com.example.j_mabmobile

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.j_mabmobile.api.ApiService
import com.example.j_mabmobile.api.RetrofitClient
import com.example.j_mabmobile.model.ApiResponse
import com.example.j_mabmobile.model.SignUpRequest
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.logging.Handler

class SignUpActivity : AppCompatActivity() {

    private lateinit var emailTextField: TextInputEditText
    private lateinit var lastNameTextField: EditText
    private lateinit var firstNameTextField: EditText
    private lateinit var passwordTextField: TextInputEditText
    private lateinit var confirmPasswordTextField: TextInputEditText
    private lateinit var signUpBtn: Button
    private lateinit var emailErrorText: TextView
    private lateinit var passwordErrorText: TextView
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var overlayBackground: View
    private lateinit var emailTIL: TextInputLayout
    private lateinit var passwordTIL: TextInputLayout
    private lateinit var confirmPasswordTIL: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        // Check if token is already present and valid
        val token = getToken()
        if (token != null && isTokenValid()) {
            val intent = Intent(this@SignUpActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        emailTextField = findViewById(R.id.emailAddress)
        lastNameTextField = findViewById(R.id.lastName)
        firstNameTextField = findViewById(R.id.firstName)
        passwordTextField = findViewById(R.id.userPassword)
        confirmPasswordTextField = findViewById(R.id.confirmPassword)
        signUpBtn = findViewById(R.id.signUpBtn)
        loadingAnimation = findViewById(R.id.loadingAnimation)
        overlayBackground = findViewById(R.id.overlayBackground)
        emailTIL = findViewById(R.id.emailTIL)
        passwordTIL = findViewById(R.id.passwordTIL)
        confirmPasswordTIL = findViewById(R.id.confirmPasswordTIL)

        signUpBtn.isEnabled = false
        signUpBtn.setBackgroundColor(Color.LTGRAY)

        emailTextField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                emailTIL.error = null // Clear error on text change
                emailTIL.isErrorEnabled = false
            }
            override fun afterTextChanged(s: Editable?) {
                toggleSignUpButton()
            }
        })

        firstNameTextField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                toggleSignUpButton()

                if (s.isNullOrEmpty()) return

                // Prevent numbers and special characters
                val filteredText = s.toString().replace("[^a-zA-Z ]".toRegex(), "")

                // Capitalize the first letter of each word
                val words = filteredText.split(" ")
                val capitalizedWords = words.joinToString(" ") { word ->
                    if (word.isNotEmpty()) word.replaceFirstChar { it.uppercase() } else ""
                }

                // Prevent infinite loop by comparing the old and new text
                if (s.toString() != capitalizedWords) {
                    firstNameTextField.removeTextChangedListener(this)
                    firstNameTextField.setText(capitalizedWords)
                    firstNameTextField.setSelection(capitalizedWords.length)
                    firstNameTextField.addTextChangedListener(this)
                }
            }
        })

        lastNameTextField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                toggleSignUpButton()

                if (s.isNullOrEmpty()) return

                // Prevent numbers and special characters
                val filteredText = s.toString().replace("[^a-zA-Z ]".toRegex(), "")

                // Capitalize the first letter of each word
                val words = filteredText.split(" ")
                val capitalizedWords = words.joinToString(" ") { word ->
                    if (word.isNotEmpty()) word.replaceFirstChar { it.uppercase() } else ""
                }

                // Prevent infinite loop by comparing the old and new text
                if (s.toString() != capitalizedWords) {
                    lastNameTextField.removeTextChangedListener(this)
                    lastNameTextField.setText(capitalizedWords)
                    lastNameTextField.setSelection(capitalizedWords.length)
                    lastNameTextField.addTextChangedListener(this)
                }
            }
        })

        passwordTextField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                passwordTIL.error = null // Clear error on text change
                passwordTIL.isErrorEnabled = false
            }
            override fun afterTextChanged(s: Editable?) {
                toggleSignUpButton()
            }
        })

        confirmPasswordTextField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                confirmPasswordTIL.error = null // Clear error on text change
                passwordTIL.isErrorEnabled = false
            }
            override fun afterTextChanged(s: Editable?) {
                toggleSignUpButton()
            }
        })

        val tvSignInHere = findViewById<TextView>(R.id.tvSignInHere)
        val text = "Already have an account? Sign in"
        val spannableString = SpannableString(text)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
            }
        }

        val signInStart = text.indexOf("Sign in")
        val signInEnd = signInStart + "Sign in".length

        spannableString.setSpan(clickableSpan, signInStart, signInEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.rgb(2, 37, 75)), signInStart, signInEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(UnderlineSpan(), signInStart, signInEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        tvSignInHere.text = spannableString
        tvSignInHere.movementMethod = LinkMovementMethod.getInstance()


        signUpBtn.setOnClickListener {
            // Validate all fields one last time
            val emailValid = validateEmail()
            val passwordValid = validatePassword()
            val confirmPasswordValid = validateConfirmPassword()

            if (emailValid && passwordValid && confirmPasswordValid) {
                // Show loading animation and overlay
                showLoading(true)

                // Disable the button to prevent multiple clicks
                signUpBtn.isEnabled = false

                val email = emailTextField.text.toString().trim()
                val firstName = firstNameTextField.text.toString().trim()
                val lastName = lastNameTextField.text.toString().trim()
                val password = passwordTextField.text.toString().trim()

                // All validations pass, proceed with sign-up request
                val signUpRequest = SignUpRequest(firstName, lastName, email, password)
                signUpUser(signUpRequest)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            loadingAnimation.visibility = View.VISIBLE
            overlayBackground.visibility = View.VISIBLE
        } else {
            loadingAnimation.visibility = View.GONE
            overlayBackground.visibility = View.GONE
        }
    }

    private fun validateEmail(): Boolean {
        val email = emailTextField.text.toString().trim()

        if (email.isEmpty()) {
            emailTIL.error = "Email is required"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailTIL.error = "Invalid email format"
            return false
        }

        // Clear error if validation passes
        emailTIL.error = null
        return true
    }

    private fun validatePassword(): Boolean {
        val password = passwordTextField.text.toString().trim()

        if (password.isEmpty()) {
            passwordTIL.error = "Password is required"
            return false
        }

        if (password.length < 8) {
            passwordTIL.error = "Password should be at least 8 characters"
            return false
        }

        val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$"
        if (!password.matches(passwordRegex.toRegex())) {
            passwordTIL.error = "Password must contain at least one digit, one lowercase letter, and one uppercase letter"
            return false
        }

        passwordTIL.error = null
        return true
    }


    private fun validateConfirmPassword(): Boolean {
        val password = passwordTextField.text.toString().trim()
        val confirmPassword = confirmPasswordTextField.text.toString().trim()

        if (confirmPassword.isEmpty()) {
            confirmPasswordTIL.error = "Confirm password is required"
            return false
        }

        if (password != confirmPassword) {
            confirmPasswordTIL.error = "Passwords do not match"
            return false
        }

        confirmPasswordTIL.error = null
        return true
    }

    // Helper methods to show and hide specific errors
    private fun showEmailError(message: String) {
        emailErrorText.text = message
    }

    private fun hideEmailError() {
        emailErrorText.text = ""
    }

    private fun showPasswordError(message: String) {
        passwordErrorText.text = message
    }

    private fun hidePasswordError() {
        passwordErrorText.text = ""
    }

    private fun toggleSignUpButton() {
        val allFieldsFilled = emailTextField.text?.isNotEmpty() == true &&
                firstNameTextField.text?.isNotEmpty() == true &&
                lastNameTextField.text?.isNotEmpty() == true &&
                passwordTextField.text?.isNotEmpty() == true &&
                confirmPasswordTextField.text?.isNotEmpty() == true

        if (allFieldsFilled) {
            signUpBtn.isEnabled = true
            signUpBtn.setBackgroundColor(Color.parseColor("#02254B"))
        } else {
            signUpBtn.isEnabled = false
            signUpBtn.setBackgroundColor(Color.LTGRAY)
        }
    }

    private fun signUpUser(signUpRequest: SignUpRequest) {
        val apiService = RetrofitClient.getRetrofitInstance(this)
        val call = apiService.create(ApiService::class.java).register(signUpRequest)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                // Hide loading animation
                showLoading(false)

                if (response.code() == 201) {
                    val apiResponse = response.body()
                    if (apiResponse?.message?.contains("verify", ignoreCase = true) == true) {
                        // Navigate to verification screen with email
                        val intent = Intent(this@SignUpActivity, VerificationCodeRegisterActivity::class.java)
                        intent.putExtra("email", signUpRequest.email)
                        intent.putExtra("password", signUpRequest.password)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    }
                } else {
                    // Re-enable the button if there's an error
                    signUpBtn.isEnabled = true

                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        try {
                            val jsonObject = JSONObject(errorBody)

                            // Extract error message from "errors" array
                            val errorsArray = jsonObject.optJSONArray("errors")
                            val errorMessage = if (errorsArray != null && errorsArray.length() > 0) {
                                errorsArray.getString(0)  // Get the first error message
                            } else {
                                "An unknown error occurred"
                            }

                            // Check if the error is specifically about email registration
                            if (errorMessage.contains("Email is already registered", ignoreCase = true)) {
                                showEmailError("This email is already in use. Please sign in.")
                            } else {
                                // Show as a Toast if it's not a field-specific error
                                Toast.makeText(this@SignUpActivity, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@SignUpActivity, "An error occurred while processing your request.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@SignUpActivity, "An unknown error occurred", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                // Hide loading and re-enable button
                showLoading(false)
                signUpBtn.isEnabled = true

                Toast.makeText(this@SignUpActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getToken(): String? {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwt_token", null)
    }

    private fun isTokenValid(): Boolean {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val expiryTime = sharedPreferences.getLong("token_expiry_time", 0)
        return System.currentTimeMillis() < expiryTime
    }
}
