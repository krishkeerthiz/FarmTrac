package com.yourapp.farmtrac.listAdapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.yourapp.farmtrac.R


class BalanceListAdapter(private val context: Activity, private val names: List<String>, private val amounts : List<Int>) :
    ArrayAdapter<String>(context, R.layout.balance_list, names) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.balance_list, null, true)

        val nameText = rowView.findViewById(R.id.nameTitle) as TextView
        val amountText = rowView.findViewById(R.id.amountTitle) as TextView

        nameText.text = names[position]
        amountText.text = amounts[position].toString()

        return rowView
    }
}
