package operator.types;

import operator.recovery.DataKey;
import operator.recovery.Key;
import org.junit.Test;
import supervisor.Position;

import javax.xml.crypto.Data;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by higla on 23/05/2018.
 */
public class OperatorTypeTest {
    Position pos = new Position("127.0.0.1", 1234);
    private List<DataKey> setup() {
        Key num1 = new Key(this.pos, 1);
        DataKey firstKey = new DataKey("C", 2.0D, num1, new ArrayList());
        Key num2 = new Key(this.pos, 2);
        DataKey secondKey = new DataKey("C", 3.0D, num2, new ArrayList());
        new Key(this.pos, 3);
        DataKey thirdKey = new DataKey("D", 3.0D, num2, new ArrayList());
        new Key(this.pos, 3);
        List<DataKey> ownedDataKey = new ArrayList();
        ownedDataKey.add(firstKey);
        ownedDataKey.add(secondKey);
        ownedDataKey.add(thirdKey);
        return ownedDataKey;
    }
    @Test
    public void reorderMessages() throws Exception {
        List<DataKey> listToOrder = setup();
        List<DataKey> ordered = OperatorType.reorderMessages(listToOrder, "C");

        assertEquals(ordered.get(0).getAggregator().getSequenceNumber(), 1);
        assertEquals(ordered.get(1).getAggregator().getSequenceNumber(), 2);

    }

}