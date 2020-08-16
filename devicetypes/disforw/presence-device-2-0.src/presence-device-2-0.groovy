/**
 *  Presence Toggle
 * ----------------
 *  A simple presence handler that can be controlled by a switch.
 *
 *
 *  2015-04-01  Initial code copied from impliciter
 *  2017-10-23  Added macAddress attribute for reference from WebCoRE
 *  2020-08-01  Code completely remodified, and simplified code.
 */
metadata {
	definition (name: "Presence Device 2.0", namespace: "disforw", author: "Ben Abrams", cstHandler: true, ocfDeviceType: "x.com.st.d.sensor.presence") {
		capability "Presence Sensor"
		capability "Switch"
	}
	
	tiles {
        standardTile("presence", "device.presence", canChangeBackground: true) {
		state "not present", labelIcon:"st.presence.tile.not-present"
        state "present", labelIcon:"st.presence.tile.present"
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