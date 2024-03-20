package com.example.ucar_login

import SignInGoogleStep1
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager.OnActivityResultListener
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.ucar_login.databinding.ActivityLogInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase



class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var googleSignInClient: GoogleSignInClient
    private var GOOGLE_SIGN_IN = 100


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

      session()


        binding = ActivityLogInBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()



        //LOGIN GOOGLE
        binding.btnLoginGoogle.setOnClickListener {
            val googleConf =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            val googleClient = GoogleSignIn.getClient(this, googleConf)
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }

        //LOGIN FACEBOOK
        binding.btnLoginFacebook.setOnClickListener {


        }

        //SING IN MANUAL, registrarse
        binding.btnSingInManual.setOnClickListener {
            val intent = Intent(this, SignInStep1Activity::class.java)
            startActivity(intent)


        }


    // ---------------------------------- AUTHENTICATION ----------------------------------
        //LOGIN MANUAL
        binding.btnLoginManual.setOnClickListener {
            binding.viewLoginUser.visibility = View.VISIBLE

            // Animación de escala en X
            val scaleX = ObjectAnimator.ofFloat(binding.viewLoginUser, View.SCALE_X, 0f, 1f)
            scaleX.duration = 500

            // Animación de escala en Y
            val scaleY = ObjectAnimator.ofFloat(binding.viewLoginUser, View.SCALE_Y, 0f, 1f)
            scaleY.duration = 500

            // Establecer el punto central de la vista como punto de escala
            binding.viewLoginUser.pivotX = (binding.viewLoginUser.width / 2).toFloat()
            binding.viewLoginUser.pivotY = (binding.viewLoginUser.height / 2).toFloat()

            // Crear un conjunto de animaciones y ejecutarlas
            val animatorSet = AnimatorSet()
            animatorSet.play(scaleX).with(scaleY)
            animatorSet.start()

            try {
                binding.btnLogin.setOnClickListener{
                    if (binding.editTextEmail.text.isNotEmpty() && binding.editTextPassword.text.isNotEmpty()){
                        auth.signInWithEmailAndPassword(binding.editTextEmail.text.toString(), binding.editTextPassword.text.toString()).addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                Log.d(ContentValues.TAG, "Autenticacion del ususario Correcta")

                                val intent = Intent(this, HomeActivity::class.java)
                                startActivity(intent)
                            }
                            else {
                                val builder = android.app.AlertDialog.Builder(this)
                                builder.setTitle("Error")
                                builder.setMessage("Usuario o Contraseña Incorrecta")
                                builder.setPositiveButton("Aceptar",null)
                                val dialog: android.app.AlertDialog = builder.create()
                                dialog.show()
                            }
                        }
                    }else{ Log.d(ContentValues.TAG, "Debes rellenar los campos") }
                }
            } catch (e: Exception) {
                Log.d(ContentValues.TAG, "Error en la autentificacion del usuario")
            }

        }

        //GO BACK BUTTON
        binding.imageBtnGoBack.setOnClickListener {

            // Crear un objeto de animación de transparencia
            val fadeOut = ObjectAnimator.ofFloat(binding.viewLoginUser, View.ALPHA, 1f, 0f)
            fadeOut.duration = 1000 // Duración de la animación en milisegundos

            // Iniciar la animación
            fadeOut.start()
        }

        //FORGOT PASSWORD BUTTON
        binding.btnForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)

                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { signInTask ->
                        if (signInTask.isSuccessful) {
                            val intent = Intent(this, SignInGoogleStep1::class.java)
                            startActivity(intent)
                        } else {
                            Log.d(ContentValues.TAG, "El usuario no fue registrado correctamente.")
                        }
                    }
                }
            } catch (e: ApiException) {
                Log.d(ContentValues.TAG, "Error bien gordo")
            }
        }
    }
    private fun session() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email",null)
        if(email != null) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

    }


}








