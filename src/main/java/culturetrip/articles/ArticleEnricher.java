package culturetrip.articles;

import culturetrip.articles.models.RichArticle;

import java.util.concurrent.Future;

public interface ArticleEnricher {
    Future<RichArticle> enrichArticleWithId(String articleId);
}
