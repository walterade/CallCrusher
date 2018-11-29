package com.walterade.callcrusher.event;

public class PhoneRingingEvent {
    public final long incomingCallId;

    public PhoneRingingEvent(long incomingCallId) {
        this.incomingCallId = incomingCallId;
    }
}
