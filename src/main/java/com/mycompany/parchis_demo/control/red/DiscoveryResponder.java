package com.mycompany.parchis_demo.control.red;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DiscoveryResponder implements Runnable {

    private final int tcpPort;         // Puerto del ServerSocket del broker (p.ej. 5000)
    private final int discoveryPort;   // Puerto UDP para descubrimiento (p.ej. 5001)
    private volatile boolean running = true;

    public DiscoveryResponder(int tcpPort, int discoveryPort) {
        this.tcpPort = tcpPort;
        this.discoveryPort = discoveryPort;
    }

    public void stop() { running = false; }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(discoveryPort)) {
            socket.setReuseAddress(true);
            byte[] buf = new byte[256];

            while (running) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                String msg = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                if (!"PARCHIS_DISCOVER".equals(msg)) continue;

                // Obtén la IP local “saliente” hacia el cliente que preguntó
                InetAddress local = getLocalAddressFor(packet.getAddress());
                String reply = "PARCHIS_OK:" + local.getHostAddress() + ":" + tcpPort;

                byte[] out = reply.getBytes(StandardCharsets.UTF_8);
                DatagramPacket resp = new DatagramPacket(out, out.length, packet.getAddress(), packet.getPort());
                socket.send(resp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Intenta seleccionar la IP de la interfaz correcta
    private static InetAddress getLocalAddressFor(InetAddress remote) {
        try {
            // Abre un socket “virtual” hacia el remoto para conocer la IP local usada en esa ruta
            try (DatagramSocket s = new DatagramSocket()) {
                s.connect(remote, 9);
                return s.getLocalAddress();
            }
        } catch (Exception ignored) { }
        // Fallback: loopback
        return InetAddress.getLoopbackAddress();
    }
}
