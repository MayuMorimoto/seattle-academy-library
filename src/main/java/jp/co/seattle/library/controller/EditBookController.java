package jp.co.seattle.library.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jp.co.seattle.library.dto.BookDetailsInfo;
import jp.co.seattle.library.service.BooksService;
import jp.co.seattle.library.service.ThumbnailService;

/**
 * 編集画面コントローラー
 */
@Controller
public class EditBookController {
    final static Logger logger = LoggerFactory.getLogger(AddBooksController.class);
    @Autowired
    private BooksService bookdService;

    @Autowired
    private ThumbnailService thumbnailService;
    
    /**
     * 編集画面に遷移する
     * @param locale
     * @param bookId
     * @param model
     * @return editBook
     */
	@Transactional
	@RequestMapping(value = "/editBook", method = RequestMethod.POST)
	public String editBook(Locale locale,
            @RequestParam("bookId") Integer bookId,
            Model model) {
		//bookIdをキーに書籍情報を取得
		BookDetailsInfo bookDetailsInfo = bookdService.getBookInfo(bookId);
		
		//編集画面に遷移する
        model.addAttribute("bookInfo", bookDetailsInfo);
        return "editBook";	
	}
	
    /**
     * 更新ボタン押下時処理
     * @param locale
     * @param bookId
     * @param model
     * @return editBook
     */
	@Transactional
	@RequestMapping(value = "/updateBook", method = RequestMethod.POST, produces = "text/plain;charset=utf-8")
	public String editBook(Locale locale,
            @RequestParam("bookId") Integer bookId,
            @RequestParam("title") String title,
            @RequestParam("author") String author,
            @RequestParam("publisher") String publisher,
            @RequestParam("publishDate") String publishDate,
            @RequestParam("thumbnail") MultipartFile file,
            @RequestParam("detail") String detail,
            @RequestParam("isbn") String isbn,
            Model model) {
		
        // パラメータで受け取った書籍情報をDtoに格納する。
        BookDetailsInfo bookInfo = new BookDetailsInfo();
        bookInfo.setBookId(bookId);
        bookInfo.setTitle(title);
        bookInfo.setAuthor(author);
        bookInfo.setPublisher(publisher);
        bookInfo.setPublishDate(publishDate);
        bookInfo.setDetail(detail);
        bookInfo.setIsbn(isbn);
        
        try {
            // 日付チェック
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            sdf.setLenient(false);
            sdf.parse(publishDate);
            bookInfo.setPublishDate(publishDate);

        } catch (ParseException ex) {
            model.addAttribute("dateError", "出版日は半角数字のYYYYMMDD形式で入力してください");
        }

        // クライアントのファイルシステムにある元のファイル名を設定する
        String thumbnail = file.getOriginalFilename();

        if (!file.isEmpty()) {
            try {
                // サムネイル画像をアップロード
                String fileName = thumbnailService.uploadThumbnail(thumbnail, file);
                // URLを取得
                String thumbnailUrl = thumbnailService.getURL(fileName);

                bookInfo.setThumbnailName(fileName);
                bookInfo.setThumbnailUrl(thumbnailUrl);

            } catch (Exception e) {

                // 異常終了時の処理
                logger.error("サムネイルアップロードでエラー発生", e);
                model.addAttribute("bookDetailsInfo", bookInfo);
                return "editBook";
            }
        }
        //書籍情報を更新する
        bookdService.updateBookInfo(bookInfo);
        //詳細画面に遷移
        model.addAttribute("bookDetailsInfo", bookdService.getBookInfo(bookId));
        return "details";

	}
}
