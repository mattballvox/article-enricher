package culturetrip.articles.clients;

import culturetrip.articles.models.Image;
import culturetrip.articles.models.Video;

import java.util.concurrent.Future;

public interface AssetsServiceClient {
    Future<Image> getImageById(String id);

    Future<Video> getVideoById(String id);
}
