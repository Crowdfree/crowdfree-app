package com.huawei.hackzurich

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.huawei.hms.site.api.model.Site
import kotlin.random.Random


class RestaurantAdapter(val restList: List<Site>): RecyclerView.Adapter<RestaurantAdapter.RestViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestViewHolder {

            return RestViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.place_rest_view_holder,
                    parent,
                    false
                )
            )
    }

    override fun onBindViewHolder(holder: RestViewHolder, position: Int) {
        val restViewHolder = holder as RestViewHolder
        //val restaurant = restList[position]
        val restaurant: Site= restList[position]
        if(restaurant!=null){
        restViewHolder.bindRestList(restaurant)
        }
    }

    override fun getItemCount(): Int {
        return restList.count()
    }

    class RestViewHolder(v: View):RecyclerView.ViewHolder(v), View.OnClickListener{
        val restaurantName: TextView = v.findViewById(R.id.restaurantName)
        val restaurantLocation: TextView = v.findViewById(R.id.restaurantLocation)
        val crowdedness: TextView = v.findViewById(R.id.crowdedness)
        val directionsButton: TextView = v.findViewById(R.id.directionsButton)

        var arr= arrayOf("Low Crowd", "Medium Crowd", "High Crowd")
        fun bindRestList(restaurant: Site){
            this.restaurantName.text = restaurant.name
            this.restaurantLocation.text = restaurant.formatAddress
            this.crowdedness.text = arr.random()
        }

        override fun onClick(p0: View?) {
            TODO("Not yet implemented")
        }

    }

}