package fr.nansty.messenger.registerlogin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import fr.nansty.messenger.R

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<Button>(R.id.login_button_login).setOnClickListener {
            val email = findViewById<TextView>(R.id.email_edittext_login).text.toString()
            val password = findViewById<TextView>(R.id.password_edittext_login).text.toString()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener
                    Log.d("Main", "Successful signed user with uid: ${it.result?.user?.uid}")
                }
                .addOnFailureListener{
                    Log.d("Main", "Failed to signed user: ${it.message}")
                }

        }

        findViewById<TextView>(R.id.back_to_register_textview).setOnClickListener {
            finish()
        }
    }
}