/**
 *  Alarm Panel 2.0
 *
 *  Copyright 2020 Ben Abrams
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
metadata {
	definition (name: "Alarm Panel 2.0", namespace: "disforw", author: "Ben Abrams", cstHandler: true) {
		capability "Contact Sensor"
		capability "Button"
		capability "Security System"

		fingerprint mfr: "0084", prod: "0453", model: "0110", deviceJoinName: "MIMOLite Switch Relay 1.0"
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		// TODO: define your main and details tiles here
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'contact' attribute
	// TODO: handle 'button' attribute
	// TODO: handle 'numberOfButtons' attribute
	// TODO: handle 'supportedButtonValues' attribute
	// TODO: handle 'securitySystemStatus' attribute
	// TODO: handle 'alarm' attribute

}

// handle commands
def armStay() {
	log.debug "Executing 'armStay'"
	// TODO: handle 'armStay' command
}

def armAway() {
	log.debug "Executing 'armAway'"
	// TODO: handle 'armAway' command
}

def disarm() {
	log.debug "Executing 'disarm'"
	// TODO: handle 'disarm' command
}