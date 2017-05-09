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
 *
 *
 *  Uses code from SmartThings
 *
 */
metadata {
	definition (name: "disforw - WOL Button Tile", namespace: "", author: "disforw") {
        capability "Button"
		capability "Momentary"
        
		attribute "about", "string"
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name: "button", type: "generic", width: 6, height: 4, canChangeIcon: true, canChangeBackground: true) {
			tileAttribute("device.button", key: "PRIMARY_CONTROL") {
				attributeState "off", label: 'PUSH', action: "momentary.push", icon: "st.samsung.da.RC_ic_power", nextState: "on"
				attributeState "on", label: 'PUSH', action: "momentary.push", icon: "st.samsung.da.RC_ic_power", backgroundColor:"#00A0DC"
			}
            tileAttribute ("statusText", key: "SECONDARY_CONTROL") {
                attributeState "default", label:'${currentValue}'
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
		details (["button"])
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
	def dstamp = new Date().format( "MM-dd-yyyy h:mm a", location.timeZone )
	log.debug "time: ${dstamp}"    
    
	def versionTxt = "Last WOL sent:  ${dstamp}"
	sendEvent (name: "statusText", value:versionTxt, displayed: false) 
} 

