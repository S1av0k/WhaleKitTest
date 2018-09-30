package org.home.whalekit;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Project WhaleKitTest
 * Created by s1av0k on 29.09.2018.
 */
public class DbServiceTest {

    private static final String TEST_PLAYER_ID = "1234567";

    private static DTOPlayer player;


    @BeforeClass
    public static void before() {
        DTOPlayer dtoPlayer = new DTOPlayer();
        dtoPlayer.setId(TEST_PLAYER_ID);
        player = DbService.getInstance().createPlayerIfNotExist(dtoPlayer);
    }

    @Test
    public void findPlayerById() {
        DTOPlayer dtoPlayer = DbService.getInstance().findPlayerById(TEST_PLAYER_ID);

        Assert.assertNotNull(dtoPlayer);
        Assert.assertEquals(player.getId(), dtoPlayer.getId());
    }

    @Test
    public void incrementLike() {
        DTOPlayer playerBefore = DbService.getInstance().findPlayerById(TEST_PLAYER_ID);
        Assert.assertNotNull(playerBefore);

        DTOPlayer player = new DTOPlayer();
        player.setId(TEST_PLAYER_ID);
        DbService.getInstance().incrementLike(player);

        DTOPlayer playerAfter = DbService.getInstance().findPlayerById(TEST_PLAYER_ID);

        Assert.assertNotNull(playerAfter);
        Assert.assertEquals(playerBefore.getLikes() + 1, playerAfter.getLikes());
    }

    @AfterClass
    public static void after() {
        DbService.getInstance().dropPlayerCollection();
    }
}
