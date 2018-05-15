package operator.recovery;

import org.junit.Test;
import supervisor.Position;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DataKeyTest {

    @Test
    public void otherHasSameSenderButOlderSequenceNumber() {
        Position position=new Position("pluto",2);
        Key source1=new Key(position,2);
        Key source2=new Key(position,3);
        DataKey old=new DataKey("1",0.0,source1,new ArrayList<>());
        DataKey recent=new DataKey("1",0.0,source2,new ArrayList<>());
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
        DataKey old=new DataKey("1",0.0,source1,new ArrayList<>());
        DataKey recent=new DataKey("1",0.0,source2,new ArrayList<>());
        assertFalse(recent.otherHasSameSenderButOlderSequenceNumber(old));
        assertFalse(old.otherHasSameSenderButOlderSequenceNumber(recent));
    }


    @Test
    public void hasSameSenderButOlderOrEqualSequenceNumber()
    {
        Position position1=new Position("pluto",2);
        Key source1=new Key(position1,2);
        Key source2=new Key(position1,3);
        DataKey recent=new DataKey("1",0.0,source2,new ArrayList<>());
        assertTrue(recent.hasOlderOrEqualSequenceNumberThanOther(source2));
        assertFalse(recent.hasOlderOrEqualSequenceNumberThanOther(source1));
    }
    
    @Test
    public void testOldestWithSameKey()
    {
    	Position position1=new Position("pluto",2);
        Position position2=new Position("plutone",3);
        Key source1=new Key(position1,2);
        Key source2=new Key(position2,3);
        DataKey old=new DataKey("key",0.0,source1,new ArrayList<>());
        DataKey recent=new DataKey("key",0.0,source2,new ArrayList<>());
        List<DataKey> all=new ArrayList<>();
        all.add(recent);
        all.add(old);
        assertEquals(old,recent.oldestInListWithSameOriginalKey(all));
    }
}