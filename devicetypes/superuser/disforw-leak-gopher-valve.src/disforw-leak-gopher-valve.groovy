/*
* Leak Gopher Water Valve
* ----------------------------------
* A Custom DT for the Leak Gopher Water Valve in my house. I wanted one that tells in
* plain text the date and time when it was last opened/closed. Also because of a wiring
* issue, I had to revearse the OPEN/CLOSE roles.
*
* Version History
* ---------------
* 2017-04-24 Initial Re-Code
* 2017-04-25 Revearse OPEN CLOSE roles
* 
* 
* 
*/

metadata {
	definition (name: "disforw - Leak Gopher Valve", namespace: "", author: "disforw") {
		capability "Actuator"
		capability "Valve"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
        capability "Configuration"
        capability "Health Check"

		fingerprint deviceId: "0x1006", inClusters: "0x25"
	}

	// simulator metadata
	simulator {
		status "open": "command: 2503, payload: FF"
		status "close":  "command: 2503, payload: 00"

		// reply messages
		reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
		reply "200100,delay 100,2502": "command: 2503, payload: 00"
	}

	// tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"valve", type: "generic", width: 6, height: 4, canChangeIcon: true, decoration: "flat"){
			tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
				attributeState "open", label: 'OPENED', action: "close", icon: "st.valves.water.open", backgroundColor: "#00A0DC", nextState:"closing"
				attributeState "closed", label: 'CLOSED', action: "open", icon: "st.valves.water.closed", backgroundColor: "#B82121", nextState:"opening"
				attributeState "opening", label: '${name}', icon: "st.valves.water.open", backgroundColor: "#00A0DC"
				attributeState "closing", label: '${name}', icon: "st.valves.water.closed", backgroundColor: "#B82121"
			}
            tileAttribute ("statusText", key: "SECONDARY_CONTROL") {
				//attributeState "statusText", label:'${currentValue}'
                attributeState "statusText", label:'${currentValue}'
            }
		}
        
        valueTile("valve", "device.contact", width: 2, height: 2) {
            state "open", label: 'OPENED', icon: "st.valves.water.open", backgroundColor: "#00A0DC", defaultState: false
            state "closed", label: 'CLOSED', icon: "st.valves.water.closed", backgroundColor: "#B82121", defaultState: true
        }

		standardTile("refresh", "device.contact", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "valve"
		details(["valve", "contact","refresh"])
	}

}

def updated() {
	response(refresh())
}

def parse(String description) {
	log.trace "parse description : $description"
    
	def result = null
	def cmd = zwave.parse(description, [0x20: 1])
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    def value = cmd.value == 0x00 ?  "open" : cmd.value == 0xFF ? "closed" : "unknown"
    [name: "contact", value: value, descriptionText: "$device.displayName valve is $value"]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {   //TODO should show MSR when device is discovered
    log.debug "manufacturerId:   ${cmd.manufacturerId}"
    log.debug "manufacturerName: ${cmd.manufacturerName}"
    log.debug "productId:        ${cmd.productId}"
    log.debug "productTypeId:    ${cmd.productTypeId}"
    def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
    updateDataValue("MSR", msr)
    [descriptionText: "$device.displayName MSR: $msr", isStateChange: false]
}

def zwaveEvent(physicalgraph.zwave.commands.deviceresetlocallyv1.DeviceResetLocallyNotification cmd) {
    [descriptionText: cmd.toString(), isStateChange: true, displayed: true]
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	def value = cmd.value == 0x00 ?  "open" : cmd.value == 0xFF ? "closed" : "unknown"
    def timeString = new Date().format("MM-dd-yyyy h:mm a", location.timeZone)
    sendEvent(name:"statusText", value:timeString)
	[name: "contact", value: value, descriptionText: "$device.displayName valve is $value"]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	[:] // Handles all Z-Wave commands we aren't interested in
}

def open() {
    log.debug "OPENING"
    delayBetween([
            zwave.basicV1.basicSet(value: 0x00).format(),
            zwave.switchBinaryV1.switchBinaryGet().format()
    ],10000) //wait for a water valve to be completely opened
}

def close() {
    log.debug "CLOSING"
    delayBetween([
            zwave.basicV1.basicSet(value: 0xFF).format(),
            zwave.switchBinaryV1.switchBinaryGet().format()
    ],10000) //wait for a water valve to be completely closed
}

def poll() {
    zwave.switchBinaryV1.switchBinaryGet().format()
}

def refresh() {
    log.debug "refresh() is called"
    def commands = [zwave.switchBinaryV1.switchBinaryGet().format()]
    if (getDataValue("MSR") == null) {
        commands << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
    }
    delayBetween(commands,100)
}