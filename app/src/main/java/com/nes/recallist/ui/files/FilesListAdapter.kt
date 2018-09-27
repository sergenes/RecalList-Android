package com.nes.recallist.ui.files

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.google.api.services.drive.model.File
import com.nes.recallist.R
import com.nes.recallist.tools.getExtDrawable
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.find
import java.util.ArrayList

class FilesListAdapter(var context: Context) : BaseAdapter() {
    var files: MutableList<File> = ArrayList(5)
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    var selected: Int = -1

    override fun getCount(): Int = files.size

    override fun getItem(position: Int): Any? = files[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val file = files[position]
        val view: View?
        val holder: ListRowHolder

        if (convertView?.tag !is View) {
            view = this.inflater.inflate(R.layout.files_list_item, parent, false)
            holder = ListRowHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ListRowHolder
        }
        if (position % 2 == 0) {
            holder.rowContainerLayout.backgroundDrawable = context.getExtDrawable(R.drawable.white_line_selector)
        } else {
            holder.rowContainerLayout.background = context.getExtDrawable(R.drawable.grey_line_selector)
        }

        holder.titleTextView.text = file.name
        holder.detailsTextView.text = file.modifiedTime.toStringRfc3339()

        return view
    }
}

private class ListRowHolder(row: View) {
    var titleTextView: TextView = row.find(R.id.titleTextView)
    var detailsTextView: TextView = row.find(R.id.detailsTextView)
    var rowContainerLayout: LinearLayout = row.find(R.id.rowContainerLayout)
}