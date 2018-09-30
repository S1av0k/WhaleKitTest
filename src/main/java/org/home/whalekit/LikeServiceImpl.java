package org.home.whalekit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Project WhaleKitTest
 * Created by s1av0k on 29.09.2018.
 */
public class LikeServiceImpl implements LikeService {

    private static final int POOL_SIZE = 1;

    private ExecutorService executor;


    public LikeServiceImpl() {
        executor = Executors.newFixedThreadPool(POOL_SIZE);
    }

    @Override
    public void like(String playerId) {
        executor.submit(new PersistLikeTask(playerId));
    }

    @Override
    public long getLikes(String playerId) {
        DbService dbService = DbService.getInstance();
        DTOPlayer player = dbService.findPlayerById(playerId);

        long likes = 0;

        if (player != null) {
            likes = player.getLikes();
        }
        return likes;
    }
}

class PersistLikeTask implements Runnable {

    private final String playerId;

    PersistLikeTask(String playerId) {
        this.playerId = playerId;
    }

    @Override
    public void run() {
        // Отдаем на исполнение в DAO
        DTOPlayer dto = new DTOPlayer();
        dto.setId(playerId);

        DbService dbService = DbService.getInstance();
        dbService.incrementLike(dto);
    }
}