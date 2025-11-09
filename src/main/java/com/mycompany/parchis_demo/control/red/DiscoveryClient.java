package com.mycompany.parchis_demo.control.red;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

public class DiscoveryClient {

    public static class Result {
        public final String host;
        public final int port;
        public Result(String host, int port) { this.host = host; this.port = port; }
    }

    public static Result discover(int discoveryPort, int timeoutMs) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(timeoutMs);

            byte[] sendData = "PARCHIS_DISCOVER".getBytes(StandardCharsets.UTF_8);

            // 1) Broadcast general 255.255.255.255
            DatagramPacket broadAll = new DatagramPacket(sendData, sendData.length,
                    InetAddress.getByName("255.255.255.255"), discoveryPort);
            socket.send(broadAll);

            // 2) Broadcast por interfaz (algunas redes bloquean 255.255.255.255)
            Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
            while (ifs.hasMoreElements()) {
                NetworkInterface ni = ifs.nextElement();
                if (!ni.isUp() || ni.isLoopback()) continue;
                for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                    InetAddress bcast = ia.getBroadcast();
                    if (bcast == null) continue;
                    DatagramPacket p = new DatagramPacket(sendData, sendData.length, bcast, discoveryPort);
                    socket.send(p);
                }
            }

            // Espera una respuesta
            byte[] recvBuf = new byte[256];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            socket.receive(receivePacket);
            String msg = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);

            if (msg.startsWith("PARCHIS_OK:")) {
                String[] parts = msg.trim().split(":");
                String host = parts[1];
                int port = Integer.parseInt(parts[2]);
                return new Result(host, port);
            }
        } catch (Exception e) {
            // Ignora para permitir fallback manual
        }
        return null;
    }
}
