package bot.misc;

import bot.Bot;
import bot.Constants;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.packet.format.FormatUtils;
import org.jnetpcap.protocol.network.Ip4;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.Inflater;

import static bot.Constants.NETWORK_ADAPTER_NAME;

public class NetworkWatcher {
    private Bot bot;
    private StringBuilder errorBuffer;
    private Pcap pcap;
    private PcapPacketHandler<String> jPacketHandler;

    private boolean running;
    public int packetSequenceNr;

    public NetworkWatcher(Bot bot) {
        this.bot = bot;
        errorBuffer = new StringBuilder();

        initialize();
    }

    private void initialize() {
        // Find a network device.
        PcapIf device = getNetworkDevice();
        if (device == null) {
            System.out.println("Unable to find network device. Stopping.");
            return;
        }

        // Start capturing on the device.
        int snapToLength = 64 * 1024;       // capture all packets, no trucation
        int flags = Pcap.MODE_PROMISCUOUS; // capture all packets
        int timeout = 1 * 1000;           // 10 seconds in millis
        pcap = Pcap.openLive(device.getName(), snapToLength, flags, timeout, errorBuffer);

        if (pcap == null) {
            System.err.printf("Error while opening device for capture: " + errorBuffer.toString());
            return;
        }

        // Create a callback for any packets captured.
        jPacketHandler = new PcapPacketHandler<String>() {
            public void nextPacket(PcapPacket packet, String user) {
                packetReceived(packet);
            }
        };

        // Clean up temp directory if it exists
        File tempDirectory = new File(bot.workingDirectory + "\\temp\\");
        if (tempDirectory.isDirectory()) {
            File[] files = tempDirectory.listFiles();
            for (File file : files) {
                String filename = file.getName();

                if (filename.endsWith("input.txt") || filename.endsWith("output.txt")) {
                    file.delete();
                }
            }
        }
    }

    public void close() {
        running = false;
    }

    public void loop() {
        running = true;
        while (running) {
            pcap.loop(1, jPacketHandler, "SoccerStarsAI");
        }

        pcap.close();
    }

    // Packet received callback.
    private void packetReceived(PcapPacket packet) {
        // Get info about packet.
        Ip4 ip = new Ip4();
        if (!packet.hasHeader(ip)) {
            return;
        }
        //String sourceIP = FormatUtils.ip(ip.source());
        //String destIP = FormatUtils.ip(ip.destination());

        // Get packet's contents as a byte array.
        byte[] byteArray = packet.getByteArray(0, packet.size());

        // Drop too short packets.
        if (byteArray.length < 59) {
            return;
        }

        // Truncate packet headers.
        byteArray = Arrays.copyOfRange(byteArray, 54, byteArray.length);

        // Drop non-protobuf and (hopefully) non-game packets.
        if (byteArray[0] != 0 || byteArray[1] != 0 || byteArray[4] != 8) {
            return;
        }

        // Truncate prefix bytes.
        byteArray = Arrays.copyOfRange(byteArray, 4, byteArray.length);

        int compressionType = byteArray[1];
        byte[] resultByteArray = null;
        if (compressionType == 0) { // no compression, just decode the message
            int uncompressedPacketPacketId = byteArray[5];
            if (uncompressedPacketPacketId == Constants.PACKET_ID_GAME_ENDED) {
                bot.onGameEnded();
            }
            resultByteArray = decodeProtobuf(byteArray);
            //return; // uncompressed packets are not useful to us at the moment
        } else { // deal with compression
            // Extract the compressed part from the original undecoded byte array (starting from byte 5).
            byte[] compressedByteArray = Arrays.copyOfRange(byteArray, 5, byteArray.length);
            byte[] uncompressedByteArray = decompress_packet(compressedByteArray);
            if (uncompressedByteArray == null) {
                return; // failed to uncompress
            }
            resultByteArray = decodeProtobuf(uncompressedByteArray);
        }

        // Parse the decompressed message
        long packetTimestamp = packet.getCaptureHeader().timestampInMillis();
        parseMessage(resultByteArray, packetTimestamp);
    }

    private void parseMessage(byte[] byteArray, long packetTimestamp) {
        // Create a BufferedReader for the byte array to read it line by line
        List<String> lines = new ArrayList<String>();
        try {
            BufferedReader bfReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(byteArray)));
            String line = null;
            while ((line = bfReader.readLine()) != null) {
                lines.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (lines.size() < 3) {
            return; // the file was empty for some reason
        }

        // Parse the first line for the packet ID
        String firstLine = lines.get(0);
        firstLine = firstLine.replace("1: ", "");
        int packetId = Integer.parseInt(firstLine);

        // Only interested in table state updating events.
        if (packetId != Constants.PACKET_ID_GAME_STARTED && packetId != Constants.PACKET_ID_SHOT_OUTCOME) {
            return;
        }

        // Check if it is our turn
        String whoseTurnLine;
        if (packetId == Constants.PACKET_ID_GAME_STARTED) {
            whoseTurnLine = lines.get(2);
        } else {
            whoseTurnLine = lines.get(3);
        }

        if (!whoseTurnLine.contains(Constants.MY_PLAYER_ID)) {
            return; // not our turn
        }

        // Extract coordinates from packets
        double xCoordinates[] = new double[11];
        double yCoordinates[] = new double[11];
        int movableId = -1;

        System.out.println("parsing coordaintes from packet " + packetSequenceNr);

        for (String line : lines) {
            if (line.contains("1: 0x")) { // x coordinate found
                movableId++;
                xCoordinates[movableId] = parseDoubleFromHex(line.trim().replace("1: 0x", ""));
                System.out.println(line + "    " + movableId + " found x coord: " + xCoordinates[movableId]);
            } else if (line.contains("2: 0x")) { // y coordinate found
                yCoordinates[movableId] = parseDoubleFromHex(line.trim().replace("2: 0x", ""));
                System.out.println(line + "    " + movableId + " found y coord: " + yCoordinates[movableId]);
            }
        }

        // Pass the information on.
        System.out.println("--- Table update (packet " + packetSequenceNr + "):");
        System.out.println(Arrays.toString(xCoordinates));
        System.out.println(Arrays.toString(yCoordinates));
        bot.onMyTurn(xCoordinates, yCoordinates, packetTimestamp);
    }

    // Decode a protobuf packet using protoc.exe
    private byte[] decodeProtobuf(byte[] byteArray) {
        packetSequenceNr++;
        String inputFile = packetSequenceNr + "input.txt";
        String outputFile = packetSequenceNr + "output.txt";
        // Export protobuf data to a file.
        try {
            FileOutputStream fos = new FileOutputStream(bot.workingDirectory + "\\temp\\" + inputFile);
            fos.write(byteArray);
            fos.close();
        } catch (Exception e) {
            System.out.println("Cannot export byte array to file.");
            e.printStackTrace();
        }

        // Launch protoc decoder on the file.
        try {
            String command = String.format("cmd /c protoc --decode_raw < \"temp\\%s\" > \"temp\\%s\"", inputFile, outputFile);
            Process p = Runtime.getRuntime().exec(command, null, new File(bot.workingDirectory));
            p.waitFor();
        } catch (Exception e) {
            System.out.println("Protoc decoder failed.");
            e.printStackTrace();
        }

        // Process the result file
        byte[] resultByteArray = null;
        try {
            resultByteArray = Files.readAllBytes(Paths.get(bot.workingDirectory + "\\temp\\", outputFile));

        } catch (Exception e) {
            System.out.println("Reading result failed.");
            e.printStackTrace();
        }

        return resultByteArray;
    }

    // Try to find a compressed part of a byte array and decompress it.
    private byte[] decompress_packet(byte[] ba) {
        // this loop could be eliminated with a known offset to where the compressed part of the packet starts
        while (ba.length > 1) {
            try {
                byte[] decompressed_bytes = decompress_bytes(ba);
                return decompressed_bytes;
            } catch (Exception e) {
                ba = Arrays.copyOfRange(ba, 1, ba.length);
            }
        }

        return null;
    }

    // Decompress a byte array.
    private byte[] decompress_bytes(byte[] ba) throws Exception {
        Inflater decompressor = new Inflater();
        decompressor.setInput(ba, 0, ba.length);
        byte[] result = new byte[1500];
        int resultLength = decompressor.inflate(result);
        result = Arrays.copyOfRange(result, 0, resultLength);
        decompressor.end();

        return result;
    }

    // Find a network device to listen game packets on.
    private PcapIf getNetworkDevice() {
        List<PcapIf> allDevices = new ArrayList<PcapIf>(); // Will be filled with NICs

        // Get a list of network devices on this system.
        int r = Pcap.findAllDevs(allDevices, errorBuffer);
        if (r == Pcap.NOT_OK || allDevices.isEmpty()) {
            System.err.printf("Can't read list of devices, error is %s", errorBuffer.toString());
            return null;
        }

        // Print a list of devices for information purposes.
        System.out.println("Network devices found:");
        int chosenAdapter = 0;
        int i = 0;
        for (PcapIf device : allDevices) {
            String description = (device.getDescription() != null) ? device.getDescription() : "No description available";
            System.out.printf("#%d: %s [%s]\n", i++, device.getName(), description);

            if (description.equals(NETWORK_ADAPTER_NAME)) {
                chosenAdapter = i - 1;
            }
        }

        PcapIf device = allDevices.get(chosenAdapter);
        System.out.printf("\nChoosing '%s'.\n", (device.getDescription() != null) ? device.getDescription() : device.getName());

        return device;
    }

    // Convert a hex string outputted by protoc to a double.
    public static double parseDoubleFromHex(String text) {
        long longHex = parseUnsignedHex(text);
        return Double.longBitsToDouble(longHex);
    }

    private static long parseUnsignedHex(String text) {
        if (text.length() == 16) {
            return (parseUnsignedHex(text.substring(0, 1)) << 60) | parseUnsignedHex(text.substring(1));
        }
        return Long.parseLong(text, 16);
    }
}
