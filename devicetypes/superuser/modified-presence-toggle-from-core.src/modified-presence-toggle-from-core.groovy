/**
 *  Presence Toggle
 * ----------------
 *  Use this "Presence Sensor" to be toggled from a COrE piston. You can execute
 *  the piston via a DD-WRT script, I will try and post the script that I use to Github.
 *
 *
 *  2015-04-01  Initial code copied from impliciter
 */
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Modified - Presence Toggle from CoRE", namespace: "", author: "disforw", oauth: false) {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
        capability "Presence Sensor"
	}

	// simulator metadata
	simulator {	}

	// UI tile definitions
	tiles {
        standardTile("presence", "device.presence", width: 3, height: 3, canChangeBackground: true) {
			state "not present", labelIcon:"st.presence.tile.not-present", backgroundColor:"#ffffff"
            state "present", labelIcon:"st.presence.tile.present", backgroundColor:"#53a7c0"
		}

		main(["presence"])
		details(["presence"])
	}
}

def parse(String description) { }

def on() {	sendEvent(name: 'presence', value: 'present', descriptionText: "User has Arrived")	}
def off() {	sendEvent(name: 'presence', value: 'not present', descriptionText: "User has Left")	}