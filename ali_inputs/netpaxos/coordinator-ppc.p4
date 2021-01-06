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
        src : 32;
        dst: 32;
    }
}
header_type udp_t {
    fields {
        srcPort : 16;
        dstPort : 16;
        length_ : 16;
        checksum : 16;
    }
}
header_type paxos_t {
    fields {
        inst : 32;
        proposal : 16;
        vproposal : 16;
        acpt : 16;
        msgtype : 16;
        val : 32;
        fsh : 32;
        fsl : 32;
        feh : 32;
        fel : 32;
        csh : 32;
        csl : 32;
        ceh : 32;
        cel : 32;
        ash : 32;
        asl : 32;
        aeh : 32;
        ael : 32;
    }
}
header_type local_metadata_t {
    fields {
        inst : 32;
    }
}
header ethernet_t ethernet;
header ipv4_t ipv4;
header udp_t udp;
header paxos_t paxos;
metadata local_metadata_t local_metadata;
parser start {
    return parse_ethernet;
}
parser parse_ethernet {
    extract(ethernet);
    return select(latest.etherType) {
        0x0800 : parse_ipv4;
        default : ingress;
    }
}
parser parse_ipv4 {
    extract(ipv4);
    return select(latest.protocol) {
        0x11 : parse_udp;
        default : ingress;
    }
}
parser parse_udp {
    extract(udp);
    return select(udp.dstPort) {
        0x8888: parse_paxos;
        default: ingress;
    }
}
parser parse_paxos {
    extract(paxos);
    return ingress;
}
register inst_register {
    width : 32;
    instance_count : 64000;
}
action forward(port) {
    modify_field(standard_metadata.egress_spec, port);
}
table fwd_tbl {
    reads {
        standard_metadata.ingress_port : exact;
    }
    actions {
        forward;
        _drop;
    }
    size : 8;
}
action _no_op() {
}
action _drop() {
    drop();
}
action increase_sequence() {
    register_read(local_metadata.inst, inst_register, 0);
    modify_field(paxos.inst, local_metadata.inst);
    add_to_field(local_metadata.inst, 1);
    register_write(inst_register, 0, local_metadata.inst);
    modify_field(paxos.msgtype, 3);
    modify_field(udp.checksum, 0);
}
action reset_paxos() {
    modify_field(local_metadata.inst, 0);
    register_write(inst_register, 0, local_metadata.inst);
}
table paxos_tbl {
    reads {
        paxos.msgtype : exact;
    }
    actions {
        increase_sequence;
        reset_paxos;
        _no_op;
    }
    size : 8;
}
control ingress {
    if (valid (ipv4)) {
        apply(fwd_tbl);
    }
    if (valid (paxos)) {
        apply(paxos_tbl);
    }
}
