/**
* MIMOlite as a Burg Alarm Control and Monitor with Fire, Burg, Armed, and Disarmed capability.
*	
*	Based on code from MIMOlite Garage Door Sensor with Position Indicator by Skyjunky 
*	https://community.smartthings.com/t/mimolite-garage-door-sensor-with-position-indicator/46696
*
* Inspired by a similar handler written by https://community.smartthings.com/users/johnconstantelo
* Great code writen by MIGNLOU to tie a MIMOlite to the keyswitch of an Alarm Panel!!
* 
* 2017/09/08 Removed all event related to motion and changed colors to match SmartThings latest design
* 2017/09/08 Added a 'read only' contact for device list view, so user will have to click in to device to change Arm/Disarm
* 2017/09/08 Added 'statusText' so user will be able to see at a glance the timestamp of the last activity (armed, disarmed, alarm)
*/
// Added Preferences
preferences {
    input name: "disarmedValue", type: "number", title: "Disarmed Value", description: "Value When Disarmed",defaultValue: "200", range: "1..500", displayDuringSetup: false
    input name: "armedValue", type: "number", title: "Armed Value", description: "Value When Armed",defaultValue: "150", range: "1..500", displayDuringSetup: false
    input name: "sirenValue", type: "number", title: "Siren Value", description: "Value When Siren is Active",defaultValue: "100", range: "1..500", displayDuringSetup: false
    input name: "smokeValue", type: "number", title: "Smoke Value", description: "Value When Smoke in Alarm",defaultValue: "50", range: "1..500", displayDuringSetup: false
    input name: "marginValue", type: "number", title: "Margin of Error", description: "+/- Margin 0f Error", defaultValue: "5", range: "1..50", displayDuringSetup: false
}

metadata {
    definition (name: "MIMOlite Alarm Control and Monitor", namespace: "mignlou", author: "mignlou") {
    capability "Polling"
    capability "Refresh"
    capability "Momentary"
    capability "Configuration"
    capability "Contact Sensor"	
    capability "Smoke Detector"	
    capability "Motion Sensor"	
    attribute "powerSupply", "string"
    attribute "inputValue", "number"
    attribute "armedValue", "number"
    attribute "sirenValue", "number"
    attribute "disarmedValue", "number"
    attribute "sensorValue", "number"
    command "setPosition"

    fingerprint deviceId:"0x1000", inClusters:"0x72, 0x86, 0x71, 0x30, 0x31, 0x35, 0x70, 0x85, 0x25"
}

// UI tile definitions
tiles(scale:2) {
    multiAttributeTile(name:"contact", type: "generic", width: 6, height: 4){
        tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
            attributeState "open", label: "Disarmed", action: "push", icon: "st.Home.home2", backgroundColor: "#ffffff", nextState:"closing"
            attributeState "opening", label: "Disarming", action: "push", icon: "st.Home.home3", backgroundColor: "#ffffff", nextState:"closing"
            attributeState "closed", label: "Armed", action: "push", icon: "st.Home.home3", backgroundColor: "#B82121", nextState:"opening"
            attributeState "closing", label: "Arming", action: "push", icon: "st.Home.home2", backgroundColor: "#ffffff", nextState:"opening"
            attributeState "Alarm", label: "ALARM", action: "push", icon: "st.alarm.alarm.alarm", backgroundColor: "#bc2323", nextState:"opening"
            attributeState "Smoke", label: "SMOKE", action: "push", icon: "st.alarm.smoke.smoke", backgroundColor: "#bc2323", nextState:"opening"
            attributeState "unknown", label: "Unknown", action: "push", icon: "st.alarm.alarm.alarm", backgroundColor: "#ffffff", nextState:"opening"
        }
        tileAttribute ("powerSupply", key: "SECONDARY_CONTROL") {
            attributeState "good", label: "Power Good"
            attributeState "powerOutOpen", label: "Power Out - Disarmed"
            attributeState "powerOutClosed", label: "Power Out - Armed"
        }
    }
    
    standardTile("contact_read", "device.contact", width: 3, height: 2, inactiveLabel: false) {
        state "open", label: "DISARMED", icon: "st.Home.home2", backgroundColor: "#ffffff"
		state "closed", label: "ARMED", icon: "st.Home.home3", backgroundColor: "#B82121"
    }
    
    standardTile("blankTile", "statusText", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
		state "default", label:'', icon:"http://cdn.device-icons.smartthings.com/secondary/device-activity-tile@2x.png"
	}
    valueTile("statusText", "statusText", inactiveLabel: false, decoration: "flat", width: 5, height: 1) {
		state "statusText", label:'${currentValue}', backgroundColor:"#ffffff"
	}

	standardTile ("refresh", "refresh", decoration: "flat", width: 2, height: 2) {
        state "refresh", label: '', action: 'refresh', icon: "st.secondary.refresh"
    }

    valueTile ("inputValue", "device.inputValue", width: 4, height: 2) {
        state "inputValue", label: 'Current Input Value of MIMOlite  (${currentValue})'
    }

    main ("contact_read")
    details("contact", "refresh", "inputValue", "blankTile", "statusText")
}
}

def parse(String description) {
    def result = null
    // supported classes
    // 0x20 - BasicSet used to report when the sensor trigger level changes
    // 0x25 - switch binary V1
    // 0x30 - sensor binary V1
    // 0x31 - sensor multilevel V1
    // 0x35 - meter pulse (not tested)
    // 0x70 - configuration V1
    // 0x71 - alarm V1 (for supply voltage monitor, does not seem to respond to AlarmGet)
    // 0x72 - manufacturer specific V1
    // 0x85 - association V1
    // 0x86 - version V1
    log.debug description
    def cmd = zwave.parse(description, [0x20: 1, 0x25: 1, 0x86: 1, 0x30: 1, 0x31: 1, 0x72: 1, 0x71: 1])
    if (cmd) {
        result = createEvent(zwaveEvent(cmd))
        if (result) {
            log.debug "Parsed command: ${result?.descriptionText} raw: ${description}"
        } else {
         MC   log.debug "Unhandled command: ${description}"
        }
    } else {
        log.debug "Unparsed command: ${description}"
    }
    return result
}

def getSensitivity() {
	return 0x1
}

def getPotOpen() {
	def rc = device.currentValue("doorPositionOpen")
    if (rc == null) {
	    sendEvent(name: "doorPositionOpen", value: 168)
    	return 168
    }
    return rc
}

def getPotClosed() {
    // def rc = device.currentValue("doorPositionClosed")
    def rc = device.currentValue ("doorPositionClosed")
    if (rc == null) {
        sendEvent(name: "doorPositionClosed", value: 0)
        return 0
    }
    return rc
}

def getSiren() {
    // def rc = device.currentValue("sirenPositionAlarm")
    def rc = device.currentValue ("sirenPositionAlarm")
    if (rc == null) {
        sendEvent(name: "sirenPositionAlarm", value: 200)
        return 200
    }
    return rc
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	// trigger fires, request the door position so we can see what state it is in
	poll().collect { sendHubCommand(new physicalgraph.device.HubAction(it)) }
	return null
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv1.AlarmReport cmd) {
	if (cmd.alarmLevel) {
		if (device.currentValue("contact") == "closed") {
			sendEvent(name: "powerSupply", value: "powerOutClosed")
		} else {
			sendEvent(name: "powerSupply", value: "powerOutOpen")
		}
		return [name: "contact", value: "unknown"]
	}
	return null
}

def convertSensorValueToDoorState( BigDecimal sensorValue ) {

	if (sensorValue <= smokeValue + marginValue && sensorValue >= smokeValue - marginValue){
		sendEvent (name: "contact", value: "Smoke")
        log.debug "SMOKE DETECTED!"
	}
	else if (sensorValue <= sirenValue + marginValue && sensorValue >= sirenValue - marginValue){
		sendEvent (name: "contact", value: "Alarm")
        log.debug "ALARM IS ACTIVE!"
	} 
	else if (sensorValue <= armedValue + marginValue && sensorValue >= armedValue - marginValue){
		sendEvent (name: "contact", value: "closed")
        log.debug "System Armed"
	} 
    else if (sensorValue <= disarmedValue + marginValue && sensorValue >= disarmedValue - marginValue){
		sendEvent (name: "contact", value: "open")
        log.debug "System Disarmed"
	} 
	else {
		sendEvent (name: "contact", value: "unknown")
	}
	def dstamp = new Date().format( 'yyyy-M-d hh:mm:ss', location.timeZone ) 
	sendEvent (name: "statusText", value: "Last Status Change:  ${dstamp}", displayed: false) 
	log.debug "MIMO Input Value ${sensorValue}"
	return device.currentValue('contact')
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv1.SensorMultilevelReport cmd) {
    // if we are in a power fail state this is the first message we'll get after the power comes back
    // so update the powerState
    if (device.currentValue('powerSupply') != "good") { 
        sendEvent(name: "powerSupply", value: "good")
    }

    def adjustedValue = cmd.scaledSensorValue.intValue() >> 4
    if (adjustedValue != device.currentValue('inputValue')) {
        def doorState = convertSensorValueToDoorState( adjustedValue )
        sendEvent(name: "inputValue", value: adjustedValue)
        if (device.currentValue('contact') != doorState) {     
            return [name: "contact", value: doorState]
        }
    }
    return null
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
// Handles all Z-Wave commands we aren't interested in
return null
}

def setPosition() {
    sendEvent(name: "doorPositionOpen", value: settings.disarmedValue)
    sendEvent(name: "doorPositionClosed", value: settings.armedValue)
    sendEvent(name: "sirenPositionAlarm", value: settings.sirenValue)
    setTriggerLevels().collect { sendHubCommand(new physicalgraph.device.HubAction(it)) }
}

def open() {
    if (device.currentValue("contact") == "closed") { push() }
}

def close() {
	if (device.currentValue("contact") == "open") { push() }
}

def push() {
    def cmds = delayBetween([
    zwave.switchBinaryV1.switchBinarySet(switchValue: 0xff).format(),
    ],500)
    return cmds
}

def poll() {
    delayBetween([
    zwave.sensorMultilevelV1.sensorMultilevelGet().format(),
    ],500)
}

def refresh() {
    delayBetween([
    zwave.sensorMultilevelV1.sensorMultilevelGet().format(),
    ],500)
}

def updated() {
	// called when the device is updated
	configure()
}

def installed() {
    // called when the device is installed
    configure()
}

def setTriggerLevels() {
    def openVal = getPotOpen()
    def closedVal = getPotClosed()
    def sirenVal = getSiren()
    def slop = getSensitivity()

    // if the potentiometer is installed backwards reverse the parameters
    //if (openVal < closedVal) {
    //    def temp = closedVal
    //    closedVal = openVal
    //  openVal = temp
    //}

    def lowerValHigh = closedVal + (2 * slop)
    def lowerValLow = closedVal + slop
    def upperValHigh = openVal - slop
    def upperValLow = openVal - (2 * slop)
        //Added sireValHigh and Low
    // def sirenValHigh = openVal - slop
    // def sirenValLow = openVal - (2 * slop)

    delayBetween([
        // Lower Threshold, High (Default=0xBB)
        zwave.configurationV1.configurationSet(scaledConfigurationValue: lowerValHigh, parameterNumber: 4, size: 2).format(), 
        // Lower Threshold, Low (Default=0xAB)
        zwave.configurationV1.configurationSet(scaledConfigurationValue: lowerValLow, parameterNumber: 5, size: 2).format(), 
        // Upper Threshold, High (Default=0xFF)
        zwave.configurationV1.configurationSet(scaledConfigurationValue: upperValHigh, parameterNumber: 6, size: 2).format(),
        // Upper Threshold, Low (Default = 0xFE)
        zwave.configurationV1.configurationSet(scaledConfigurationValue: upperValLow, parameterNumber: 7, size: 2).format(), 
    ],500)
}

def configure() {
    log.debug "configure"
    def cmds = delayBetween([
    // enable analog alert thresholds
    zwave.configurationV1.configurationSet(scaledConfigurationValue: 0, parameterNumber: 8, size: 2).format(),
    // turn on momentary button press, this presses it for 1 second
    zwave.configurationV1.configurationSet(scaledConfigurationValue: 10, parameterNumber: 11, size: 2).format(),
    // tell device to send multivalue sensor updates every 10 seconds 
    zwave.configurationV1.configurationSet(scaledConfigurationValue: 1, parameterNumber: 9, size: 2).format(),
    // enable alarms to be sent to smartthings hub
    zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format(),
    // enable multivalue sensor updates to be sent to smartthings hub
    zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format(),
    ],500)
    cmds += setTriggerLevels()
    return cmds
}