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
        proposal : 16;
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
register acceptor_id {
    width: 16;
    instance_count : 1;
}
register proposal_register {
    width : 16;
    instance_count : 16;
}
register vproposal_register {
    width : 16;
    instance_count : 16;
}
register val_register {
    width : 32;
    instance_count : 16;
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
action read_round() {
    register_read(local_metadata.proposal, proposal_register, paxos.inst);
}
table round_tbl {
    actions { read_round; }
    size : 1;
}
action handle_phase1a() {
    register_write(proposal_register, paxos.inst, paxos.proposal);
    register_read(paxos.vproposal, vproposal_register, paxos.inst);
    register_read(paxos.val, val_register, paxos.inst);
    modify_field(paxos.msgtype, 2);
    register_read(paxos.acpt, acceptor_id, 0);
    modify_field(udp.checksum, 0);
}
action handle_phase2a() {
    register_write(proposal_register, paxos.inst, paxos.proposal);
    register_write(vproposal_register, paxos.inst, paxos.proposal);
    register_write(val_register, paxos.inst, paxos.val);
    modify_field(paxos.msgtype, 4);
    modify_field(paxos.vproposal, paxos.proposal);
    register_read(paxos.acpt, acceptor_id, 0);
    modify_field(udp.checksum, 0);
}
table paxos_tbl {
    reads {
        paxos.msgtype : exact;
    }
    actions {
        handle_phase1a;
        handle_phase2a;
        _no_op;
    }
    size : 8;
}
table drop_tbl {
    actions {
        _drop;
    }
    size : 1;
}
control ingress {
    if (valid (ipv4)) {
        apply(fwd_tbl);
    }
    if (valid (paxos)) {
        apply(round_tbl);
        if (local_metadata.proposal <= paxos.proposal) {
            apply(paxos_tbl);
        } else {
            apply(drop_tbl);
        }
    }
}
