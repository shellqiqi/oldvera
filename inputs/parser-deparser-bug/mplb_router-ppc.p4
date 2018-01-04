header_type ethernet_t {
    fields {
        dstAddr : 48;
        srcAddr : 48;
        etherType : 16;
    }
}
header_type ipv4_t {
    fields {
        version : 4;
        ihl : 4;
        diffserv : 8;
        totalLen : 16;
        identification : 16;
        flags : 3;
        fragOffset : 13;
        ttl : 8;
        protocol : 8;
        hdrChecksum : 16;
        srcAddr : 32;
        dstAddr: 32;
    }
}
header_type tcp_t {
    fields {
        srcPort : 16;
        dstPort : 16;
        sequence : 32;
        ack : 32;
        len : 4;
        reserved : 6;
        flags : 6;
        window : 16;
        check : 16;
        urgent : 16;
    }
}
header_type mplb_t {
    fields {
        tip : 8;
        lengt : 8;
        pointer : 8;
        clientip : 32;
        virtualip : 32;
        blank : 8;
    }
}
header_type mplb_ipopt_t {
    fields {
        copied : 1;
        class : 2;
        number : 5;
        len : 8;
        padding : 16;
        pdip : 32;
        ts : 32;
    }
}
header_type hash_metadata_t {
    fields {
        mplb_hash : 32;
        mplb_hash_modulo : 32;
        recirculate_flag : 8;
    }
}
header_type routing_metadata_t {
    fields {
        nhop_ipv4 : 32;
    }
}
header ethernet_t ethernet;
header ipv4_t ipv4;
header ipv4_t inner_ipv4;
header tcp_t tcp;
header mplb_t mplb;
header mplb_ipopt_t mplb_ipopt;
parser start {
    return parse_ethernet;
}
parser parse_ethernet {
    extract(ethernet);
    return select(latest.etherType) {
        0x0800 : parse_ipv4;
        default: ingress;
    }
}
field_list recirc_FL {
        standard_metadata;
        hash_metadata;
}
parser parse_mplb {
    extract(mplb);
    return parse_tcp;
}
parser parse_mplb_ipopt {
    extract(mplb_ipopt);
    return parse_inner_ip;
}
parser parse_inner_ip {
    extract(inner_ipv4);
    return parse_tcp;
}
parser parse_tcp {
    extract(tcp);
    return ingress;
}
parser parse_ipv4 {
    extract(ipv4);
    return select(ipv4.protocol) {
        4: parse_mplb_ipopt;
        default: parse_tcp;
    }
}
action _drop() {
    drop();
}
metadata routing_metadata_t routing_metadata;
metadata hash_metadata_t hash_metadata;
action set_nhop(nhop_ipv4, port) {
}
action set_dst_mplb_port(dst)
{
    add_header(inner_ipv4);
}
action set_dst(dst, pdip, ts) {
    add_header(mplb_ipopt);
    add_header(inner_ipv4);
}
action calc_modulo() {
    modify_field(hash_metadata.mplb_hash_modulo, hash_metadata.mplb_hash);
}
action set_dmac(dmac) {
    modify_field(ethernet.dstAddr, dmac);
}
action _recirculate() {
    modify_field(standard_metadata.egress_port, standard_metadata.ingress_port);
    recirculate(recirc_FL);
}
action rewrite_mac(smac) {
    modify_field(ethernet.srcAddr, smac);
}
table mplb_port {
    reads {
        tcp.dstPort : exact;
    }
    actions {
        set_dst_mplb_port;
        _drop;
    }
    size: 65536;
}
table modulo {
    reads {
        hash_metadata.mplb_hash : exact;
    }
    actions {
        calc_modulo;
    }
    size : 10;
}
table mplb {
    reads {
        hash_metadata.mplb_hash_modulo : exact;
    }
    actions {
        set_dst;
        _drop;
    }
    size: 2000;
}
table ipv4_lpm {
    reads {
        ipv4.dstAddr : lpm;
    }
    actions {
        set_nhop;
        _drop;
    }
    size: 1024;
}
table recirc {
    reads {
        ipv4.srcAddr : exact;
    }
    actions {
        _recirculate;
    }
    size:2;
}
table forward {
    reads {
        routing_metadata.nhop_ipv4 : exact;
    }
    actions {
        set_dmac;
        _drop;
    }
    size: 512;
}
table send_frame {
    reads {
        standard_metadata.egress_port: exact;
    }
    actions {
        rewrite_mac;
        _drop;
    }
    size: 256;
}
control ingress {
    if (standard_metadata.instance_type == 0)
    {
        apply(recirc);
    }
    else
    {
        if(valid(ipv4) and ipv4.ttl > 0) {
            if (tcp.dstPort < 1024)
            {
                apply(modulo);
                apply(mplb);
            }
            else
            {
                apply(mplb_port);
            }
            apply(ipv4_lpm);
            apply(forward);
        }
    }
}
control egress {
    apply(send_frame);
}