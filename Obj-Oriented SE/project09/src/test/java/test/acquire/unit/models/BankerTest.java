package test.acquire.unit.models;

import org.acquire.constants.HotelLabel;
import org.acquire.models.Banker;
import org.acquire.models.Tile;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;

public class BankerTest {
    private Banker banker;

    @Before
    public void setUp(){
        banker = Banker.getBanker();
    }

    @After
    public void destroy(){
        banker = null;
        Banker.destroyBanker();
    }

    @Test
    public void testRandomTileSelection(){
        Assert.assertEquals(Tile.class, banker.getRandomTile().getClass());
        for(int i=0;i<53;i++){
            Assert.assertNotEquals(banker.getRandomTile(), banker.getRandomTile());
        }
        banker.getRandomTile();
        Assert.assertNull(banker.getRandomTile());
    }

    @Test(expected = RuntimeException.class)
    public void testSharePriceCalculator(){

        Assert.assertEquals(banker.getCertificatePrice(HotelLabel.American,13),800);
        Assert.assertEquals(banker.getCertificatePrice(HotelLabel.Sackson,8),600);
        banker.getCertificatePrice(HotelLabel.Continental,0);
        Assert.assertEquals(banker.getCertificatePrice(HotelLabel.Tower,50),1200);
        Assert.assertEquals(banker.getCertificatePrice(HotelLabel.Festival,41),1100);

        Assert.assertEquals(banker.getCertificatePrice(HotelLabel.Imperial,2 ),300);
        Assert.assertEquals(banker.getCertificatePrice(HotelLabel.Worldwide,13),700);
    }
}
