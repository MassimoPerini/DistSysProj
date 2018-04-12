package operator.recovery;

import javafx.geometry.Pos;
import org.junit.Test;
import supervisor.Position;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class DataKeyTest {

    @Test
    public void otherHasSameSenderButOlderSequenceNumber() {
        Position position=new Position("pluto",2);
        Key source1=new Key(position,2);
        Key source2=new Key(position,3);
        DataKey old=new DataKey(0.0,source1,new ArrayList<>());
        DataKey recent=new DataKey(0.0,source2,new ArrayList<>());
        assertTrue(recent.otherHasSameSenderButOlderSequenceNumber(old));
        assertFalse(old.otherHasSameSenderButOlderSequenceNumber(recent));
    }

    @Test
    public void differentSources()
    {
        Position position1=new Position("pluto",2);
        Position position2=new Position("plutone",3);
        Key source1=new Key(position1,2);
        Key source2=new Key(position2,3);
        DataKey old=new DataKey(0.0,source1,new ArrayList<>());
        DataKey recent=new DataKey(0.0,source2,new ArrayList<>());
        assertFalse(recent.otherHasSameSenderButOlderSequenceNumber(old));
        assertFalse(old.otherHasSameSenderButOlderSequenceNumber(recent));
    }


}