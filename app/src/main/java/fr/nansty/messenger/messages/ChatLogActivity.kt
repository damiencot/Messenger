package fr.nansty.messenger.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import fr.nansty.messenger.R
import fr.nansty.messenger.models.User
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import java.sql.Timestamp

class ChatLogActivity : AppCompatActivity() {
    companion object{
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        findViewById<RecyclerView>(R.id.recyclerview_chat_log).adapter = adapter

        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        //val username = intent.getStringExtra(NewMessageActivity.USER_KEY)
        supportActionBar?.title = user!!.username

        //setupDummyData()
        listenForMessage()

        findViewById<Button>(R.id.send_button_chat_log).setOnClickListener {
            Log.d(TAG, "Attempt to send message")
            performSendMessage()
        }
    }

    private fun listenForMessage() {
        val ref = FirebaseDatabase.getInstance().getReference("/messages")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                Log.d(TAG, chatMessage?.text.toString())

                if(chatMessage != null){

                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid)
                    {
                        adapter.add(ChatFromItem(chatMessage.text))
                    }else
                    {
                        adapter.add(ChatToItem(chatMessage.text))
                    }

                }
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

    class ChatMessage(val id: String, val text: String, val fromId: String, val toId: String, val timestamp: Long){
        constructor(): this("","","","", -1)
    }

    private fun performSendMessage() {
        val text = findViewById<TextView>(R.id.edittext_chat_log).text.toString()

        //id message after push
        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user!!.uid

        if(fromId == null) return

        val chatMessage = ChatMessage(reference.key!!, text, fromId!!, toId, System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG, "Saved our chat message: ${reference.key}")

        }
    }

    private fun setupDummyData() {
        val adapter = GroupAdapter<GroupieViewHolder>()


        adapter.add(ChatFromItem("FROM MESSAGE"))
        adapter.add(ChatToItem("TO MESSAGE"))


        findViewById<RecyclerView>(R.id.recyclerview_chat_log).adapter = adapter
    }
}
class ChatFromItem(val text: String): Item<GroupieViewHolder>()
{
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textview_from_row).text = text
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

}

class ChatToItem(val text: String): Item<GroupieViewHolder>()
{
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textview_to_row).text = text
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

}

