package test;

import com.systems.ko.DocumentConverter;
import junit.framework.TestCase;

/**
 * Created by wk on 7/28/14 AD.
 */

public class TestConverter extends TestCase {

    public void testConvert() {
        boolean rs = DocumentConverter.convert();
        //boolean rs = true;
        assertEquals(true, rs);
    }

    public void testPass() {

        assertEquals(true, true);
    }

    public void testFailed() {
        assertEquals(true, true);
    }
}
