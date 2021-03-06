package jp.co.seattle.library.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jp.co.seattle.library.dto.BookDetailsInfo;
import jp.co.seattle.library.dto.BookInfo;
import jp.co.seattle.library.rowMapper.BookDetailsInfoRowMapper;
import jp.co.seattle.library.rowMapper.BookInfoRowMapper;

/**
 * 書籍サービス
 * 
 *  booksテーブルに関する処理を実装する
 */
@Service
public class BooksService {
    final static Logger logger = LoggerFactory.getLogger(BooksService.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 書籍リストを取得する
     *
     * @return 書籍リスト
     */
    public List<BookInfo> getBookList() {

        //取得したい情報を取得するようにSQLを修正
        List<BookInfo> getedBookList = jdbcTemplate.query(
                "select * from books order by title ASC",
                new BookInfoRowMapper());

        return getedBookList;
    }

    /**
     * 書籍IDに紐づく書籍詳細情報を取得する
     *
     * @param bookId 書籍ID
     * @return 書籍情報
     */
    public BookDetailsInfo getBookInfo(int bookId) {

        // JSPに渡すデータを設定する
        String sql = "SELECT * FROM books where id ="
                + bookId;

        BookDetailsInfo bookDetailsInfo = jdbcTemplate.queryForObject(sql, new BookDetailsInfoRowMapper());

        return bookDetailsInfo;
    }



    /**
     * 書籍を登録する
     *
     * @param bookInfo 書籍情報
     */
    public void registBook(BookDetailsInfo bookInfo) {

        String sql = "INSERT INTO books (title,author,publisher,thumbnail_name,thumbnail_url,reg_date,upd_date,detail,isbn,publish_date) VALUES ('"
                + bookInfo.getTitle() + "','" + bookInfo.getAuthor() + "','" + bookInfo.getPublisher() + "','"
                + bookInfo.getThumbnailName() + "','"
                + bookInfo.getThumbnailUrl() + "',"
                + "sysdate(),"
                + "sysdate(),"
                + "'" + bookInfo.getDetail() + "',"
                + "'" + bookInfo.getIsbn() + "',"
                + "'" + bookInfo.getPublishDate() + "'"
                + ");";

        jdbcTemplate.update(sql);
    }
    
    /**
     * 書籍を削除する
     *
     * @param bookId 書籍ID
     */
    public void deleteBook(Integer bookId) {
    	//削除SQL実行
    	String sql = "delete from books where id = " + bookId + ";";
    	jdbcTemplate.update(sql);
    }
    
    /**
     * 追加した本の書籍IDを取得
     *
     * @return　書籍ID
     */
    public Integer getBookId() {
    	//SQL実行
    	String sql = "select * from books where id = (select max(id) from books);";
    	BookDetailsInfo bookDetailsInfo = jdbcTemplate.queryForObject(sql, new BookDetailsInfoRowMapper());
    	
    	return bookDetailsInfo.getBookId();
    } 
    
    /**
     * 書籍を更新する
     *
     * @param bookInfo 書籍情報
     */
    public void updateBookInfo(BookDetailsInfo bookInfo) {
    	//サムネイルを更新していない場合はサムネイルは更新しない
    	String sql = null;
    	if(StringUtils.isEmpty(bookInfo.getThumbnailUrl())) {
    		sql = "update books set title = '"+ bookInfo.getTitle() + "', author = '" + bookInfo.getAuthor() + "', publisher = '" + bookInfo.getPublisher() + "', publish_date = '" + bookInfo.getPublishDate() + "', detail = '" + bookInfo.getDetail() + "', isbn = '" + bookInfo.getIsbn() + "' where id = " + bookInfo.getBookId() + ";";
    	}else {
    		sql = "update books set title = '"+ bookInfo.getTitle() + "', author = '" + bookInfo.getAuthor() + "', publisher = '" + bookInfo.getPublisher() + "', publish_date = '" + bookInfo.getPublishDate() + "', detail = '" + bookInfo.getDetail() + "', isbn = '" + bookInfo.getIsbn() + "', thumbnail_url = '"+ bookInfo.getThumbnailUrl() + "', thumbnail_name = '" + bookInfo.getThumbnailName() + "' where id = " + bookInfo.getBookId() + ";";
    	}
    	//SQL実行
    	jdbcTemplate.update(sql);
    }
}
