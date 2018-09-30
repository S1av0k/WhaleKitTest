package org.home.whalekit;

/**
 * Project WhaleKitTest
 * Created by s1av0k on 29.09.2018.
 */
public interface LikeService {

    /**
     * Лайкнуть игрока
     */
    void like(String playerId);

    /**
     * Количество лайков у игрока от других игроков
     */
    long getLikes(String playerId);
}