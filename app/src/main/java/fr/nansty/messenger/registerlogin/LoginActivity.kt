package fr.nansty.messenger.registerlogin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import fr.nansty.messenger.R
import fr.nansty.messenger.messages.LatestMessagesActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<Button>(R.id.login_button_login).setOnClickListener {
//            val email = findViewById<TextView>(R.id.email_edittext_login).text.toString()
//            val password = findViewById<TextView>(R.id.password_edittext_login).text.toString()
//
//            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener {
//                    if (!it.isSuccessful) return@addOnCompleteListener
//                    Log.d("Main", "Successful signed user with uid: ${it.result?.user?.uid}")
//                }
//                .addOnFailureListener{
//                    Log.d("Main", "Failed to signed user: ${it.message}")
//                }
            performLogin()

        }

        findViewById<TextView>(R.id.back_to_register_textview).setOnClickListener {
            finish()
        }
    }

    private fun performLogin() {
        val email = findViewById<TextView>(R.id.email_edittext_login).text.toString()
        val password = findViewById<TextView>(R.id.password_edittext_login).text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill out email/pw.", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                Log.d("Login", "Successfully logged in: ${it.result!!.user!!.uid}")

                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to log in: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}