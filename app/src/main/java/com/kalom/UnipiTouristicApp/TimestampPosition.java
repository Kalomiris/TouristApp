package com.kalom.UnipiTouristicApp;

import java.io.Serializable;
import java.util.Date;

public class TimestampPosition implements Serializable {

    private Date time;
    private PositionModel positionModel;

    TimestampPosition(Date time, PositionModel positionModelForTimestamp) {
        this.time = time;
        this.positionModel = positionModelForTimestamp;

    }

    public TimestampPosition() {
    }

    public Date getTime() {
        return time;
    }

    public PositionModel getPositionModel() {
        return positionModel;
    }
}
