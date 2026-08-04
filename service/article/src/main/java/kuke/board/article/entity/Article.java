package kuke.board.article.entity;

import ch.qos.logback.classic.model.processor.LogbackClassicDefaultNestedComponentRules;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "article")
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article {

    @Id
    private Long articleId;
    private String title;
    private String content;
    private Long boardId; //shard key
    private Long writerId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    // 굳이 생성자 대신 정적 팩토리 메서드(create)를 사용하는 이유:
    // 생성 로직이 복잡해질 경우(제목/본문 검증, 공백 제거, 초기 상태 설정 등)
    // 관련 로직을 한곳에서 관리하기 쉽고, 객체 생성 의도를 명확하게 표현할 수 있기 때문
    public static Article create(Long articleId, String title, String content, Long boardId, Long writerId) {
        Article article = new Article();
        article.articleId = articleId;
        article.title = title;
        article.content = content;
        article.boardId = boardId;
        article.writerId = writerId;
        article.createdAt = LocalDateTime.now();
        article.modifiedAt = article.createdAt;

        return article;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        this.modifiedAt = LocalDateTime.now();
    }
}
