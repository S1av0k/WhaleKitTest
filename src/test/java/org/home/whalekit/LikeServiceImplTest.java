package org.home.whalekit;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Random;

/**
 * Project WhaleKitTest
 * Created by s1av0k on 29.09.2018.
 */
public class LikeServiceImplTest {

    private static final String TEST_PLAYER_ID = "12345";

    private static LikeService likeService;
    private static DTOPlayer player;


    @BeforeClass
    public static void before() {
        likeService = new LikeServiceImpl();
        DbService dbService = DbService.getInstance();
        dbService.dropPlayerCollection();

        DTOPlayer dtoPlayer = new DTOPlayer();
        dtoPlayer.setId(TEST_PLAYER_ID);
        player = dbService.createPlayerIfNotExist(dtoPlayer);
    }

    @Test
    public void like_multiThreaded() throws InterruptedException {
        // Допустим, что сервис может вызваться из нескольких потоков
        int count = 5 + new Random().nextInt(6);

        System.out.println(String.format("Запускаем %s потоков лайкинга игрока id=%s...", count, player.getId()));

        for (int i = 0; i < count; ++i) {
            Thread thread = new Thread(new CallLikeService(likeService, player.getId()));
            thread.setName(String.format("Thread_%s_playerId_%s", i, player.getId()));
            thread.start();
        }

        // Некрасиво, но что поделать. Необходимо дождаться окончания работы всех потоков
        // ExecutorService в LikeServiceImpl. Join проблему не решит.
        // Потоки в любом случае отработают все, если не закончится ОП
        Thread.sleep(5000);

        // После выполнения всех потоков у игрока должно быть count лайков
        DTOPlayer dtoPlayer = DbService.getInstance().findPlayerById(player.getId());

        Assert.assertNotNull(dtoPlayer);
        Assert.assertEquals(count, dtoPlayer.getLikes());
    }

    @Test
    public void getLikes() throws InterruptedException {
        // Тут неожиданностей нет, сколько сохранено в БД, столько и прочитаем
        DTOPlayer playerBefore = DbService.getInstance().findPlayerById(player.getId());
        Assert.assertNotNull(playerBefore);

        int count = 5 + new Random().nextInt(6);

        System.out.println(String.format("Ставим %s лайков игроку id=%s...", count, player.getId()));

        for (int i = 0; i < count; ++i) {
            likeService.like(player.getId());
        }

        // Опять же, ждем для того, чтобы отработали потоки ExecutorService в LikeServiceImpl
        Thread.sleep(5000);

        DTOPlayer playerAfter = DbService.getInstance().findPlayerById(player.getId());

        Assert.assertNotNull(playerAfter);
        Assert.assertEquals(playerBefore.getLikes() + count, playerAfter.getLikes());
    }

    @AfterClass
    public static void after() {
        DbService.getInstance().dropPlayerCollection();
    }
}

class CallLikeService implements Runnable {

    private final LikeService likeService;
    private final String playerId;

    CallLikeService(final LikeService likeService, final String playerId) {
        this.likeService = likeService;
        this.playerId = playerId;
    }

    @Override
    public void run() {
        likeService.like(playerId);
    }
}
