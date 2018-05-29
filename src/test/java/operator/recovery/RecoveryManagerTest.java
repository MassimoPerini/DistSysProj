package operator.recovery;

import org.junit.Test;
import supervisor.Position;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by higla on 23/05/2018.
 */
public class RecoveryManagerTest {
    Position pos = new Position("127.0.0.1", 11340);

    private List<DataKey> setup(){
        Key num1 = new Key(pos, 1);
        DataKey firstKey = new DataKey("C", 2.0, num1, new ArrayList<>());
        Key num2 = new Key(pos, 2);
        DataKey secondKey = new DataKey("C", 3.0, num2, new ArrayList<>());
        Key num3 = new Key(pos, 3);
        DataKey thirdKey = new DataKey("D", 3.0, num2, new ArrayList<>());
        Key ack = new Key(pos, 3);
        List<DataKey> ownedDataKey = new ArrayList<>();
        ownedDataKey.add(firstKey);
        ownedDataKey.add(secondKey);
        ownedDataKey.add(thirdKey);
        return ownedDataKey;
    }
    @Test
    public void filterAck() throws Exception {
        //String originalKey = "C";
        List<DataKey> testList= setup();
        Key ack = new Key(pos, 3);


        RecoveryManager.filterAck("C", ack, pos, testList);

        assertEquals(0,testList.get(2).getSources().size());
        assertEquals(1,testList.get(1).getSources().size());
        assertEquals(1,testList.get(0).getSources().size());
    }
    @Test
    public void filterSAck() throws Exception {
        //String originalKey = "C";
        List<DataKey> testList= setup();

        Key ack = new Key(pos, 3);
        testList.get(0).addSource(pos);

        RecoveryManager.filterAck("E", ack, pos, testList);

        assertEquals(0,testList.get(2).getSources().size());
        assertEquals(0,testList.get(1).getSources().size());
        assertEquals(1,testList.get(0).getSources().size());
    }

}