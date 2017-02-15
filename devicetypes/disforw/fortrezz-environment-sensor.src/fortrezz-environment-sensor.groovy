/*
* FortrezZ Moisture Temp Sensor v2.0
* ----------------------------------
* After spending much time on the phone with FortrezZ telling them that their integration does not
* work, I just decided to do it myself. So here is my fully functional version for the following models:
* WWA02AAUSW; WWA02AAUSB; WWA01AAUSW; WWA01AAUSB by FortrezZ
*
*
* Version History
* ---------------
* 2017-01-07 Initial Re-Code
* 2017-02-15 Added proper sendEvent to trigger other apps for water, temperature and battery

*/

metadata {
	// Automatically generated. Make future change here.
	definition (name: "Fortrezz Environment Sensor", namespace: "disforw", author: "Ben Abrams") {
		capability "Water Sensor"
		capability "Sensor"
		capability "Battery"
        capability "Temperature Measurement"
        
        command "wet"
        command "dry"

		fingerprint deviceId: "0x2001", inClusters: "0x30,0x9C,0x9D,0x85,0x80,0x72,0x31,0x84,0x86"
		fingerprint deviceId: "0x2101", inClusters: "0x71,0x70,0x85,0x80,0x72,0x31,0x84,0x86"
	}

	simulator {
		status "dry": "command: 7105, payload: 00 00 00 FF 05 FE 00 00"
		status "wet": "command: 7105, payload: 00 FF 00 FF 05 02 00 00"
		status "overheated": "command: 7105, payload: 00 00 00 FF 04 02 00 00"
		status "freezing": "command: 7105, payload: 00 00 00 FF 04 05 00 00"
		status "normal": "command: 7105, payload: 00 00 00 FF 04 FE 00 00"
		for (int i = 0; i <= 100; i += 20) {
			status "battery ${i}%": new physicalgraph.zwave.Zwave().batteryV1.batteryReport(batteryLevel: i).incomingMessage()
		}
	}
	tiles {
        
        multiAttributeTile(name:"main", type: "lighting", width: 4, height: 2, canChangeIcon: true, decoration: "flat"){
			tileAttribute ("device.main", key: "PRIMARY_CONTROL") {
            attributeState "dry", label: "NORMAL", icon:"st.Home.home1", backgroundColor:"#ffffff"
            attributeState "freezing", label: "FREEZING", icon:"st.alarm.temperature.freeze", backgroundColor:"#53a7c0"
            attributeState "overheated", label: "OVERHEATED", icon:"st.alarm.temperature.overheat", backgroundColor:"#F80000"
            attributeState "wet", label: "WET", icon:"st.alarm.water.wet", backgroundColor:"#53a7c0"
            }
            tileAttribute ("device.temperature", key: "SECONDARY_CONTROL") {
            attributeState "temp", label:'${currentValue}°', icon:"st.alarm.temperature.normal"
            }
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'Request update', action:"refresh.refresh"
		}

		standardTile("blankBatt", "statusText", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "default", label:'', icon:"https://dl.dropboxusercontent.com/u/19576368/SmartThings/battery.png" }
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 5, height: 1) { 
			state "battery", label:'The Battery level reported at: ${currentValue}%', unit:""
		}

		main (["main"])
		details(["main", "blankBatt","battery"])
	}
}

def refresh() {
    //zwave.wakeUpV2.wakeUpIntervalSet(seconds:2 * 3600, nodeid:zwaveHubNodeId).format()
    log.debug "Requesting temperature"
	delayBetween([
        zwave.sensormultilevelv1.SensorMultilevelGet().format()
	])
    //log.debug "Sent wakeup config command for ${zwaveHubNodeId}"
    log.debug "Requested temperature for ${zwaveHubNodeId}"
}

def parse(String description) {

	def parsedZwEvent = zwave.parse(description, [0x30: 1, 0x71: 2, 0x84: 1, 0x31: 2])
	def zwEvent = zwaveEvent(parsedZwEvent)
	def result = []

	log.debug "Parser description ${description}"


	result << createEvent( zwEvent )

	if( parsedZwEvent.CMD == "8407" ) {
        if (!state.lastbatt || (new Date().time) - state.lastbatt > 24*60*60*1000) {
                result << response(zwave.batteryV1.batteryGet())
                result << response("delay 1200")  // leave time for device to respond to batteryGet
        }
		result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
        //batt
        /*
		def lastStatus = device.currentState("battery")
		def ageInMinutes = lastStatus ? (new Date().time - lastStatus.date.time)/60000 : 600
		log.debug "Battery status was last checked ${ageInMinutes} minutes ago"

		if (ageInMinutes >= 600) {
			log.debug "Battery status is outdated, requesting battery report"
			result << new physicalgraph.device.HubAction(zwave.batteryV1.batteryGet().format())
            //?device.currentState("battery") = new Date().time;
		}
        
		result << new physicalgraph.device.HubAction(zwave.wakeUpV1.wakeUpNoMoreInformation().format())
        */
	}

	log.debug "Parse returned ${result}"
	return result
}
def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv2.SensorMultilevelReport cmd)
{
	def map = [:]
	switch (cmd.sensorType) {
		case 1:
			// temperature
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.unit = getTemperatureScale()
			map.name = "temperature"
            map.displayed = false //Remove this line to see the temp changing events
			break;
			//log.debug "Got temperature: $map.value"
	}
	map
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
	[descriptionText: "${device.displayName} woke up", isStateChange: false]
}

/*
def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd)
{
	def map = [:]
	map.name = "main" //ORIGINAL: map.name = "water"
	map.value = cmd.sensorValue ? "wet" : "dry"
	map.descriptionText = "${device.displayName} is ${map.value}"
	//map
    sendEvent(map)
    sendEvent(name:"water", map.value, map.descriptionText, displayed: false)
} */

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [:]
	if(cmd.batteryLevel == 0xFF) {
		map.name = "battery"
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
		map.displayed = true
	} else {
		map.name = "battery"
		map.value = cmd.batteryLevel > 0 ? cmd.batteryLevel.toString() : 1
		map.unit = "%"
		map.displayed = false
	}
    //batt
	state.lastbatt = new Date().time

	map
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv2.AlarmReport cmd)
{
	def map = [:]
	if (cmd.zwaveAlarmType == physicalgraph.zwave.commands.alarmv2.AlarmReport.ZWAVE_ALARM_TYPE_WATER) {
		map.name = "main" //ORIGINAL: map.name = "water"
		map.value = cmd.alarmLevel ? "wet" : "dry"
		map.descriptionText = "${device.displayName} is ${map.value}"
        sendEvent(name:"water", value: map.value, displayed: false)

	}
	if(cmd.zwaveAlarmType ==  physicalgraph.zwave.commands.alarmv2.AlarmReport.ZWAVE_ALARM_TYPE_HEAT) {
		map.name = "main" //ORIGINAL: map.name = "temperature"
		if(cmd.zwaveAlarmEvent == 1) { map.value = "overheated"}
		if(cmd.zwaveAlarmEvent == 2) { map.value = "overheated"}
		if(cmd.zwaveAlarmEvent == 3) { map.value = "changing temperature rapidly"}
		if(cmd.zwaveAlarmEvent == 4) { map.value = "changing temperature rapidly"}
		if(cmd.zwaveAlarmEvent == 5) { map.value = "freezing"}
		if(cmd.zwaveAlarmEvent == 6) { map.value = "freezing"}
		if(cmd.zwaveAlarmEvent == 254) { map.value = "dry"}
		map.descriptionText = "${device.displayName} is ${map.value}"
        //sendEvent(name:"temperature", value: map.value, displayed: false)
	}
    map
}

def zwaveEvent(physicalgraph.zwave.Command cmd)
{
	log.debug "COMMAND CLASS: $cmd"
}