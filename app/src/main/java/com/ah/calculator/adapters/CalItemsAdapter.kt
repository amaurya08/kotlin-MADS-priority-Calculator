package com.ah.calculator.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ah.calculator.CalItem
import com.ah.calculator.R
import com.ah.calculator.databinding.RvCalcItemBinding

class CalItemsAdapter : RecyclerView.Adapter<CalItemsAdapter.ItemViewHolder>() {
    private var items: ArrayList<CalItem> = ArrayList()
    public fun setDataSet(list: ArrayList<CalItem>) {
        this.items = list
        notifyDataSetChanged()
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        public fun bindData(elementAt: CalItem) {
            val infixTV: TextView = itemView.findViewById<TextView>(R.id.rv_tv_infix)
            val resultTV: TextView = itemView.findViewById<TextView>(R.id.rv_tv_ans)
            infixTV.text = elementAt.infixString;
            resultTV.text = "= ${elementAt.result}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = RvCalcItemBinding.inflate(LayoutInflater.from(parent.context))
        return ItemViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindData(items.elementAt(position))
    }

    override fun getItemCount(): Int {
        return items.size
    }
}