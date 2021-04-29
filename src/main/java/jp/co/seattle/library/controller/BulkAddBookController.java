package jp.co.seattle.library.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jp.co.seattle.library.dto.BookDetailsInfo;
import jp.co.seattle.library.service.BooksService;

/**
 * @author morimotomayu
 *
 */
/**
 * @author morimotomayu
 *
 */
@Controller //APIの入り口
public class BulkAddBookController {
    final static Logger logger = LoggerFactory.getLogger(AddBooksController.class);
    final static String EXTENSION = ".csv";
    
    @Autowired
    BooksService booksService;
    /**
     * 一括登録画面初期表示
     * @param model モデル
     * @return 一括登録画面
     */
    @RequestMapping(value = "/bulkAddBook", method = RequestMethod.GET)
    public String showBulkAdd(Model model) {
    	return "bulkRegist";
    }
    
    /**
     * 一括登録ボタン押下時処理
     * @param locale ロケール情報
     * @param file csvファイル
     * @param model モデル
     * @return Home画面
     */
    @Transactional
    @RequestMapping(value = "/bulkRegist", method = RequestMethod.POST, produces = "text/plain;charset=utf-8")
	public String bulkRegist(Locale locale,
			@RequestParam("csv") MultipartFile file,
			Model model) {
        logger.info("Welcome insertBooks.java! The client locale is {}.", locale);
        //ファイル名取得
        String fileName = file.getOriginalFilename();
        //CSVファイル形式であることのチェック
        checkCsvFile(fileName,model);
        
        //ファイルを読み込む
        BufferedReader br = null;
        try {
        	InputStream stream = file.getInputStream(); 
        	Reader reader = new InputStreamReader(stream);
        	br = new BufferedReader(reader);
        	//1行以上あるか確認（最初の1行を読み込んでnullならば、1行もない事になる）
        	//model = checkLine(br,model);
        	if(checkLine(br,model) != null) {
            	return "bulkRegist";
        	}
        	//読み込み行
        	String line;
        	//分割後のデータを保持する配列
        	String[] dataList;
        	//2行目以降1行ずつ読み込み
        	while((line = br.readLine()) != null) {
        		//1行の中で各項目をカンマで分割し、Listに追加
        		dataList = line.split(",");
        		//booksTBLのカラム数と配列のサイズが同じであるか確認
        		checkColumnSize(dataList,model);
        		//DB登録      		
        		setBookDetailsInfo(dataList);
        	}
        }catch(Exception ex) {
        	ex.printStackTrace();
        	model.addAttribute("error","ファイルの読み込みに失敗しました");
        	return "bulkRegist";
        }finally {
            try{
                br.close();
              } catch(Exception ex) {
              	model.addAttribute("error","ファイルの読み込みに失敗しました");
            	return "bulkRegist";
              }
        }
        
        model.addAttribute("bookList", booksService.getBookList());
    	return "home";
    }
    
    
    /**
     * CSVファイル形式チェック
     * @param file 画面で設定されたファイル情報
     * @param model モデル
     * @return 一括登録画面 or null
     */
    public String checkCsvFile(String fileName, Model model) {
        //拡張子の部分を取得
        int index = fileName.indexOf(".");
        index += ".".length();
        String extension = fileName.substring(index);
        //拡張子部分が.csvではない場合、エラーメッセージ表示
        if(!extension.equals(EXTENSION)) {
        	model.addAttribute("error","CSVファイルを選択してください");
        	return "bulkRegist";
        }
        return null;
    }

    /**
     * CSVファイルの内容チェック
     * @param br CSV読み込み情報
     * @param model モデル
     * @return 一括登録画面 or null
     */
    public Model checkLine(BufferedReader br, Model model) throws IOException {
        String line = br.readLine();
        String a = "";
        if(StringUtils.isEmpty(a)) {
          	return model.addAttribute("error","CSVファイルの中身が空です");
//        	return "bulkRegist";
        }
    	//分割後のデータを保持する配列
    	String[] dataList;
    	//1行の中で各項目をカンマで分割し、Listに追加
		dataList = line.split(",");
		//booksTBLのカラム数と配列のサイズが同じであるか確認
		checkColumnSize(dataList,model);
		//DB登録      		
		setBookDetailsInfo(dataList);
        return null;
    }
    
    
    /**
     * CSVファイルのカラム数チェック
     * @param dataList 1行のデータ
     * @param model モデル
     * @return 一括登録画面 or null
     */
    public String checkColumnSize(String[] dataList, Model model) {
    	//Listサイズが5であることの確認（書籍名、著者名、出版社、出版日、ISBN）
    	if(dataList.length != 5) {
          	model.addAttribute("error","必須項目が足りません。");
        	return "bulkRegist";
    	}
    	return null;
    }
    
    
    /**
     * DB登録
     * @param dataList 1行のデータ
     */
    public void setBookDetailsInfo (String[] dataList) {
    	//BookDetailsInfoに格納
    	BookDetailsInfo bookDetailsInfo = new BookDetailsInfo();
    	//書籍名
    	bookDetailsInfo.setTitle(dataList[0]);
    	//著者名
    	bookDetailsInfo.setAuthor(dataList[1]);
    	//出版社
    	bookDetailsInfo.setPublisher(dataList[2]);
    	//出版日
    	bookDetailsInfo.setPublishDate(dataList[3]);
    	//ISBN
    	bookDetailsInfo.setIsbn(dataList[4]);
    	//DBに登録
    	booksService.registBook(bookDetailsInfo);
    }
}
