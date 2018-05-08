
package culturetrip.articles;

import culturetrip.articles.clients.ArticleRepositoryClient;
import culturetrip.articles.clients.AssetsServiceClient;
import culturetrip.articles.models.ArticleReference;
import culturetrip.articles.models.Image;
import culturetrip.articles.models.RichArticle;
import culturetrip.articles.models.Video;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static org.hamcrest.CoreMatchers.instanceOf;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;

/**
 *
 * @author mball
 */
public class SimpleArticleEnricherTest {

    private static final String ARTICLE_ID_1 = "articleId1";
    private static final String ARTICLE_ID_2 = "articleId2";
    private static final String ARTICLE_ID_3 = "articleId3";
    private static final String ARTICLE_NAME_1 = "articleName";
    private static final String ARTICLE_NAME_2 = "articleName";
    private static final String ARTICLE_NAME_3 = "articleName";
    private static final String IMAGE_URL = "someImageUrl";
    private static final String IMAGE_ALT_TEXT = "someAltText";
    private static final String VIDEO_URL_1 = "someVideoURL1";
    private static final String VIDEO_URL_2 = "someVideoURL2";
    private static final String VIDEO_URL_3 = "someVideoURL3";
    private static final String NON_EXISTENT_VIDEO_URL = "someNonExistentVideoURL";

    private static final Video VIDEO_1 = new Video(VIDEO_URL_1, "some Caption 1");
    private static final Video VIDEO_2 = new Video(VIDEO_URL_2, "some Caption 2");
    private static final Video VIDEO_3 = new Video(VIDEO_URL_3, "some Caption 3");

    private static final Image IMAGE = new Image(IMAGE_URL, IMAGE_ALT_TEXT);

    private static final List<String> VIDEO_URLS = Arrays.asList(VIDEO_URL_1, VIDEO_URL_2, VIDEO_URL_3);

    public SimpleArticleEnricherTest() {
    }    

    /**
     * Test of enrichArticleWithId method, of class SimpleArticleEnricher when
     * it has all the available resource information.We expect it to build a
     * complete RichArticle.
     *
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    @Test
    public void testEnrichArticleWithIdSuccess() throws InterruptedException, ExecutionException {
        System.out.println("testEnrichArticleWithIdSuccess");

        SimpleArticleEnricher instance = new SimpleArticleEnricher();
        instance.setTimeoutSeconds(3);

        AssetsServiceClient mockAssetsServiceClient = mock(AssetsServiceClient.class);
        ArticleRepositoryClient mockArticleRepositoryClient = mock(ArticleRepositoryClient.class);

        Mockito.when(mockAssetsServiceClient.getImageById(IMAGE_URL))
                .thenReturn(CompletableFuture.completedFuture(IMAGE));
        Mockito.when(mockAssetsServiceClient.getVideoById(VIDEO_URL_1))
                .thenReturn(CompletableFuture.completedFuture(VIDEO_1));
        Mockito.when(mockAssetsServiceClient.getVideoById(VIDEO_URL_2))
                .thenReturn(CompletableFuture.completedFuture(VIDEO_2));
        Mockito.when(mockAssetsServiceClient.getVideoById(VIDEO_URL_3))
                .thenReturn(CompletableFuture.completedFuture(VIDEO_3));

        Mockito.when(mockArticleRepositoryClient.getArticleReferenceForId(ARTICLE_ID_1))
                .thenReturn(CompletableFuture.completedFuture(
                        new ArticleReference(ARTICLE_ID_1, ARTICLE_NAME_1, IMAGE_URL, VIDEO_URLS)));

        instance.setArticleRepositoryClient(mockArticleRepositoryClient);
        instance.setAssetsServiceClient(mockAssetsServiceClient);

        Future<RichArticle> result = instance.enrichArticleWithId(ARTICLE_ID_1);

        RichArticle richArticle = result.get();

        assertEquals(ARTICLE_ID_1, richArticle.getId());
        assertEquals(ARTICLE_NAME_1, richArticle.getName());
        assertEquals(IMAGE_URL, richArticle.getHeroImage().getId());
        assertEquals(3, richArticle.getVideos().size());
        assertTrue(richArticle.getVideos().contains(VIDEO_1));
        assertTrue(richArticle.getVideos().contains(VIDEO_2));
        assertTrue(richArticle.getVideos().contains(VIDEO_3));
    }
    
    /**
     * Test of enrichArticleWithId method, of class SimpleArticleEnricher when
     * we make several requests for different articles. it has all the available 
     * resource information. We expect it to build a complete RichArticle for 
     * each articleId we pass to it.
     *
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    @Test
    public void testEnrichArticleWithIdSuccessMultiple() throws InterruptedException, ExecutionException {
        System.out.println("testEnrichArticleWithIdSuccessMultiple");

        SimpleArticleEnricher instance = new SimpleArticleEnricher();
        instance.setTimeoutSeconds(3);

        AssetsServiceClient mockAssetsServiceClient = mock(AssetsServiceClient.class);
        ArticleRepositoryClient mockArticleRepositoryClient = mock(ArticleRepositoryClient.class);

        Mockito.when(mockAssetsServiceClient.getImageById(IMAGE_URL))
                .thenReturn(CompletableFuture.completedFuture(IMAGE));
        Mockito.when(mockAssetsServiceClient.getVideoById(VIDEO_URL_1))
                .thenReturn(CompletableFuture.completedFuture(VIDEO_1));
        Mockito.when(mockAssetsServiceClient.getVideoById(VIDEO_URL_2))
                .thenReturn(CompletableFuture.completedFuture(VIDEO_2));
        Mockito.when(mockAssetsServiceClient.getVideoById(VIDEO_URL_3))
                .thenReturn(CompletableFuture.completedFuture(VIDEO_3));

        Mockito.when(mockArticleRepositoryClient.getArticleReferenceForId(ARTICLE_ID_1))
                .thenReturn(CompletableFuture.completedFuture(
                        new ArticleReference(ARTICLE_ID_1, ARTICLE_NAME_1, IMAGE_URL, Arrays.asList(VIDEO_URL_1))));
        
        Mockito.when(mockArticleRepositoryClient.getArticleReferenceForId(ARTICLE_ID_2))
                .thenReturn(CompletableFuture.completedFuture(
                        new ArticleReference(ARTICLE_ID_2, ARTICLE_NAME_2, IMAGE_URL, Arrays.asList(VIDEO_URL_1, VIDEO_URL_2))));
        
        Mockito.when(mockArticleRepositoryClient.getArticleReferenceForId(ARTICLE_ID_3))
                .thenReturn(CompletableFuture.completedFuture(
                        new ArticleReference(ARTICLE_ID_3, ARTICLE_NAME_3, IMAGE_URL, VIDEO_URLS)));

        instance.setArticleRepositoryClient(mockArticleRepositoryClient);
        instance.setAssetsServiceClient(mockAssetsServiceClient);

        Future<RichArticle> result_1 = instance.enrichArticleWithId(ARTICLE_ID_1);
        Future<RichArticle> result_2 = instance.enrichArticleWithId(ARTICLE_ID_2);
        Future<RichArticle> result_3 = instance.enrichArticleWithId(ARTICLE_ID_3);

        RichArticle richArticle_1 = result_1.get();
        RichArticle richArticle_2 = result_2.get();
        RichArticle richArticle_3 = result_3.get();
        

        assertEquals(ARTICLE_ID_1, richArticle_1.getId());
        assertEquals(ARTICLE_NAME_1, richArticle_1.getName());
        assertEquals(IMAGE_URL, richArticle_1.getHeroImage().getId());
        assertEquals(1, richArticle_1.getVideos().size());
        assertTrue(richArticle_1.getVideos().contains(VIDEO_1));
        
        assertEquals(ARTICLE_ID_2, richArticle_2.getId());
        assertEquals(ARTICLE_NAME_2, richArticle_2.getName());
        assertEquals(IMAGE_URL, richArticle_2.getHeroImage().getId());
        assertEquals(2, richArticle_2.getVideos().size());
        assertTrue(richArticle_2.getVideos().contains(VIDEO_1));
        assertTrue(richArticle_2.getVideos().contains(VIDEO_2));
        
        assertEquals(ARTICLE_ID_3, richArticle_3.getId());
        assertEquals(ARTICLE_NAME_3, richArticle_3.getName());
        assertEquals(IMAGE_URL, richArticle_3.getHeroImage().getId());
        assertEquals(3, richArticle_3.getVideos().size());
        assertTrue(richArticle_3.getVideos().contains(VIDEO_1));
        assertTrue(richArticle_3.getVideos().contains(VIDEO_2));
        assertTrue(richArticle_3.getVideos().contains(VIDEO_3));
        
        
    }

    /**
     * Test of enrichArticleWithId method, of class SimpleArticleEnricher when
     * there are no Videos available.We expect it to build a partial RichArticle
     * without any Videos.
     *
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    @Test
    public void testEnrichArticleWithIdSuccessNoVideo() throws InterruptedException, ExecutionException {
        System.out.println("testEnrichArticleWithIdSuccessNoVideo");

        SimpleArticleEnricher instance = new SimpleArticleEnricher();

        instance.setTimeoutSeconds(3);

        AssetsServiceClient mockAssetsServiceClient = mock(AssetsServiceClient.class);
        ArticleRepositoryClient mockArticleRepositoryClient = mock(ArticleRepositoryClient.class);

        Mockito.when(mockAssetsServiceClient.getImageById(IMAGE_URL))
                .thenReturn(CompletableFuture.completedFuture(IMAGE));
        Mockito.when(mockAssetsServiceClient.getVideoById(any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        Mockito.when(mockArticleRepositoryClient.getArticleReferenceForId(ARTICLE_ID_1))
                .thenReturn(CompletableFuture.completedFuture(
                        new ArticleReference(ARTICLE_ID_1, ARTICLE_NAME_1, IMAGE_URL, Arrays.asList(NON_EXISTENT_VIDEO_URL))));

        instance.setArticleRepositoryClient(mockArticleRepositoryClient);
        instance.setAssetsServiceClient(mockAssetsServiceClient);

        Future<RichArticle> result = instance.enrichArticleWithId(ARTICLE_ID_1);

        RichArticle richArticle = result.get();

        assertEquals(ARTICLE_ID_1, richArticle.getId());
        assertEquals(ARTICLE_NAME_1, richArticle.getName());
        assertEquals(IMAGE_URL, richArticle.getHeroImage().getId());
        assertEquals(0, richArticle.getVideos().size());
    }

    /**
     * Test of enrichArticleWithId method, of class SimpleArticleEnricher when
     * we have no image available. We expect it to build a partial RichArticle
     * but with the missing image
     *
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    @Test
    public void testEnrichArticleWithIdSuccessNoImage() throws InterruptedException, ExecutionException {
        System.out.println("testEnrichArticleWithIdSuccessNoImage");

        SimpleArticleEnricher instance = new SimpleArticleEnricher();
        instance.setTimeoutSeconds(3);

        AssetsServiceClient mockAssetsServiceClient = mock(AssetsServiceClient.class);
        ArticleRepositoryClient mockArticleRepositoryClient = mock(ArticleRepositoryClient.class);

        Mockito.when(mockAssetsServiceClient.getImageById(any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        Mockito.when(mockAssetsServiceClient.getVideoById(VIDEO_URL_1))
                .thenReturn(CompletableFuture.completedFuture(VIDEO_1));
        Mockito.when(mockAssetsServiceClient.getVideoById(VIDEO_URL_2))
                .thenReturn(CompletableFuture.completedFuture(VIDEO_2));
        Mockito.when(mockAssetsServiceClient.getVideoById(VIDEO_URL_3))
                .thenReturn(CompletableFuture.completedFuture(VIDEO_3));

        Mockito.when(mockArticleRepositoryClient.getArticleReferenceForId(ARTICLE_ID_1))
                .thenReturn(CompletableFuture.completedFuture(
                        new ArticleReference(ARTICLE_ID_1, ARTICLE_NAME_1, IMAGE_URL, VIDEO_URLS)));

        instance.setArticleRepositoryClient(mockArticleRepositoryClient);
        instance.setAssetsServiceClient(mockAssetsServiceClient);

        Future<RichArticle> result = instance.enrichArticleWithId(ARTICLE_ID_1);

        RichArticle richArticle = result.get();

        assertEquals(ARTICLE_ID_1, richArticle.getId());
        assertEquals(ARTICLE_NAME_1, richArticle.getName());
        assertNull(richArticle.getHeroImage());
        assertEquals(3, richArticle.getVideos().size());
        assertTrue(richArticle.getVideos().contains(VIDEO_1));
        assertTrue(richArticle.getVideos().contains(VIDEO_2));
        assertTrue(richArticle.getVideos().contains(VIDEO_3));
    }

    /**
     * Test of enrichArticleWithId method, of class SimpleArticleEnricher when
     * there is no article to reference.We expect that the Future will throw an
     * Exception caused by NullPointer when referring to ArticleReference that
     * doesn't exist.
     *
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    @Test
    public void testEnrichArticleWithIdNoArticleReference() throws InterruptedException, ExecutionException {
        System.out.println("testEnrichArticleWithIdNoArticleReference");

        SimpleArticleEnricher instance = new SimpleArticleEnricher();
        instance.setTimeoutSeconds(3);

        AssetsServiceClient mockAssetsServiceClient = mock(AssetsServiceClient.class);
        ArticleRepositoryClient mockArticleRepositoryClient = mock(ArticleRepositoryClient.class);

        Mockito.when(mockArticleRepositoryClient.getArticleReferenceForId(ARTICLE_ID_1))
                .thenReturn(CompletableFuture.completedFuture(null));

        instance.setArticleRepositoryClient(mockArticleRepositoryClient);
        instance.setAssetsServiceClient(mockAssetsServiceClient);

        CompletableFuture<RichArticle> result
                = (CompletableFuture<RichArticle>) instance.enrichArticleWithId(ARTICLE_ID_1);

        try {
            result.get();
        } catch (ExecutionException ex) {
            assertThat(ex.getCause(), instanceOf(NullPointerException.class));
        }
    }

    /**
     * Test of enrichArticleWithId method, of class SimpleArticleEnricher when
     * the Image Future takes too long and we get a TimeoutException.We should
     * get a TimeoutException thrown when we call get() on our resulting Future.
     *
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    @Test
    public void testEnrichArticleWithIdTimeoutExceptionOnImageFuture() throws InterruptedException, ExecutionException {
        System.out.println("testEnrichArticleWithIdTimeoutExceptionOnImageFuture");

        SimpleArticleEnricher instance = new SimpleArticleEnricher();
        instance.setTimeoutSeconds(3);

        AssetsServiceClient mockAssetsServiceClient = mock(AssetsServiceClient.class);
        ArticleRepositoryClient mockArticleRepositoryClient = mock(ArticleRepositoryClient.class);

        CompletableFuture<Image> timeoutCompletableFuture
                = CompletableFuture.supplyAsync(() -> {
                    try {
                        TimeUnit.SECONDS.sleep(5);
                        return null;
                    } catch (InterruptedException ex) {
                        return null;
                    }
                });

        Mockito.when(mockAssetsServiceClient.getImageById(IMAGE_URL))
                .thenReturn(timeoutCompletableFuture);

        Mockito.when(mockArticleRepositoryClient.getArticleReferenceForId(ARTICLE_ID_1))
                .thenReturn(CompletableFuture.completedFuture(
                        new ArticleReference(ARTICLE_ID_1, ARTICLE_NAME_1, IMAGE_URL, VIDEO_URLS)));

        instance.setArticleRepositoryClient(mockArticleRepositoryClient);
        instance.setAssetsServiceClient(mockAssetsServiceClient);

        CompletableFuture<RichArticle> result
                = (CompletableFuture<RichArticle>) instance.enrichArticleWithId(ARTICLE_ID_1);

        try {
            result.get();
        } catch (ExecutionException ex) {
            assertThat(ex.getCause(), instanceOf(TimeoutException.class));
        }
    }

    /**
     * Test of enrichArticleWithId method, of class SimpleArticleEnricher when
     * the Video Future takes too long and we get a TimeoutException.We should
     * get a TimeoutException thrown when we call get() on our resulting Future.
     *
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    @Test
    public void testEnrichArticleWithIdTimeoutExceptionOnVideoFuture() throws InterruptedException, ExecutionException {
        System.out.println("testEnrichArticleWithIdTimeoutExceptionOnVideoFuture");

        SimpleArticleEnricher instance = new SimpleArticleEnricher();
        instance.setTimeoutSeconds(3);

        AssetsServiceClient mockAssetsServiceClient = mock(AssetsServiceClient.class);
        ArticleRepositoryClient mockArticleRepositoryClient = mock(ArticleRepositoryClient.class);

        CompletableFuture<Video> timeoutCompletableFuture
                = CompletableFuture.supplyAsync(() -> {
                    try {
                        TimeUnit.SECONDS.sleep(5);
                        return null;
                    } catch (InterruptedException ex) {
                        return null;
                    }
                });

        Mockito.when(mockAssetsServiceClient.getImageById(IMAGE_URL))
                .thenReturn(CompletableFuture.completedFuture(IMAGE));
        Mockito.when(mockAssetsServiceClient.getVideoById(VIDEO_URL_1))
                .thenReturn(timeoutCompletableFuture);
        Mockito.when(mockAssetsServiceClient.getVideoById(VIDEO_URL_2))
                .thenReturn(CompletableFuture.completedFuture(VIDEO_2));
        Mockito.when(mockAssetsServiceClient.getVideoById(VIDEO_URL_3))
                .thenReturn(CompletableFuture.completedFuture(VIDEO_3));

        Mockito.when(mockArticleRepositoryClient.getArticleReferenceForId(ARTICLE_ID_1))
                .thenReturn(CompletableFuture.completedFuture(
                        new ArticleReference(ARTICLE_ID_1, ARTICLE_NAME_1, IMAGE_URL, VIDEO_URLS)));

        instance.setArticleRepositoryClient(mockArticleRepositoryClient);
        instance.setAssetsServiceClient(mockAssetsServiceClient);

        CompletableFuture<RichArticle> result
                = (CompletableFuture<RichArticle>) instance.enrichArticleWithId(ARTICLE_ID_1);

        try {
            result.get();
        } catch (ExecutionException ex) {
            assertThat(ex.getCause(), instanceOf(TimeoutException.class));
        }
    }

    /**
     * Test of enrichArticleWithId method, of class SimpleArticleEnricher when
     * the ArticleReference Future takes too long and we get a
     * TimeoutException.We should get a TimeoutException thrown when we call
     * get() on our resulting Future.
     *
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    @Test
    public void testEnrichArticleWithIdTimeoutExceptionOnArticleReferenceFuture() throws InterruptedException, ExecutionException {
        System.out.println("testEnrichArticleWithIdTimeoutExceptionOnArticleReferenceFuture");

        SimpleArticleEnricher instance = new SimpleArticleEnricher();
        instance.setTimeoutSeconds(3);

        AssetsServiceClient mockAssetsServiceClient = mock(AssetsServiceClient.class);
        ArticleRepositoryClient mockArticleRepositoryClient = mock(ArticleRepositoryClient.class);

        CompletableFuture<ArticleReference> timeoutCompletableFuture
                = CompletableFuture.supplyAsync(() -> {
                    try {
                        TimeUnit.SECONDS.sleep(5);
                        return null;
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                        return null;
                    }
                });

        Mockito.when(mockAssetsServiceClient.getImageById(IMAGE_URL))
                .thenReturn(CompletableFuture.completedFuture(IMAGE));
        Mockito.when(mockAssetsServiceClient.getVideoById(VIDEO_URL_1))
                .thenReturn(CompletableFuture.completedFuture(VIDEO_1));

        Mockito.when(mockArticleRepositoryClient.getArticleReferenceForId(ARTICLE_ID_1))
                .thenReturn(timeoutCompletableFuture);

        instance.setArticleRepositoryClient(mockArticleRepositoryClient);
        instance.setAssetsServiceClient(mockAssetsServiceClient);

        CompletableFuture<RichArticle> result
                = (CompletableFuture<RichArticle>) instance.enrichArticleWithId(ARTICLE_ID_1);

        try {
            result.get();
        } catch (ExecutionException ex) {
            assertThat(ex.getCause(), instanceOf(TimeoutException.class));
        }
    }

    /**
     * Test of enrichArticleWithId method, of class SimpleArticleEnricher when
     * the ArticleReference Future takes too long and we get a
     * TimeoutException.We should get a TimeoutException thrown when we call
     * get() on our resulting Future.
     *
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    @Test
    public void testEnrichArticleWithIdInteruptExceptionOnArticleReferenceFuture() throws InterruptedException, ExecutionException {
        System.out.println("testEnrichArticleWithIdInteruptExceptionOnArticleReferenceFuture");

        SimpleArticleEnricher instance = new SimpleArticleEnricher();
        instance.setTimeoutSeconds(3);

        AssetsServiceClient mockAssetsServiceClient = mock(AssetsServiceClient.class);
        ArticleRepositoryClient mockArticleRepositoryClient = mock(ArticleRepositoryClient.class);

        CompletableFuture<ArticleReference> timeoutCompletableFuture
                = CompletableFuture.supplyAsync(() -> {
                    try {
                        TimeUnit.SECONDS.sleep(5);
                        return null;
                    } catch (InterruptedException ex) {
                        return null;
                    }
                });

        Mockito.when(mockAssetsServiceClient.getImageById(IMAGE_URL))
                .thenReturn(CompletableFuture.completedFuture(IMAGE));
        Mockito.when(mockAssetsServiceClient.getVideoById(VIDEO_URL_1))
                .thenReturn(CompletableFuture.completedFuture(VIDEO_1));

        Mockito.when(mockArticleRepositoryClient.getArticleReferenceForId(ARTICLE_ID_1))
                .thenReturn(timeoutCompletableFuture);

        instance.setArticleRepositoryClient(mockArticleRepositoryClient);
        instance.setAssetsServiceClient(mockAssetsServiceClient);

        CompletableFuture<RichArticle> result
                = (CompletableFuture<RichArticle>) instance.enrichArticleWithId(ARTICLE_ID_1);

        try {
            result.get();
        } catch (ExecutionException ex) {
            assertThat(ex.getCause(), instanceOf(TimeoutException.class));
        }
    }
}
