package culturetrip.articles.clients;

import culturetrip.articles.models.ArticleReference;

import java.util.concurrent.Future;

public interface ArticleRepositoryClient {
    Future<ArticleReference> getArticleReferenceForId(String articleId);
}
