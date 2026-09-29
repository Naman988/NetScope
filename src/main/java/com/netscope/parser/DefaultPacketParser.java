package com.netscope.parser;

import com.netscope.model.ParsedPacket;
import com.netscope.model.Protocol;
import com.netscope.model.RawPacket;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.IcmpV4CommonPacket;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.IpV6Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;
import org.pcap4j.packet.namednumber.IpNumber;
import org.pcap4j.packet.IllegalRawDataException;

/**
 * Default {@link PacketParser} implementation, built on Pcap4J's
 * own layered packet-parsing API.
 *
 * <p>This class re-parses a {@link RawPacket}'s raw bytes through
 * Pcap4J (via {@code EthernetPacket.newPacket}) rather than
 * hand-rolling byte-level parsing — Pcap4J already understands
 * Ethernet/IP/TCP/UDP framing, so this class's job is only to walk
 * the resulting layer chain and extract the fields NetScope cares
 * about.
 *
 * <p>Any protocol this class does not specially handle (ARP, IGMP,
 * etc.) resolves to {@link Protocol#UNKNOWN} with null address
 * fields, per the interface contract — it is not treated as an
 * error.
 *
 * <p>Stateless and thread-safe.
 */
public final class DefaultPacketParser implements PacketParser {

    @Override
    public ParsedPacket parse(RawPacket rawPacket) throws PacketParseException {
        if (rawPacket == null) {
            throw new IllegalArgumentException("rawPacket must not be null");
        }

        try {
            Packet packet = EthernetPacket.newPacket(
                    rawPacket.getRawData(), 0, rawPacket.getRawData().length);

            // Walk down to the IP layer, if present.
            Packet ipLayer = findPayload(packet, IpV4Packet.class);
            if (ipLayer == null) {
                ipLayer = findPayload(packet, IpV6Packet.class);
            }

            if (ipLayer == null) {
                // No IP layer at all (e.g. ARP) — well-formed, just not
                // something this parser extracts addresses from.
                return new ParsedPacket(rawPacket, Protocol.UNKNOWN, null, null, null, null);
            }

            String sourceIp;
            String destinationIp;
            Packet transportLayer;

            if (ipLayer instanceof IpV4Packet ipv4) {
                sourceIp = ipv4.getHeader().getSrcAddr().getHostAddress();
                destinationIp = ipv4.getHeader().getDstAddr().getHostAddress();
                transportLayer = ipv4.getPayload();
            } else {
                IpV6Packet ipv6 = (IpV6Packet) ipLayer;
                sourceIp = ipv6.getHeader().getSrcAddr().getHostAddress();
                destinationIp = ipv6.getHeader().getDstAddr().getHostAddress();
                transportLayer = ipv6.getPayload();
            }

            if (transportLayer instanceof TcpPacket tcp) {
                int srcPort = tcp.getHeader().getSrcPort().valueAsInt();
                int dstPort = tcp.getHeader().getDstPort().valueAsInt();
                return new ParsedPacket(rawPacket, Protocol.TCP, sourceIp, destinationIp, srcPort, dstPort);

            } else if (transportLayer instanceof UdpPacket udp) {
                int srcPort = udp.getHeader().getSrcPort().valueAsInt();
                int dstPort = udp.getHeader().getDstPort().valueAsInt();
                return new ParsedPacket(rawPacket, Protocol.UDP, sourceIp, destinationIp, srcPort, dstPort);

            } else if (transportLayer instanceof IcmpV4CommonPacket) {
                // ICMP has no ports.
                return new ParsedPacket(rawPacket, Protocol.ICMP, sourceIp, destinationIp, null, null);

            } else {
                // IP layer present but transport protocol not specially
                // handled (e.g. GRE, ESP) — still a valid, parsed packet.
                return new ParsedPacket(rawPacket, Protocol.UNKNOWN, sourceIp, destinationIp, null, null);
            }

        } catch (IllegalRawDataException e) {
            // Pcap4J throws this checked exception for genuinely malformed bytes.
            throw new PacketParseException(
                    "Failed to parse packet bytes for packet " + rawPacket.getPacketId(), e);
        }
    }

    /**
     * Walks the packet's payload chain looking for a layer of the given type.
     *
     * @return the matching layer cast to {@code T}, or null if not found
     */
    @SuppressWarnings("unchecked")
    private <T extends Packet> T findPayload(Packet packet, Class<T> targetType) {
        Packet current = packet;
        while (current != null) {
            if (targetType.isInstance(current)) {
                return (T) current;
            }
            current = current.getPayload();
        }
        return null;
    }
}