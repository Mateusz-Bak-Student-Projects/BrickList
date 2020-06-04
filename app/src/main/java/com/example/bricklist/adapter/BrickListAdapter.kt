package com.example.bricklist.adapter

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bricklist.R
import com.example.bricklist.model.InventoryItem
import kotlinx.android.synthetic.main.brick_list_item.view.*

class BrickListAdapter(private val brickList: List<InventoryItem>) :
    RecyclerView.Adapter<BrickListAdapter.BrickViewHolder>() {

    class BrickViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrickViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.brick_list_item, parent, false)
        return BrickViewHolder(view)
    }

    override fun onBindViewHolder(holder: BrickViewHolder, position: Int) {
        val item = brickList[position]
        holder.view.apply {
            val name = if (!item.extra) item.name else "[Extra] ${item.name}".also {
                itemName.setTypeface(itemName.typeface, Typeface.BOLD_ITALIC)
            }
            itemName.text = name
            itemDescription.text =
                context.getString(R.string.brick_description, item.color, item.code)
            itemQuantity.text = context.getString(R.string.quantity_text, item.inStore, item.inSet)
            if (item.image != null) itemImage.setImageBitmap(item.image)
            if (item.inStore == item.inSet) {
                itemName.setTextColor(Color.LTGRAY)
                itemName.paintFlags = itemName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                itemName.setTextColor(Color.BLACK)
                itemName.paintFlags = itemName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            increment.setOnClickListener {
                if (item.inStore < item.inSet) {
                    item.inStore++
                    notifyItemChanged(position)
                }
            }
            decrement.setOnClickListener {
                if (item.inStore > 0) {
                    item.inStore--
                    notifyItemChanged(position)
                }
            }
        }
    }

    override fun getItemCount() = brickList.size
}
