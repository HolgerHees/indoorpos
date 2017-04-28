package com.holgerhees.indoorpos.persistance.dto;

import com.holgerhees.shared.persistance.annotations.DbColumn;
import com.holgerhees.shared.persistance.annotations.DbForeignKey;
import com.holgerhees.shared.persistance.annotations.DbTable;
import com.holgerhees.shared.persistance.dto.AbstractBaseDTO;

@DbTable( name = "tracked_beacon" )
public class TrackedBeaconDTO extends AbstractBaseDTO
{
    @DbColumn( name = "tracker_id",
               type = "int(11)",
               updatable = false,
               foreignKey = { @DbForeignKey( target = TrackerDTO.class,
                                             field = "id",
                                             onUpdate = "CASCADE",
                                             onDelete = "CASCADE" ) } )
    private Long trackerId;

    @DbColumn( name = "beacon_id",
               type = "int(11)",
               updatable = false,
               foreignKey = { @DbForeignKey( target = BeaconDTO.class,
                                             field = "id",
                                             onUpdate = "CASCADE",
                                             onDelete = "CASCADE" ) } )
    private Long beaconId;

    @DbColumn( name = "tx_power",
               type = "tinyint(2)" )
    private int txPower;

    @DbColumn( name = "rssi",
               type = "tinyint(2)" )
    private int rssi;

    public Long getTrackerId()
    {
        return trackerId;
    }

    public void setTrackerId( Long trackerId )
    {
        this.trackerId = trackerId;
    }

    public Long getBeaconId()
    {
        return beaconId;
    }

    public void setBeaconId( Long beaconId )
    {
        this.beaconId = beaconId;
    }

    public int getTxPower()
    {
        return txPower;
    }

    public void setTxPower( int txPower )
    {
        this.txPower = txPower;
    }

    public int getRssi()
    {
        return rssi;
    }

    public void setRssi( int rssi )
    {
        this.rssi = rssi;
    }
}
