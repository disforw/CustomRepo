/**
 *  Presence Toggle
 * ----------------
 *  Use this "Presence Sensor" to be toggled from a COrE piston. You can execute
 *  the piston via a DD-WRT script, I will try and post the script that I use to Github.
 *
 *
 *  2015-04-01  Initial code copied from impliciter
 *  2017-10-23  Added macAddress attribute for reference from WebCoRE
 */
metadata {
	// Automatically generated. Make future change here.
	definition (name: "disforw - Presence Toggle from CoRE", namespace: "", author: "disforw", oauth: false) {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
        capability "Presence Sensor"
        
        attribute "macAddress", "string"
	}
	
    preferences {
        input "macAddresss", "macAddress", title: "MAC Address", description: "Enter the MAC address of the device you want to monitor", required: true
	}
	// UI tile definitions
	tiles {
        standardTile("presence", "device.presence", width: 3, height: 3, canChangeBackground: true) {
			state "not present", labelIcon:"st.presence.tile.not-present", backgroundColor:"#ffffff"
            state "present", labelIcon:"st.presence.tile.present", backgroundColor:"#53a7c0"
		}
        standardTile("button", "device.switch", width: 2, height: 2, canChangeIcon: false,  canChangeBackground: true) {
			state "off", label: 'Away', action: "switch.on",  nextState: "on"
			state "on", label: 'Present', action: "switch.off", nextState: "off"
		}


		main(["presence"])
		details(["presence"])
	}
}

def parse(String description) { }

def updated(){
	//log.debug "The pref has changed to $macAddresss"
    //def macAddress = "$macAddresss"
	sendEvent (name: "macAddress", value: "$macAddresss", displayed: false)
}

def on() {	sendEvent(name: 'presence', value: 'present', descriptionText: "User has Arrived")	}
def off() {	sendEvent(name: 'presence', value: 'not present', descriptionText: "User has Left")	}