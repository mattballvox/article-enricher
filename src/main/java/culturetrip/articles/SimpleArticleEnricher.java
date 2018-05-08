package culturetrip.articles;

import culturetrip.articles.clients.ArticleRepositoryClient;
import culturetrip.articles.clients.AssetsServiceClient;
import culturetrip.articles.models.ArticleReference;
import culturetrip.articles.models.Image;
import culturetrip.articles.models.RichArticle;
import culturetrip.articles.models.Video;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * A simple implementation of the ArticleEnricher.
 * 
 * @author mball
 */
public class SimpleArticleEnricher implements ArticleEnricher {

    private long timeoutSeconds;
    private AssetsServiceClient assetsServiceClient;
    private ArticleRepositoryClient articleRepositoryClient;

    public void setTimeoutSeconds(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public void setAssetsServiceClient(AssetsServiceClient assetsServiceClient) {
        this.assetsServiceClient = assetsServiceClient;
    }

    public void setArticleRepositoryClient(ArticleRepositoryClient articleRepositoryClient) {
        this.articleRepositoryClient = articleRepositoryClient;
    }

    @Override
    public Future<RichArticle> enrichArticleWithId(String articleId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                //fetch the ArticleReference to lookup.
                ArticleReference articleReference = getArticleReference(articleId);                

                //return a new RichArticle with all the information from the ArticleReference.
                return new RichArticle(articleId,
                        articleReference.getName(), 
                        getImage(articleReference.getHeroImageUrl()),
                        getVideos(articleReference));

            } catch (NullPointerException | InterruptedException | ExecutionException | TimeoutException ex) {
                /**
                 * Something went wrong working with one of the Futures returned from the client, so throw a CompleteExecution. 
                 * This will be wrapped in an ExecutionException, but that is just Java. The root cause can be extracted.
                 * using getCause() on the resulting exception.
                 */                
                throw new CompletionException(ex);
            }
        });
    }
    /**
     * Gets the ArticleReference given an articleId. It will call get()
     * on the Future, so we have to ensure the correct Exceptions are thrown.
     * These are dealt with in the calling method.
     * 
     * @param articleId
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException 
     */
    private ArticleReference getArticleReference(String articleId) throws InterruptedException, ExecutionException, TimeoutException {
        //get the Future from the article client.
        Future<ArticleReference> articleReferenceFuture = articleRepositoryClient.getArticleReferenceForId(articleId);
        //block to get the ArticleReference for a given amount of time then throw TimeoutException
        return articleReferenceFuture.get(timeoutSeconds, TimeUnit.SECONDS);        
    }

    /**
     * Gets the Image for a given String URL. It calls get(duration, unit) on the Future, so we have
     * to ensure the correct Exceptions are thrown. These are dealt with in the 
     * calling method.
     * 
     * @param imageURL
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException 
     */
    private Image getImage(String imageURL) throws InterruptedException, ExecutionException, TimeoutException {
        //get the Future from the asset service.
        Future<Image> imageFuture = assetsServiceClient.getImageById(imageURL);

        //block to get the Image for a given amount of time then throw TimeoutException
        return imageFuture.get(timeoutSeconds, TimeUnit.SECONDS);
    }    

    /**
     * TODO: We could create our own Function that calls the Future.get(duration, unit) and handle the Exception. Then we could use .map() in the Stream.
     * I've kept it simple for now and used a for-each instead.
     * 
     * Gets the list of videos from a given ArticleReference. It has to collect all the Futures first, then
     * it loops through and calls the get() on each to return the underlying Video Object. We have
     * to ensure the correct Exceptions are thrown. These are dealt with in the 
     * calling method.
     * 
     * 
     * @param articleReference
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException 
     */
    private List<Video> getVideos(ArticleReference articleReference) throws InterruptedException, ExecutionException, TimeoutException {
        List<Video> videos = new ArrayList<>();

        for (Future<Video> videoFuture : getVideoFutures(articleReference)) {
            //block to get the Video for a given amount of time then throw TimeoutException.
            Video video = videoFuture.get(timeoutSeconds, TimeUnit.SECONDS);
            if (video != null) {
                videos.add(video);
            }
        }
        return videos;
    }
    
    /**
     * Get a Collection of Future<Video> objects from each video URL in the 
     * ArticleReference. 
     * 
     * @param articleReference
     * @return 
     */
    private Collection<Future<Video>> getVideoFutures(ArticleReference articleReference) {
         return articleReference.getVideoUrls().stream()
                        .map(v -> assetsServiceClient.getVideoById(v))
                        .collect(Collectors.toSet());
    }

}
