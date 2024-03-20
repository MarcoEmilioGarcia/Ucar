import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.example.ucar_login.HomeActivity
import com.example.ucar_login.R
import com.example.ucar_login.databinding.ActivitySignInStep3Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class SignInGoogleStep1 : AppCompatActivity() {

    private lateinit var binding: ActivitySignInStep3Binding
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInStep3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGallery.setOnClickListener {
            pickImageFromGallery()
        }

        binding.btnNext.setOnClickListener {
            if (imageUri != null && binding.editTextName.text.toString().isNotEmpty()) {
                uploadImageToFirebaseStorage()
            } else {
                binding.textViewResult.setTextColor(ContextCompat.getColor(this, R.color.warning))
                binding.textViewResult.text = "You have to select an image and put your name."
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun uploadImageToFirebaseStorage() {
        val storageReference = FirebaseStorage.getInstance().reference.child("images/${System.currentTimeMillis()}")
        val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        val uploadTask = storageReference.putBytes(imageData)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                val currentUser = FirebaseAuth.getInstance().currentUser
                val username = currentUser?.displayName
                val email = currentUser?.email
                val phoneNumber = currentUser?.phoneNumber
                val name = binding.editTextName.text.toString()
                val biography = ""

                currentUser?.let {
                    saveUserToDatabase(it.uid, username, email, phoneNumber, name, biography, uri.toString())
                }
            }
        }.addOnFailureListener { exception ->
            // Handle failure
            // You can display a toast or log the error
        }
    }

    private fun saveUserToDatabase(
        uid: String,
        username: String?,
        email: String?,
        phoneNumber: String?,
        name: String,
        biography: String,
        imageUrl: String
    ) {
        val database = FirebaseDatabase.getInstance().reference

        val user = SignInStep4Activity.User(username, email, phoneNumber, name, biography, imageUrl)

        database.child("users").child(uid).setValue(user)
            .addOnSuccessListener {
                // User data saved successfully.
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                // Error saving user data
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                imageUri = uri
                binding.imageUser.setImageURI(uri)
            }
        }
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 100
    }
}
