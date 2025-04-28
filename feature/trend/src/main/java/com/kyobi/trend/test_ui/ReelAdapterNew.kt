package com.kyobi.trend.test_ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kyobi.feature.trend.R

data class TestReel(
    val id: String,
    val title: String
)

class ReelAdapterNew(
    val items: List<TestReel>,
    private val context: Context
) : RecyclerView.Adapter<ReelAdapterNew.ViewHolder>() {

    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
        val textView: TextView = itemView.findViewById(R.id.text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reel_new, parent, false)
        // Lấy chiều cao của RecyclerView (parent của item)
        val recyclerViewHeight = (parent as RecyclerView).height

        // Set chiều cao item bằng chiều cao của RecyclerView
        view.layoutParams = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            if (recyclerViewHeight > 0) recyclerViewHeight else parent.context.resources.displayMetrics.heightPixels // Fallback nếu RecyclerView chưa đo xong
        )
        view.setPadding(0, 0, 0, 0)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textView.text = item.title

        // Tùy chỉnh màu nền để dễ phân biệt các item
        val colors = listOf(
            ContextCompat.getColor(context, android.R.color.holo_red_light),
            ContextCompat.getColor(context, android.R.color.holo_blue_light),
            ContextCompat.getColor(context, android.R.color.holo_green_light),
            ContextCompat.getColor(context, android.R.color.holo_orange_light)
        )
        holder.imageView.setBackgroundColor(colors[position % colors.size])
    }

    override fun getItemCount(): Int = items.size
}