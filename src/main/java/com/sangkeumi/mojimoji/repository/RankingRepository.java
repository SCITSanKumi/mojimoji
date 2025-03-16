package com.sangkeumi.mojimoji.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.sangkeumi.mojimoji.dto.rank.*;
import com.sangkeumi.mojimoji.entity.User;

public interface RankingRepository extends JpaRepository<User, Long> {

  @Query(value = """
      SELECT
          CAST(U.user_id AS BIGINT) AS userId,
          U.nickname AS nickname,
          U.profile_url AS profileUrl,
          CAST(COALESCE(B.BookCount, 0) AS BIGINT) AS books,
          CAST(COALESCE(BL.LineCount, 0) AS BIGINT) AS bookLines,
          CAST(COALESCE(KC.KanjiCollectionCount, 0) AS BIGINT) AS kanjiCollections,
          CAST(COALESCE(KC.KanjiCollectionBonus, 0) AS BIGINT) AS kanjiCollectionBonus,
          CAST(COALESCE(SB.SharedBooksCount, 0) AS BIGINT) AS sharedBooks,
          CAST(COALESCE(SB.TotalHit, 0) AS BIGINT) AS totalHit,
          CAST(COALESCE(SB.TotalGaechu, 0) AS BIGINT) AS totalGaechu,
          CAST(COALESCE(SBR.SharedBookReplyCount, 0) AS BIGINT) AS sharedBookReplies,
          CAST((COALESCE(B.BookCount, 0) * 5 + COALESCE(BL.LineCount, 0) * 1) AS DOUBLE) AS bookScore,
          CAST((COALESCE(KC.KanjiCollectionCount, 0) * 2 + COALESCE(KC.KanjiCollectionBonus, 0) * 1) AS DOUBLE) AS kanjiScore,
          CAST((COALESCE(SB.SharedBooksCount, 0) * 4 + COALESCE(SB.TotalHit, 0) / 100.0 +
                COALESCE(SB.TotalGaechu, 0) * 2 + COALESCE(SBR.SharedBookReplyCount, 0) * 2) AS DOUBLE) AS likeScore,
          CAST(((COALESCE(B.BookCount, 0) * 5 + COALESCE(BL.LineCount, 0) * 1) +
                (COALESCE(KC.KanjiCollectionCount, 0) * 2 + COALESCE(KC.KanjiCollectionBonus, 0) * 1) +
                (COALESCE(SB.SharedBooksCount, 0) * 4 + COALESCE(SB.TotalHit, 0) / 100.0 +
                 COALESCE(SB.TotalGaechu, 0) * 2 + COALESCE(SBR.SharedBookReplyCount, 0) * 2)
               ) AS DOUBLE) AS totalScore,
          CAST((COALESCE(KC.KanjiCollectionCount, 0) * 100.0 / 2136) AS DOUBLE) AS collectionPercentage
      FROM Users U
      LEFT JOIN (SELECT user_id, COUNT(*) AS BookCount FROM Books GROUP BY user_id) B ON U.user_id = B.user_id
      LEFT JOIN (
          SELECT B.user_id, COUNT(*) AS LineCount
          FROM Book_Lines BL JOIN Books B ON BL.book_id = B.book_id
          GROUP BY B.user_id
      ) BL ON U.user_id = BL.user_id
      LEFT JOIN (
          SELECT B.user_id,
                 COUNT(*) AS SharedBooksCount,
                 SUM(hit_count) AS TotalHit,
                 SUM(gaechu) AS TotalGaechu
          FROM Shared_Books SB JOIN Books B ON SB.book_id = B.book_id
          GROUP BY B.user_id
      ) SB ON U.user_id = SB.user_id
      LEFT JOIN (
          SELECT B.user_id,
                 COUNT(*) AS SharedBookReplyCount
          FROM Shared_Book_Replies SBR
          JOIN Shared_Books SB ON SBR.shared_book_id = SB.shared_book_id
          JOIN Books B ON SB.book_id = B.book_id
          GROUP BY B.user_id
      ) SBR ON U.user_id = SBR.user_id
      LEFT JOIN (
          SELECT user_id,
                 COUNT(*) AS KanjiCollectionCount,
                 SUM(collected_count) AS KanjiCollectionBonus
      FROM Kanji_Collections
          WHERE collected_count <> 0
          GROUP BY user_id
      ) KC ON U.user_id = KC.user_id
      ORDER BY totalScore DESC
      """, nativeQuery = true)
  List<Ranking> findOverallRankingRecords();

  @Query(value = """
          SELECT
              CAST(U.user_id AS BIGINT) AS userId,
              U.nickname AS nickname,
              U.profile_url AS profileUrl,
              CAST(COALESCE(KC.KanjiCollectionCount, 0) AS BIGINT) AS kanjiCollections,
              CAST(COALESCE(KC.KanjiCollectionBonus, 0) AS BIGINT) AS kanjiCollectionBonus,
              CAST((COALESCE(KC.KanjiCollectionCount, 0) * 2 + COALESCE(KC.KanjiCollectionBonus, 0) * 1) AS DOUBLE) AS kanjiScore,
              CAST((COALESCE(KC.KanjiCollectionCount, 0) * 100.0 / 2136) AS DOUBLE) AS collectionPercentage
          FROM Users U
          LEFT JOIN (
              SELECT user_id,
                     COUNT(*) AS KanjiCollectionCount,
                     SUM(collected_count) AS KanjiCollectionBonus
      FROM Kanji_Collections
              WHERE collected_count <> 0
              GROUP BY user_id
          ) KC ON U.user_id = KC.user_id
          ORDER BY kanjiScore DESC
          """, nativeQuery = true)
  List<KanjiRanking> findKanjiRankingRecords();

  @Query(value = """
      SELECT
          CAST(U.user_id AS BIGINT) AS userId,
          U.nickname AS nickname,
          U.profile_url AS profileUrl,
          CAST(COALESCE(B.BookCount, 0) AS BIGINT) AS books,
          CAST(COALESCE(BL.LineCount, 0) AS BIGINT) AS bookLines,
          CAST((COALESCE(B.BookCount, 0) * 5 + COALESCE(BL.LineCount, 0) * 1) AS DOUBLE) AS bookScore
      FROM Users U
      LEFT JOIN (SELECT user_id, COUNT(*) AS BookCount FROM Books GROUP BY user_id) B ON U.user_id = B.user_id
      LEFT JOIN (
          SELECT B.user_id, COUNT(*) AS LineCount
          FROM Book_Lines BL JOIN Books B ON BL.book_id = B.book_id
          GROUP BY B.user_id
      ) BL ON U.user_id = BL.user_id
      ORDER BY bookScore DESC
      """, nativeQuery = true)
  List<BookRanking> findBookRankingRecords();

  @Query(value = """
      SELECT
          CAST(U.user_id AS BIGINT) AS userId,
          U.nickname AS nickname,
          U.profile_url AS profileUrl,
          CAST(COALESCE(SB.SharedBooksCount, 0) AS BIGINT) AS sharedBooks,
          CAST(COALESCE(SB.TotalHit, 0) AS BIGINT) AS totalHit,
          CAST(COALESCE(SB.TotalGaechu, 0) AS BIGINT) AS totalGaechu,
          CAST(COALESCE(SBR.SharedBookReplyCount, 0) AS BIGINT) AS sharedBookReplies,
          CAST((COALESCE(SB.SharedBooksCount, 0) * 4 + COALESCE(SB.TotalHit, 0) / 100.0 +
                COALESCE(SB.TotalGaechu, 0) * 2 + COALESCE(SBR.SharedBookReplyCount, 0) * 2) AS DOUBLE) AS likeScore
      FROM Users U
      LEFT JOIN (
          SELECT B.user_id,
                 COUNT(*) AS SharedBooksCount,
                 SUM(hit_count) AS TotalHit,
                 SUM(gaechu) AS TotalGaechu
          FROM Shared_Books SB JOIN Books B ON SB.book_id = B.book_id
          GROUP BY B.user_id
      ) SB ON U.user_id = SB.user_id
      LEFT JOIN (
          SELECT B.user_id,
                 COUNT(*) AS SharedBookReplyCount
          FROM Shared_Book_Replies SBR
          JOIN Shared_Books SB ON SBR.shared_book_id = SB.shared_book_id
          JOIN Books B ON SB.book_id = B.book_id
          GROUP BY B.user_id
      ) SBR ON U.user_id = SBR.user_id
      ORDER BY likeScore DESC
      """, nativeQuery = true)
  List<LikeRanking> findLikesRankingRecords();
}
