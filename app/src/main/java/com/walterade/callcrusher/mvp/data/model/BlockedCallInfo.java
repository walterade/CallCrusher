package com.walterade.callcrusher.mvp.data.model;

import android.arch.persistence.room.ColumnInfo;

public class BlockedCallInfo extends BlockedCall {

    @ColumnInfo(name = "last_call_date")
    private long lastCallDate;

    @ColumnInfo(name = "block_count")
    private int blockCount;

    public long getLastCallDate() {
        return lastCallDate;
    }

    public void setLastCallDate(long lastCallDate) {
        this.lastCallDate = lastCallDate;
    }

    public int getBlockCount() {
        return blockCount;
    }

    public void setBlockCount(int blockCount) {
        this.blockCount = blockCount;
    }

}
