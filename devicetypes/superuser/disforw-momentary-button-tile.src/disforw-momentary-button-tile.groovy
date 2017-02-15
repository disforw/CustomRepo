/**
 * Momentary Button Tile
 *----------------------
 * Use this simulated button in conjunction with a COrE piston to wake-on-lan device
 * 
 * 2017-01-07  Template taken from SmartThings Repo
 * 2017-01-08  Replace old icon with dope power button
 * 2017-01-08  Created COrE piston to send WOL packet to device
 *
 *
 *  Uses code from SmartThings
 *
 */
metadata {
	definition (name: "disforw - Momentary Button Tile", namespace: "", author: "disforw") {
		capability "Actuator"
        capability "Button"
		capability "Switch"
		capability "Momentary"
		capability "Sensor"
        
		attribute "about", "string"
	}

	// simulator metadata
	simulator {
	}
	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name: "button", type: "generic", width: 6, height: 4, canChangeIcon: true, canChangeBackground: true) {
			tileAttribute("device.button", key: "PRIMARY_CONTROL") {
				attributeState "off", label: 'PUSH', action: "momentary.push", icon: "https://dl.dropboxusercontent.com/u/19576368/SmartThings/buttonC.png", nextState: "on"
				attributeState "on", label: 'PUSH', action: "momentary.push", icon: "https://dl.dropboxusercontent.com/u/19576368/SmartThings/buttonO.png"
			}
        }
        standardTile("blankTile", "statusText", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "default", label:'', icon:"st.samsung.da.RC_ic_power"
		}
        //icon: st.samsung.da.RC_ic_power
        valueTile("aboutTxt", "device.about", inactiveLabel: false, decoration: "flat", width: 5, height:1) {
            state "default", label:'${currentValue}'
		}
        
        main "blankTile"
		details (["button","blankTile","aboutTxt"])
	}
}
def installed() {
	showVersion()	
}

def parse(String description) {
}

def push() {
	
    //sendEvent(name: "button", value: "on", isStateChange: true, displayed: false)
	//sendEvent(name: "button", value: "off", isStateChange: true, displayed: false)
	sendEvent(name: "packet", value: "Sent", isStateChange: true)
    showStamp()
	sendEvent(name: "button", value: "pushed", data: [buttonNumber: "1"], displayed: false, isStateChange: true)
}

def showStamp(){
	def dstamp = new Date().format( 'yyyy-M-d hh:mm:ss',TimeZone.getTimeZone('EST') )
	log.debug "time: ${dstamp}"    
    
	def versionTxt = "Last WOL sent:  ${dstamp}"
	sendEvent (name: "about", value:versionTxt, displayed: false) 
} 

