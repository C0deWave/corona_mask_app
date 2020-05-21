package com.jumyeong.corona

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView

class AutoCompleteSearchItem(context: Context, ArrayListFull: ArrayList<SearchItem>) :ArrayAdapter<SearchItem>(context,0,ArrayList()) {
    override fun getFilter(): Filter {
        return searchFilter
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var a = convertView
        if (a == null){
            a = LayoutInflater.from(context).inflate(
                R.layout.adapter,parent,false
            )
        }

        a?.findViewById<TextView>(R.id.testMainName)
        a?.findViewById<TextView>(R.id.testSubName)

        var searchItem :SearchItem? = getItem(position)

        if (searchItem != null){
            a?.findViewById<TextView>(R.id.testMainName)?.setText(searchItem.name)
            a?.findViewById<TextView>(R.id.testSubName)?.setText(searchItem.snippet)
        }

        return a
    }

    val searchFilter = object : Filter(){
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val result : FilterResults = object :FilterResults(){}
            val suggestions :MutableList<SearchItem> = ArrayList<SearchItem>()

            if (constraint == null || constraint.length == 0){
                suggestions.addAll(ArrayListFull)
            }else{
                val filterPattern = constraint.toString().toLowerCase().trim()

                for (item:SearchItem in ArrayListFull){
                    if (item.name.toLowerCase().contains(filterPattern)){
                        suggestions.add(item)
                    }
                }
            }

            result.values = suggestions
            result.count = suggestions.size

            return result
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            clear()
            addAll(results?.values as List<SearchItem>)
            notifyDataSetChanged()
        }

        override fun convertResultToString(resultValue: Any?): CharSequence {
            return (resultValue as SearchItem).name
        }

    }

}