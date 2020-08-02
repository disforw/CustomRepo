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
	definition (name: "disforw - Presence with Switch", namespace: "", author: "disforw", oauth: false, ocfDeviceType: "x.com.st.d.mobile.presence") {
	capability "Sensor"
        capability "Occupancy Sensor"
        capability "Presence Sensor"
        capability "Switch"

	}
	

	// UI tile definitions
	tiles {
        standardTile("presence", "device.presence", width: 3, height: 3, canChangeBackground: true) {
		state "not present", labelIcon:"st.presence.tile.not-present", backgroundColor:"#ffffff"
        	state "present", labelIcon:"st.presence.tile.present", backgroundColor:"#53a7c0"
	}
	standardTile("switch", "device.switch", inactiveLabel: false, decoration: "flat") {
		state "off", action:"switch.on"
		state "on", action:"switch.off"
	}


		main(["presence"])
		details(["presence, switch"])
	}
}

def parse(String description) { }

def updated() { }
def on() {	sendEvent(name: 'presence', value: 'present', descriptionText: "User has Arrived");
sendEvent(name: 'switch', value: 'on')}
def off() {	sendEvent(name: 'presence', value: 'not present', descriptionText: "User has Left");
sendEvent(name: 'switch', value: 'off')	}
