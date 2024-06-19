package org.acquire.models;

import org.acquire.constants.HotelLabel;

public class Share {
    private HotelLabel label;

    private int count;

    public Share(HotelLabel label, int count) {
        this.label = label;
        this.count = count;
    }

    public Share(Share otherShare) {
        this.label = otherShare.label;
        this.count = otherShare.count;
    }

    public HotelLabel getLabel() {
        return label;
    }

    public void setLabel(HotelLabel label) {
        this.label = label;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return this.label+":"+this.count;
    }
}
