package fr.nansty.messenger.registerlogin

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import fr.nansty.messenger.R
import fr.nansty.messenger.messages.LatestMessagesActivity
import fr.nansty.messenger.models.User
import java.util.*

class RegisterActivity : AppCompatActivity() {

    lateinit var username: TextView
    var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        username = findViewById<TextView>(R.id.username_editText_register)


        findViewById<Button>(R.id.register_button_register).setOnClickListener {
            performRegister()
        }

        findViewById<TextView>(R.id.already_have_account_text_view).setOnClickListener {
            Log.d("RegisterActivity", "already_have_account_text_view")
            //Launch the login activity somehow
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.selectphoto_view_register).setOnClickListener {
            Log.d("RegisterActivity", "Try to show photo")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("RegisterActivity", "Photo was selected")

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            val bitmapDrawable = BitmapDrawable(this.resources, bitmap)
            findViewById<ImageView>(R.id.selectphoto_view_register).setImageDrawable(bitmapDrawable)
            //findViewById<CircleImageView>(R.id.selectphoto_imageview_register).setImageDrawable(bitmapDrawable)
            //findViewById<CircleImageView>(R.id.selectphoto_imageview_register).alpha = 0f

        }
    }

//    private fun getCapturedImage(selectedPhotoUri: Uri): Bitmap {
////        val bitmap = when {
////            Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
////                this.contentResolver,
////                selectedPhotoUri
////            )
////            else -> {
////                val source = ImageDecoder.createSource(this.contentResolver, selectedPhotoUri)
////                ImageDecoder.decodeBitmap(source)
////            }
////        }
////        try {
////            selectedPhotoUri?.let {
////                val unit: Unit? = if (Build.VERSION.SDK_INT < 28) {
////                    imageBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true)
////                    val bitmap = MediaStore.Images.Media.getBitmap(
////                        this.contentResolver,
////                        selectedPhotoUri
////                    )
////                    findViewById<Button>(R.id.selectphoto_button_register).setImageBitmap(bitmap)
////                } else {
////                    val source =
////                        ImageDecoder.createSource(this.contentResolver, selectedPhotoUri)
////                    val bitmap = ImageDecoder.decodeBitmap(source)
////                    findViewById<Button>(R.id.selectphoto_button_register).setImageBitmap(bitmap)
////                }
////                unit
////            }
////        } catch (e: Exception) {
////            e.printStackTrace()
////        }
//    }

    private fun performRegister() {
        val email = findViewById<TextView>(R.id.email_editText_register).text.toString()
        val password = findViewById<TextView>(R.id.password_editText_register).text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please eneter text in email or password", Toast.LENGTH_LONG)
                .show()
            return
        }

        Log.d("RegisterActivity", "email_editText_register: $email")
        Log.d("RegisterActivity", "password_editText_register: $password")

        //Firebase Authentication to create a user with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
            email,
            password
        ).addOnCompleteListener {
            if (!it.isSuccessful) return@addOnCompleteListener
            Log.d("RegisterActivity", "Successful created user with uid: ${it.result?.user?.uid}")

            uploadImageToFirebaseStorage()
        }.addOnFailureListener {
            Log.d("RegisterActivity", "Failed to create user: ${it.message}")
            Toast.makeText(this, "Failed to create user", Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            Log.d("RegisterActivity", "Successfully uploaded image: ${it.metadata?.path}")

            ref.downloadUrl.addOnSuccessListener {
                Log.d("RegisterActivity", "File Location: $it")
                saveUserToFirebaseDatabase(it.toString())
            }
            .addOnFailureListener {

             }
        }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username.text.toString(), profileImageUrl)

        ref.setValue(user).addOnSuccessListener {
            Log.d("RegisterActivity", "Finally we saved the user to Firebase Database")

            val intent = Intent(this, LatestMessagesActivity::class.java)
            intent.flags =  Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

}

