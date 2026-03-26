package com.example.chillrate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.neurosdk2.neuro.types.SensorInfo

class SensorAdapter(
    private val sensors: MutableList<SensorInfo>,
    private val onClick: (SensorInfo) -> Unit
) : RecyclerView.Adapter<SensorAdapter.SensorViewHolder>() {
    private val batteryMap = mutableMapOf<String, Int>()

    class SensorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.sensorName)
        val address: TextView = view.findViewById(R.id.sensorAddress)
        val battery: TextView = view.findViewById(R.id.sensorBattery)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sensor_item, parent, false)
        return SensorViewHolder(view)
    }

    override fun getItemCount() = sensors.size

    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {

        val sensor = sensors[position]

        holder.name.text = sensor.name
        holder.address.text = "MAC: ${sensor.address}"

        val battery = batteryMap[sensor.address]

        if (battery != null) {
            holder.battery.text = "$battery%"
        } else {
            holder.battery.text = "--%"
        }

        holder.itemView.setOnClickListener {
            onClick(sensor)
        }
    }


    fun update(newSensors: List<SensorInfo>) {
        sensors.clear()
        sensors.addAll(newSensors)
        notifyDataSetChanged()
    }

    fun updateBattery(address: String, battery: Int) {

        batteryMap[address] = battery

        notifyDataSetChanged()
    }

}