/**
 * WOL Momentary Button Tile
 *----------------------
 * Use this simulated button in conjunction with a COrE piston to wake-on-lan device
 * 
 * 2017-01-07  Template taken from SmartThings Repo
 * 2017-01-08  Replace old icon with dope power button
 * 2017-01-08  Created COrE piston to send WOL packet to device
 * 2017-05-09  Removed info line and moved in to main tile
 * 2017-05-09  changed timezone to local hub
 * 2017-09-08  added a switch to use for scheduling
 *
 *  Uses code from SmartThings
 *
 */
metadata {
	definition (name: "disforw - WOL Button Tile", namespace: "", author: "disforw") {
        capability "Button"
		capability "Momentary"
        capability "switch"
        
		attribute "about", "string"
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name: "button", type: "generic", width: 6, height: 4, canChangeIcon: true, canChangeBackground: true) {
			tileAttribute("device.button", key: "PRIMARY_CONTROL") {
				attributeState "off", label: 'SEND PACKET', action: "momentary.push", icon: "st.samsung.da.RC_ic_power", nextState: "on"
				attributeState "on", label: 'PACKET SENT', action: "momentary.push", icon: "st.samsung.da.RC_ic_power", backgroundColor:"#00A0DC"
			}
            tileAttribute ("dateText", key: "SECONDARY_CONTROL") {
                attributeState "default", label:'Last WOL Sent: ${currentValue}'
            }
        }
        multiAttributeTile(name: "switch", type: "generic", width: 6, height: 4, canChangeIcon: false) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label: 'SCHEDULE OFF', action: "on", icon: "st.samsung.da.RC_ic_power", backgroundColor: "#B82121"
				attributeState "on", label: 'SCHEDULE ON', action: "off", icon: "st.samsung.da.RC_ic_power"
			}
            tileAttribute ("statusText", key: "SECONDARY_CONTROL") {
                attributeState "default", label:'${currentValue}'
            }
        }
        
        standardTile("sched", "device.switch", decoration: "flat") {
            state "off", label: 'auto: off', backgroundColor: "#B82121"
            state "on", label: 'auto: on'
        }
        
        main "sched"
		details (["button", "switch"])
	}
}
def installed() {
	showVersion()	
}

def parse(String description) {
}

def push() {
    showStamp()
	sendEvent(name: "button", value: "pushed", data: [buttonNumber: "1"], displayed: false, isStateChange: true)
}

def showStamp(){
	def dstamp = new Date().format( "MM-dd-yyyy h:mm a", location.timeZone )
	log.debug "time: ${dstamp}"    
	sendEvent (name: "dateText", value:dstamp, displayed: false) 
} 

def on(){
log.debug "switch ON"
sendEvent (name: "switch", value:on, displayed: false)
sendEvent (name: "statusText", value: "The system will auto-on at 3pm everyday")
}

def off(){
log.debug "switch OFF"
sendEvent (name: "switch", value:off, displayed: false)
sendEvent (name: "statusText", value: "The auto-on has been disabled")
}