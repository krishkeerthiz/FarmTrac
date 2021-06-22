package com.yourapp.farmtrac.listAdapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.yourapp.farmtrac.R

class  HistoryListAdapter(private val context : Activity, private val names : List<String>, private val points : List<Int>, private val dates : List<String>) :
    ArrayAdapter<String>(context, R.layout.history_list, names){
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.history_list, null, true)

        val nameText = rowView.findViewById(R.id.nameTitle) as TextView
        val pointsText = rowView.findViewById(R.id.pointsTitle) as TextView
        val dateText = rowView.findViewById(R.id.dateTitle) as TextView

        nameText.text = names[position]
        pointsText.text = points[position].toString()
        dateText.text = dates[position]

        return rowView
    }

}