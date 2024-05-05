package com.example.ucar_home

import SignInGoogleStep1
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
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
import com.example.ucar_home.R
import com.example.ucar_home.databinding.ActivityLogInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
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


        //LOGIN GOOGLE ------------------------------------------------------------------------------NO FUNCIONA
        binding.btnLoginGoogle.setOnClickListener {
            val googleConf =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            val googleClient = GoogleSignIn.getClient(this, googleConf)
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }


    // ---------------------------------- AUTHENTICATION ------------------------------------------
        //LOGIN MANUAL
        binding.btnLoginManual.setOnClickListener {
            binding.viewLoginUser.visibility = View.VISIBLE
            binding.viewLoginUser.alpha=1f

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
                    }else{
                        Log.d(ContentValues.TAG, "Debes rellenar los campos")
                        }
                }
            } catch (e: Exception) {
                Log.d(ContentValues.TAG, "Error en la autentificacion del usuario")
            }

        }

        //GO BACK BUTTON
        binding.imageBtnGoBack.setOnClickListener {

            // Crear un objeto de animación de transparencia
            val fadeOut = ObjectAnimator.ofFloat(binding.viewLoginUser, View.ALPHA, 1f, 0f)
            fadeOut.duration = 900 // Duración de la animación en milisegundos

            // Iniciar la animación
            fadeOut.start()

            // Listener para saber cuándo finaliza la animación
            fadeOut.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.viewLoginUser.visibility = View.GONE
                }
            })
        }

        //FORGOT PASSWORD BUTTON
        binding.btnForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }


    // ---------------------------------- CREATE ACCOUNT ------------------------------------------
        //CREATE ACCOUNT
        binding.btnSingIn.setOnClickListener {
            binding.viewCreateAccount.visibility=View.VISIBLE
            binding.viewCreateAccount.alpha=1f

            // Animación de escala en X
            val scaleX = ObjectAnimator.ofFloat(binding.viewCreateAccount, View.SCALE_X, 0f, 1f)
            scaleX.duration = 500

            // Animación de escala en Y
            val scaleY = ObjectAnimator.ofFloat(binding.viewCreateAccount, View.SCALE_Y, 0f, 1f)
            scaleY.duration = 500

            // Establecer el punto central de la vista como punto de escala
            binding.viewCreateAccount.pivotX = (binding.viewCreateAccount.width / 2).toFloat()
            binding.viewCreateAccount.pivotY = (binding.viewCreateAccount.height / 2).toFloat()

            // Crear un conjunto de animaciones y ejecutarlas
            val animatorSet = AnimatorSet()
            animatorSet.play(scaleX).with(scaleY)
            animatorSet.start()
        }

        //GO BACK BUTTON 2
        binding.imageBtnGoBack2.setOnClickListener {

            // Crear un objeto de animación de transparencia
            val fadeOut = ObjectAnimator.ofFloat(binding.viewCreateAccount, View.ALPHA, 1f, 0f)
            fadeOut.duration = 900 // Duración de la animación en milisegundos

            // Iniciar la animación
            fadeOut.start()

            // Listener para saber cuándo finaliza la animación
            fadeOut.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.viewCreateAccount.visibility = View.GONE
                }
            })
        }

        //SIGN IN GOOGLE ----------------------------------------------------------------------------CREAR registro con google
        binding.btnSignInGoogle.setOnClickListener {
            //Añadir funcionalidad al boton para poder registrarse con Google
        }

        //SING IN MANUAL, registrarse
        binding.btnSignInManual.setOnClickListener {
            val intent = Intent(this, SignInStep1Activity::class.java)
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








