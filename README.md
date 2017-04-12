# Dependancies / requirements
* jNetPcap (for capturing game packets)
* SikuliX (for interacting with the game environment)
* protoc.exe from Google's protobuf (for decoding packets)

# How to use
You should define your player ID in Constants.java, which you can find using a packet capture tool like WireShark while starting the flash game. Note that this program was built to work at the 125% Windows DPI setting, therefore to make it work with other DPIs you might have to replace the screenshots in the resource folder and also change some hardcoded pixel values in Constants.java.

The program should be started by manually entering a match in the game and then starting the program. It should rematch / join a new match automatically after that.

# Known bugs
* Sometimes the y coordinates on the game field are inverted, causing this program to not function correctly
* The flash applet becomes unstable after extended use, finally crashing
