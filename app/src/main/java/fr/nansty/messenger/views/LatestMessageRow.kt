package fr.nansty.messenger.views

import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import fr.nansty.messenger.R
import fr.nansty.messenger.models.ChatMessage
import fr.nansty.messenger.models.User

class LatestMessageRow(val chatMessage: ChatMessage): Item<GroupieViewHolder>(){

    var chatPartnerUser: User? = null

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.message_textview_latest_message).text = chatMessage.text

        val chatPartnerId: String
        if(chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            chatPartnerId = chatMessage.toId
        } else {
            chatPartnerId = chatMessage.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatPartnerUser  = snapshot.getValue(User::class.java)
                viewHolder.itemView.findViewById<TextView>(R.id.username_textview_latest_message).text = chatPartnerUser!!.username

                val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageview_latest_message)
                Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetImageView)
            }
            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

}