package fr.nansty.messenger.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import fr.nansty.messenger.R
import fr.nansty.messenger.models.ChatMessage
import fr.nansty.messenger.models.User
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import java.sql.Timestamp

class ChatLogActivity : AppCompatActivity() {
    companion object{
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()
    var toUser: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        findViewById<RecyclerView>(R.id.recyclerview_chat_log).adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        //val username = intent.getStringExtra(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username

        listenForMessage()

        findViewById<Button>(R.id.send_button_chat_log).setOnClickListener {
            Log.d(TAG, "Attempt to send message")
            performSendMessage()
        }
    }

    private fun listenForMessage() {

        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                Log.d(TAG, chatMessage?.text.toString())

                if(chatMessage != null){

                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid)
                    {
                        val currentUser = LatestMessagesActivity.currentUser ?: return
                        adapter.add(ChatFromItem(chatMessage.text, currentUser))
                    }else
                    {
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }

                }
                findViewById<RecyclerView>(R.id.recyclerview_chat_log).scrollToPosition(adapter.itemCount -1)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }



    private fun performSendMessage() {
        val text = findViewById<TextView>(R.id.edittext_chat_log).text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user!!.uid

        if(fromId == null) return

        //id message after push
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        //ref for the user sending the message
        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()


        val chatMessage = ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG, "Saved our chat message: ${reference.key}")
            //TODO("IMPLEMENT CLEAR edittext_chat_log")
            //findViewById<TextView>(R.id.edittext_chat_log).setText("")
            recyclerview_chat_log.scrollToPosition(adapter.itemCount -1)

        }
        toReference.setValue(chatMessage)

        val latestMessageFromRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageFromRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)

    }
}
class ChatFromItem(val text: String, val user: User): Item<GroupieViewHolder>()
{
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textview_from_row).text = text

        //load our user image
        val uri = user.profileImageUrl
        var targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageview_chat_from_row)
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

}

class ChatToItem(val text: String, val user: User): Item<GroupieViewHolder>()
{
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textview_to_row).text = text

        //load our user image
        val uri = user.profileImageUrl
        var targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageview_chat_to_row)
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

}

