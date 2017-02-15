/**
 *  Momentary Button Tile
 *
 *  Copyright 2016 Michael Struck
 *  Version 1.0.3 3/18/16
 *
 *  Version 1.0.0 Initial release
 *  Version 1.0.1 Reverted back to original icons for better GUI experience
 *  Version 1.0.2 Added dynamic feedback to user on code version of switch
 *  Version 1.0.3 Added PNG style icons to better differenciate the Alexa Helper created devices
 *
 *  Uses code from SmartThings
 *
 */
metadata {
	definition (name: "Momentary Button Tile", namespace: "Customized", author: "SmartThings") {
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

