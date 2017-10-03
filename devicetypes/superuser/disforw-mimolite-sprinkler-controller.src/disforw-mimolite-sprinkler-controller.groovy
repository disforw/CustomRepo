/**
 *  FortrezZ MIMOlite 
 *
 *
 * 
 */
metadata {
	// Automatically generated. Make future change here.
	definition (name: "disforw - MIMOlite Sprinkler Controller", namespace: "", author: "disforw") {
		capability "Configuration"
		capability "Switch"
		capability "Refresh"
		capability "Contact Sensor"
        capability "Voltage Measurement"

		attribute "powered", "string"
		command "on"
		command "off"
        
        fingerprint deviceId: "0x1000", inClusters: "0x72,0x86,0x71,0x30,0x31,0x35,0x70,0x85,0x25,0x03"
	}
    
    preferences {
       input "RelaySwitchDelay", "decimal", title: "Momentary Relay1 output enable/disable. 0 = disable (Default)", description: "Numbers 0 to 3.0 allowed.", defaultValue: 0, required: false, displayDuringSetup: true
       input "PeriodicVoltReport", "decimal", title: "Periodic send interval of Multilevel Sensor Reports in seconds. 0 to 255 allowed. 0 = Disable", description: "Numbers 0 to 255 allowed.", defaultValue: 0, required: false, displayDuringSetup: true
    }


	// UI tile definitions 
	tiles (scale: 2) {
        multiAttributeTile(name:"contact", type: "generic", width: 6, height: 4, canChangeIcon: true, decoration: "flat"){
			tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
			attributeState "closed", label: "FLOWING", icon: "st.valves.water.open", backgroundColor: "#53a7c0"
			attributeState "open", label: "INACTIVE", icon: "st.valves.water.closed"
            }
		}
        multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true, decoration: "flat"){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
            attributeState "on", label: "DISABLED", action: "switch.off", backgroundColor: "#ffa81e", icon: "st.Outdoor.outdoor12"
			attributeState "off", label: 'ENABLED', action: "switch.on", backgroundColor: "#79b821", icon: "st.Outdoor.outdoor12"
            }
        }
        standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'Refresh', action:"refresh.refresh", icon:"st.secondary.refresh-icon"
		}
        standardTile("powered", "device.powered", width: 2, height: 2, inactiveLabel: false) {
			state "powerOn", label:'Power ON', icon: "https://dl.dropboxusercontent.com/u/19576368/SmartThings/power.png"
			state "powerOff", label:'Power OFF', icon: "https://dl.dropboxusercontent.com/u/19576368/SmartThings/power.png", backgroundColor: "#B82121"
		}
		standardTile("configure", "device.configure", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "configure", label:'Configure', action:"configuration.configure", icon:"st.secondary.tools"
		}
        valueTile("voltage", "device.voltage", width: 2, height: 2) {
            state "val", label:'${currentValue}v', unit:"", defaultState: true
        }
        valueTile("voltageCounts", "device.voltageCounts", width: 2, height: 2) {
            state "val", label:'${currentValue}', unit:"", defaultState: true
        }
        standardTile("blankTile", "statusText", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "default", label:'', icon:"http://cdn.device-icons.smartthings.com/secondary/device-activity-tile@2x.png"
		}
        valueTile("aboutTxt", "device.about", inactiveLabel: false, decoration: "flat", width: 5, height:1) {
            state "default", label:'${currentValue}'
		}
		main (["contact"])
		details(["contact","switch", "powered", "refresh","configure"])
	}
}

def refresh() {
	delayBetween([
        zwave.switchBinaryV1.switchBinaryGet().format(), // Status of the Relay1
        zwave.sensorMultilevelV5.sensorMultilevelGet().format(), // Status of the Sensor Voltage (requests a report of the anologue input voltage)
        zwave.sensorBinaryV1.sensorBinaryGet().format(), // Status of the Sensor1
		//wave.basicV1.basicGet().format(),
		//ave.alarmV1.alarmGet().format() 
    ],800)
}

def parse(String description) {
	log.warn "Device Responded: ${description}"

	def result = null
	def cmd = zwave.parse(description, [0x20: 1, 0x84: 1, 0x30: 1, 0x70: 1, 0x31: 5])
    
	if (cmd.CMD == "7105") { sendEvent(name: "powered", value: "powerOff", descriptionText: "$device.displayName lost power") }  // POWER LOSS
    else { sendEvent(name: "powered", value: "powerOn", descriptionText: "$device.displayName regained power") }  // POWER ON
    
    if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
    log.debug "Parsed ${cmd} to ${result.inspect()}"
	return result
}

def updated() {
	log.debug "Settings Updated..."
    configure()
}

def configure() {
	//def x = (RelaySwitchDelay*10).tointeger()
    def y = PeriodicVoltReport.toInteger()
    log.debug "Configuring.... " //setting up to monitor power alarm and actuator duration
    
	delayBetween([
		zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format(), // If a power dropout occurs, the MIMOlite will send an Alarm Command Class report
        //zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format(), // The MIMOlite will periodically send a MultiLevel Sensor report indicating the input’s voltage level
        zwave.associationV1.associationSet(groupingIdentifier:4, nodeId:[zwaveHubNodeId]).format(), // When the input is triggered or untriggered, the MIMOlite will automatically send a Binary Sensor report
        zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 11, size: 1).format(), // configurationValue for parameterNumber means how many 100ms do you want the relay
        
        //zwave.configurationV1.configurationSet(configurationValue: [16], parameterNumber: 4, size: 1).format(),
        zwave.configurationV1.configurationGet(parameterNumber: 4).format(),
        //zwave.configurationV1.configurationSet(configurationValue: [00], parameterNumber: 5, size: 1).format(),
        zwave.configurationV1.configurationGet(parameterNumber: 5).format(),
        //zwave.configurationV1.configurationSet(configurationValue: [3488], parameterNumber: 6, size: 1).format(),
        zwave.configurationV1.configurationGet(parameterNumber: 6).format(),
        //zwave.configurationV1.configurationSet(configurationValue: [3472], parameterNumber: 7, size: 1).format(),
        zwave.configurationV1.configurationGet(parameterNumber: 7).format(),
        
        zwave.configurationV1.configurationSet(configurationValue: [y], parameterNumber: 9, size: 1).format(), // Status reprt ever 42.5 minuts
        zwave.configurationV1.configurationGet(parameterNumber: 9).format() // gets the new parameter changes. not currently needed. (forces a null return value without a zwaveEvent funciton
	], 800)
}

// NOTES ABOUT ZWAVE EVENTS:
// These are special overloaded functions which MUST be returned with a map similar to (return [name: "switch", value: "on"])
// not doing so will produce a null on the parse function, this will mess you up in the future.
def sensorValueEvent(Short value) {
	if (value) {
		createEvent(name: "contact", value: "open", descriptionText: "$device.displayName is open" )	
    } else {
		createEvent(name: "contact", value: "closed", descriptionText: "$device.displayName is closed" )	
    }
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	[name: "switch", value: cmd.value ? "on" : "off", type: "physical"]
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	[name: "switch", value: cmd.value ? "on" : "off", type: "digital"]
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd) {
	sensorValueEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv1.AlarmReport cmd) {
	sensorValueEvent(cmd.sensorState)
}


def zwaveEvent (physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) // sensorMultilevelReport is used to report the value of the analog voltage for SIG1
{
	//log.debug "sent a SensorMultilevelReport"
	def ADCvalue = cmd.scaledSensorValue
    sendEvent(name: "voltageCounts", value: ADCvalue)
   
    CalculateVoltage(cmd.scaledSensorValue)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
     //log.debug("Un-parsed Z-Wave message ${cmd}")
	[:]
}

def CalculateVoltage(ADCvalue){
	 def map = [:]
     def volt = (((1.5338*(10**-16))*(ADCvalue**5)) - ((1.2630*(10**-12))*(ADCvalue**4)) + ((3.8111*(10**-9))*(ADCvalue**3)) - ((4.7739*(10**-6))*(ADCvalue**2)) + ((2.8558*(10**-3))*(ADCvalue)) - (2.2721*(10**-2)))

    //def volt = (((3.19*(10**-16))*(ADCvalue**5)) - ((2.18*(10**-12))*(ADCvalue**4)) + ((5.47*(10**-9))*(ADCvalue**3)) - ((5.68*(10**-6))*(ADCvalue**2)) + (0.0028*ADCvalue) - (0.0293))
	//log.debug "$cmd.precision $cmd.size $cmd.sensorType $cmd.sensorValue $cmd.scaledSensorValue"
	def voltResult = volt.round(1)// + "v"
    
	map.name = "voltage"
    map.value = voltResult
    map.unit = "v"
    return map
}

def on() {
    delayBetween([
        zwave.basicV1.basicSet(value: 0xFF).format(),
        zwave.switchBinaryV1.switchBinaryGet().format()
    ])
	//sendEvent (name: "aboutTxt", value: "The system is operating in normal mode: odd days at 5am") 
}


def off() {
    delayBetween([
        zwave.basicV1.basicSet(value: 0x00).format(),
        zwave.switchBinaryV1.switchBinaryGet().format()
    ])
    //def dstamp = new Date().format( 'yyyy-M-d',TimeZone.getTimeZone('EST') )
    //sendEvent (name: "aboutTxt", value: "The system was disabled on:  ${dstamp}")
}
