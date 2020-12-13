package fr.nansty.messenger.views

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import fr.nansty.messenger.R
import fr.nansty.messenger.models.ChatMessage
import fr.nansty.messenger.models.User
import fr.nansty.messenger.utils.DateUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.latest_message_row.view.*



class LatestMessageRow(val chatMessage: ChatMessage, val context: Context) : Item<GroupieViewHolder>() {

    var chatPartnerUser: User? = null

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.latest_message_textview).text = chatMessage.text

        val chatPartnerId: String
        if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            chatPartnerId = chatMessage.toId
        } else {
            chatPartnerId = chatMessage.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                chatPartnerUser = snapshot.getValue(User::class.java)

                viewHolder.itemView.findViewById<TextView>(R.id.username_textview_latest_message).text = chatPartnerUser!!.username
                viewHolder.itemView.latest_msg_time.text = DateUtils.getFormattedTime(chatMessage.timestamp)

                val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageview_latest_message)

                if (!chatPartnerUser?.profileImageUrl?.isEmpty()!!) {
                    Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetImageView)
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }


        })

    }

}
